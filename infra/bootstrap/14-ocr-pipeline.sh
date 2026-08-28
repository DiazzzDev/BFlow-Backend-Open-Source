#!/usr/bin/env bash
# 14-ocr-pipeline.sh — Provisions the async receipt-OCR pipeline:
#
#   ECS Task Role --(SendMessage)--> Requests SQS Queue
#   Textract      --(Publish)------> Results SNS Topic --(subscribed)--> Results SQS Queue
#   ECS Task Role --(ReceiveMessage/DeleteMessage)--> Results SQS Queue
#
# Textract needs its own IAM role (TEXTRACT_SNS_ROLE_ARN) to publish job
# completion notifications to the results SNS topic. The ECS Task Role
# needs iam:PassRole on that role to be allowed to hand it to Textract
# when starting an async job (StartExpenseAnalysis / StartDocumentTextDetection).
#
# Idempotent: safe to re-run. Queue/topic creation is idempotent by name,
# and put-role-policy overwrites the named inline policy in place.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

source "$SCRIPT_DIR/../config.env"
source "$SCRIPT_DIR/../outputs.env"
source "$SCRIPT_DIR/../lib/helpers.sh"

OUTPUT_FILE="$SCRIPT_DIR/../outputs.env"

REQUESTS_QUEUE_NAME="${PROJECT_NAME}-receipt-ocr-requests"
RESULTS_QUEUE_NAME="${PROJECT_NAME}-receipt-ocr-results"
RESULTS_TOPIC_NAME="${PROJECT_NAME}-receipt-ocr-results"
TEXTRACT_ROLE_NAME="${PROJECT_NAME}-textract-sns-role"

create_queue() {

    local QUEUE_NAME="$1"

    local QUEUE_URL
    QUEUE_URL=$(aws sqs get-queue-url \
        --queue-name "$QUEUE_NAME" \
        --region "$AWS_REGION" \
        --query "QueueUrl" \
        --output text 2>/dev/null || true)

    if [[ -z "$QUEUE_URL" || "$QUEUE_URL" == "None" ]]; then

        echo "Creating SQS queue: $QUEUE_NAME"

        QUEUE_URL=$(aws sqs create-queue \
            --queue-name "$QUEUE_NAME" \
            --region "$AWS_REGION" \
            --attributes '{
                "VisibilityTimeout": "120",
                "MessageRetentionPeriod": "345600"
            }' \
            --tags \
                Project="$PROJECT_NAME",Environment="$ENVIRONMENT",ManagedBy="$MANAGED_BY" \
            --query "QueueUrl" \
            --output text)

    else
        echo "SQS queue already exists: $QUEUE_NAME"
    fi

    echo "$QUEUE_URL"
}

echo "Creating requests queue..."
REQUESTS_QUEUE_URL=$(create_queue "$REQUESTS_QUEUE_NAME")

echo "Creating results queue..."
RESULTS_QUEUE_URL=$(create_queue "$RESULTS_QUEUE_NAME")

RESULTS_QUEUE_ARN=$(aws sqs get-queue-attributes \
    --queue-url "$RESULTS_QUEUE_URL" \
    --region "$AWS_REGION" \
    --attribute-names QueueArn \
    --query "Attributes.QueueArn" \
    --output text)

echo "Creating results SNS topic..."

RESULTS_TOPIC_ARN=$(aws sns create-topic \
    --name "$RESULTS_TOPIC_NAME" \
    --region "$AWS_REGION" \
    --tags \
        Key=Project,Value="$PROJECT_NAME" \
        Key=Environment,Value="$ENVIRONMENT" \
        Key=ManagedBy,Value="$MANAGED_BY" \
    --query "TopicArn" \
    --output text)

echo "Subscribing results queue to results topic..."

SUBSCRIPTION_ARN=$(aws sns list-subscriptions-by-topic \
    --topic-arn "$RESULTS_TOPIC_ARN" \
    --region "$AWS_REGION" \
    --query "Subscriptions[?Endpoint=='${RESULTS_QUEUE_ARN}'].SubscriptionArn | [0]" \
    --output text)

if [[ -z "$SUBSCRIPTION_ARN" || "$SUBSCRIPTION_ARN" == "None" ]]; then

    aws sns subscribe \
        --topic-arn "$RESULTS_TOPIC_ARN" \
        --protocol sqs \
        --notification-endpoint "$RESULTS_QUEUE_ARN" \
        --region "$AWS_REGION" \
        >/dev/null

else
    echo "Subscription already exists."
fi

echo "Allowing SNS to deliver to the results queue..."

# The results queue must explicitly allow the SNS topic to SendMessage,
# or delivery silently fails (messages are dropped, no error surfaced).
aws sqs set-queue-attributes \
    --queue-url "$RESULTS_QUEUE_URL" \
    --region "$AWS_REGION" \
    --attributes "{
        \"Policy\": \"{
            \\\"Version\\\": \\\"2012-10-17\\\",
            \\\"Statement\\\": [{
                \\\"Effect\\\": \\\"Allow\\\",
                \\\"Principal\\\": {\\\"Service\\\": \\\"sns.amazonaws.com\\\"},
                \\\"Action\\\": \\\"sqs:SendMessage\\\",
                \\\"Resource\\\": \\\"${RESULTS_QUEUE_ARN}\\\",
                \\\"Condition\\\": {\\\"ArnEquals\\\": {\\\"aws:SourceArn\\\": \\\"${RESULTS_TOPIC_ARN}\\\"}}
            }]
        }\"
    }"

echo "Creating Textract SNS publish role..."

TEXTRACT_TRUST_POLICY='{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Principal": {
                "Service": "textract.amazonaws.com"
            },
            "Action": "sts:AssumeRole"
        }
    ]
}'

TEXTRACT_ROLE_ARN=$(aws iam get-role \
    --role-name "$TEXTRACT_ROLE_NAME" \
    --query "Role.Arn" \
    --output text 2>/dev/null || true)

if [[ -z "$TEXTRACT_ROLE_ARN" || "$TEXTRACT_ROLE_ARN" == "None" ]]; then

    echo "Creating role: $TEXTRACT_ROLE_NAME"

    TEXTRACT_ROLE_ARN=$(aws iam create-role \
        --role-name "$TEXTRACT_ROLE_NAME" \
        --assume-role-policy-document "$TEXTRACT_TRUST_POLICY" \
        --tags \
            Key=Project,Value="$PROJECT_NAME" \
            Key=Environment,Value="$ENVIRONMENT" \
            Key=ManagedBy,Value="$MANAGED_BY" \
        --query "Role.Arn" \
        --output text)

else
    echo "Role already exists: $TEXTRACT_ROLE_NAME"
fi

aws iam put-role-policy \
    --role-name "$TEXTRACT_ROLE_NAME" \
    --policy-name "${PROJECT_NAME}-textract-publish-results" \
    --policy-document "{
        \"Version\": \"2012-10-17\",
        \"Statement\": [
            {
                \"Effect\": \"Allow\",
                \"Action\": \"sns:Publish\",
                \"Resource\": \"${RESULTS_TOPIC_ARN}\"
            }
        ]
    }"

echo "Granting the ECS task role access to the OCR pipeline..."

if [[ -z "${ECS_TASK_ROLE_NAME:-}" ]]; then
    echo "ECS_TASK_ROLE_NAME is not set in config.env — skipping task role grant."
    echo "Run bootstrap/09-iam.sh first, then re-run this script."
else

    REQUESTS_QUEUE_ARN=$(aws sqs get-queue-attributes \
        --queue-url "$REQUESTS_QUEUE_URL" \
        --region "$AWS_REGION" \
        --attribute-names QueueArn \
        --query "Attributes.QueueArn" \
        --output text)

    aws iam put-role-policy \
        --role-name "$ECS_TASK_ROLE_NAME" \
        --policy-name "${PROJECT_NAME}-receipt-ocr-access" \
        --policy-document "{
            \"Version\": \"2012-10-17\",
            \"Statement\": [
                {
                    \"Sid\": \"SendOcrRequests\",
                    \"Effect\": \"Allow\",
                    \"Action\": [\"sqs:SendMessage\", \"sqs:GetQueueAttributes\"],
                    \"Resource\": \"${REQUESTS_QUEUE_ARN}\"
                },
                {
                    \"Sid\": \"ConsumeOcrResults\",
                    \"Effect\": \"Allow\",
                    \"Action\": [
                        \"sqs:ReceiveMessage\",
                        \"sqs:DeleteMessage\",
                        \"sqs:GetQueueAttributes\"
                    ],
                    \"Resource\": \"${RESULTS_QUEUE_ARN}\"
                },
                {
                    \"Sid\": \"StartTextractJobs\",
                    \"Effect\": \"Allow\",
                    \"Action\": [
                        \"textract:StartExpenseAnalysis\",
                        \"textract:StartDocumentTextDetection\",
                        \"textract:GetExpenseAnalysis\",
                        \"textract:GetDocumentTextDetection\"
                    ],
                    \"Resource\": \"*\"
                },
                {
                    \"Sid\": \"PassTextractRole\",
                    \"Effect\": \"Allow\",
                    \"Action\": \"iam:PassRole\",
                    \"Resource\": \"${TEXTRACT_ROLE_ARN}\",
                    \"Condition\": {
                        \"StringEquals\": {\"iam:PassedToService\": \"textract.amazonaws.com\"}
                    }
                }
            ]
        }"
fi

append_output "RECEIPT_OCR_REQUESTS_QUEUE_URL" "$REQUESTS_QUEUE_URL"
append_output "RECEIPT_OCR_RESULTS_QUEUE_URL" "$RESULTS_QUEUE_URL"
append_output "RECEIPT_OCR_RESULTS_TOPIC_ARN" "$RESULTS_TOPIC_ARN"
append_output "TEXTRACT_SNS_ROLE_ARN" "$TEXTRACT_ROLE_ARN"

echo "OCR pipeline ready."
echo "Copy these into your GitHub repository Variables (Settings > Environments > production):"
echo "  RECEIPT_OCR_REQUESTS_QUEUE_URL=$REQUESTS_QUEUE_URL"
echo "  RECEIPT_OCR_RESULTS_QUEUE_URL=$RESULTS_QUEUE_URL"
echo "  RECEIPT_OCR_RESULTS_TOPIC_ARN=$RESULTS_TOPIC_ARN"
echo "  TEXTRACT_SNS_ROLE_ARN=$TEXTRACT_ROLE_ARN"