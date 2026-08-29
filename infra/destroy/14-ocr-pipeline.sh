#!/usr/bin/env bash
# 14-ocr-pipeline.sh (destroy) — Tears down the receipt-OCR pipeline:
# SQS queues, SNS topic + subscription, Textract IAM role, and the
# inline policy granted to the ECS task role.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

source "$SCRIPT_DIR/../config.env"
source "$SCRIPT_DIR/../outputs.env"
source "$SCRIPT_DIR/../lib/helpers.sh"

REQUESTS_QUEUE_NAME="${PROJECT_NAME}-receipt-ocr-requests"
RESULTS_QUEUE_NAME="${PROJECT_NAME}-receipt-ocr-results"
RESULTS_TOPIC_NAME="${PROJECT_NAME}-receipt-ocr-results"
TEXTRACT_ROLE_NAME="${PROJECT_NAME}-textract-sns-role"

echo "Removing OCR access from the ECS task role..."

if [[ -n "${ECS_TASK_ROLE_NAME:-}" ]]; then
    aws iam delete-role-policy \
        --role-name "$ECS_TASK_ROLE_NAME" \
        --policy-name "${PROJECT_NAME}-receipt-ocr-access" \
        2>/dev/null || true
fi

echo "Deleting Textract SNS publish role..."

if aws iam get-role --role-name "$TEXTRACT_ROLE_NAME" >/dev/null 2>&1; then

    aws iam delete-role-policy \
        --role-name "$TEXTRACT_ROLE_NAME" \
        --policy-name "${PROJECT_NAME}-textract-publish-results" \
        2>/dev/null || true

    aws iam delete-role --role-name "$TEXTRACT_ROLE_NAME"

else
    echo "Role already deleted: $TEXTRACT_ROLE_NAME"
fi

echo "Deleting SNS topic (and its subscription)..."

TOPIC_ARN=$(aws sns list-topics \
    --region "$AWS_REGION" \
    --query "Topics[?ends_with(TopicArn, ':${RESULTS_TOPIC_NAME}')].TopicArn | [0]" \
    --output text)

if [[ -n "$TOPIC_ARN" && "$TOPIC_ARN" != "None" ]]; then
    aws sns delete-topic --topic-arn "$TOPIC_ARN" --region "$AWS_REGION"
else
    echo "Topic already deleted: $RESULTS_TOPIC_NAME"
fi

delete_queue() {

    local QUEUE_NAME="$1"

    local QUEUE_URL
    QUEUE_URL=$(aws sqs get-queue-url \
        --queue-name "$QUEUE_NAME" \
        --region "$AWS_REGION" \
        --query "QueueUrl" \
        --output text 2>/dev/null || true)

    if [[ -n "$QUEUE_URL" && "$QUEUE_URL" != "None" ]]; then
        echo "Deleting queue: $QUEUE_NAME"
        aws sqs delete-queue --queue-url "$QUEUE_URL" --region "$AWS_REGION"
    else
        echo "Queue already deleted: $QUEUE_NAME"
    fi
}

delete_queue "$REQUESTS_QUEUE_NAME"
delete_queue "$RESULTS_QUEUE_NAME"

for KEY in RECEIPT_OCR_REQUESTS_QUEUE_URL RECEIPT_OCR_RESULTS_QUEUE_URL RECEIPT_OCR_RESULTS_TOPIC_ARN TEXTRACT_SNS_ROLE_ARN; do
    grep -v "^${KEY}=" "$SCRIPT_DIR/../outputs.env" > "$SCRIPT_DIR/../outputs.env.tmp" || true
    mv "$SCRIPT_DIR/../outputs.env.tmp" "$SCRIPT_DIR/../outputs.env"
done

echo "OCR pipeline removed."