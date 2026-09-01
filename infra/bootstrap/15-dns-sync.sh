#!/usr/bin/env bash
# 15-dns-sync.sh — Keeps the Cloudflare DNS record for the backend in sync
# with the ECS task's public IP, independently of GitHub Actions deploys.
#
#   ECS Task State Change (RUNNING) --> EventBridge Rule --> Lambda
#                                                               │
#                                                               ▼
#                                          DescribeNetworkInterfaces (EC2)
#                                                               │
#                                                               ▼
#                                        Cloudflare API (update A record)
#
# Without an ALB, the task's public IP changes on every replacement, not
# just on deploys (crash, OOM kill, host retirement, Fargate Spot
# interruption). This closes that gap: ANY RUNNING task for
# ECS_SERVICE_NAME triggers a sync. Because of this, the
# "Update Cloudflare DNS record" step in deploy.yml has been removed —
# this Lambda is now the single source of truth for that record.
#
# Idempotent: safe to re-run. Requires infra/cloudflare.env (see
# infra/cloudflare.env.example) — skips with instructions if missing.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

source "$SCRIPT_DIR/../config.env"
source "$SCRIPT_DIR/../outputs.env"
source "$SCRIPT_DIR/../lib/helpers.sh"

OUTPUT_FILE="$SCRIPT_DIR/../outputs.env"

LAMBDA_ROLE_NAME="${PROJECT_NAME}-dns-sync-lambda-role"
LAMBDA_FUNCTION_NAME="${PROJECT_NAME}-dns-sync"
EVENTBRIDGE_RULE_NAME="${PROJECT_NAME}-ecs-task-state-change"
CLOUDFLARE_SECRET_NAME="${PROJECT_NAME}/cloudflare-dns"
LAMBDA_SOURCE_DIR="$SCRIPT_DIR/../dns-sync"
LAMBDA_ZIP="$LAMBDA_SOURCE_DIR/.build/lambda_function.zip"

if [[ ! -f "$SCRIPT_DIR/../cloudflare.env" ]]; then
    echo "infra/cloudflare.env not found - skipping DNS sync setup."
    echo "Copy infra/cloudflare.env.example to infra/cloudflare.env and fill it in to enable this."
    exit 0
fi

source "$SCRIPT_DIR/../cloudflare.env"

for VAR in CLOUDFLARE_API_TOKEN CLOUDFLARE_ZONE_ID CLOUDFLARE_DNS_RECORD_ID CLOUDFLARE_DNS_RECORD_NAME; do
    if [[ -z "${!VAR:-}" ]]; then
        echo "Missing $VAR in infra/cloudflare.env."
        exit 1
    fi
done

ECS_CLUSTER_ARN=$(require_output ECS_CLUSTER_ARN)

# --- Secrets Manager: Cloudflare credentials --------------------------------

create_or_update_cloudflare_secret() {

    local SECRET_ARN
    local SECRET_VALUE

    SECRET_VALUE=$(jq -n \
        --arg token "$CLOUDFLARE_API_TOKEN" \
        --arg zone "$CLOUDFLARE_ZONE_ID" \
        --arg record "$CLOUDFLARE_DNS_RECORD_ID" \
        --arg name "$CLOUDFLARE_DNS_RECORD_NAME" \
    '{
        "CLOUDFLARE_API_TOKEN": $token,
        "CLOUDFLARE_ZONE_ID": $zone,
        "CLOUDFLARE_DNS_RECORD_ID": $record,
        "CLOUDFLARE_DNS_RECORD_NAME": $name
    }')

    if aws secretsmanager describe-secret \
        --region "$AWS_REGION" \
        --secret-id "$CLOUDFLARE_SECRET_NAME" >/dev/null 2>&1; then

        echo "Cloudflare secret already exists. Updating value..." >&2

        aws secretsmanager put-secret-value \
            --region "$AWS_REGION" \
            --secret-id "$CLOUDFLARE_SECRET_NAME" \
            --secret-string "$SECRET_VALUE" \
            >/dev/null

        SECRET_ARN=$(aws secretsmanager describe-secret \
            --region "$AWS_REGION" \
            --secret-id "$CLOUDFLARE_SECRET_NAME" \
            --query ARN \
            --output text)

    else

        echo "Creating Cloudflare secret..." >&2

        SECRET_ARN=$(aws secretsmanager create-secret \
            --region "$AWS_REGION" \
            --name "$CLOUDFLARE_SECRET_NAME" \
            --description "Cloudflare DNS credentials for ${PROJECT_NAME} dns-sync Lambda" \
            --secret-string "$SECRET_VALUE" \
            --tags \
                Key=Project,Value="$PROJECT_NAME" \
                Key=Environment,Value="$ENVIRONMENT" \
                Key=ManagedBy,Value="$MANAGED_BY" \
            --query "ARN" \
            --output text)

    fi

    append_output "CLOUDFLARE_SECRET_ARN" "$SECRET_ARN"
    echo "$SECRET_ARN"
}

# --- IAM: Lambda execution role ----------------------------------------------

create_lambda_role() {

    local SECRET_ARN="$1"
    local ROLE_ARN

    ROLE_ARN=$(aws iam get-role \
        --role-name "$LAMBDA_ROLE_NAME" \
        --query "Role.Arn" \
        --output text 2>/dev/null || true)

    if [[ -z "$ROLE_ARN" || "$ROLE_ARN" == "None" ]]; then

        echo "Creating role: $LAMBDA_ROLE_NAME" >&2

        ROLE_ARN=$(aws iam create-role \
            --role-name "$LAMBDA_ROLE_NAME" \
            --assume-role-policy-document '{
                "Version": "2012-10-17",
                "Statement": [
                    {
                        "Effect": "Allow",
                        "Principal": {"Service": "lambda.amazonaws.com"},
                        "Action": "sts:AssumeRole"
                    }
                ]
            }' \
            --tags \
                Key=Project,Value="$PROJECT_NAME" \
                Key=Environment,Value="$ENVIRONMENT" \
                Key=ManagedBy,Value="$MANAGED_BY" \
            --query "Role.Arn" \
            --output text)

        # Role propagation lag - creating the Lambda immediately after a
        # brand-new role can fail with "cannot be assumed by Lambda".
        sleep 8

    else
        echo "Role already exists: $LAMBDA_ROLE_NAME" >&2
    fi

    aws iam attach-role-policy \
        --role-name "$LAMBDA_ROLE_NAME" \
        --policy-arn "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole" \
        2>/dev/null || true

    aws iam put-role-policy \
        --role-name "$LAMBDA_ROLE_NAME" \
        --policy-name "${PROJECT_NAME}-dns-sync-permissions" \
        --policy-document "{
            \"Version\": \"2012-10-17\",
            \"Statement\": [
                {
                    \"Sid\": \"DescribeEcsAndEni\",
                    \"Effect\": \"Allow\",
                    \"Action\": [
                        \"ecs:DescribeTasks\",
                        \"ecs:ListTasks\",
                        \"ec2:DescribeNetworkInterfaces\"
                    ],
                    \"Resource\": \"*\"
                },
                {
                    \"Sid\": \"ReadCloudflareSecret\",
                    \"Effect\": \"Allow\",
                    \"Action\": \"secretsmanager:GetSecretValue\",
                    \"Resource\": \"${SECRET_ARN}\"
                }
            ]
        }"

    append_output "DNS_SYNC_LAMBDA_ROLE_ARN" "$ROLE_ARN"
    echo "$ROLE_ARN"
}

# --- Lambda function ----------------------------------------------------------

package_lambda() {
    local BUILD_DIR="$LAMBDA_SOURCE_DIR/.build"

    if ! command -v go >/dev/null 2>&1; then
        echo "Go toolchain not found. Install Go to build the dns-sync Lambda."
        exit 1
    fi

    echo "Compiling dns-sync Lambda (Go, linux/arm64)..." >&2

    mkdir -p "$BUILD_DIR"
    rm -f "$LAMBDA_ZIP" "$BUILD_DIR/bootstrap"

    (
        cd "$LAMBDA_SOURCE_DIR"
        CGO_ENABLED=0 GOOS=linux GOARCH=arm64 \
            go build -trimpath -ldflags="-s -w" -o "$BUILD_DIR/bootstrap" .
    )

    (cd "$BUILD_DIR" && zip -q "$LAMBDA_ZIP" bootstrap)
}

deploy_lambda() {

    local ROLE_ARN="$1"
    local SECRET_ARN="$2"
    local FUNCTION_ARN
    local ENV_JSON

    package_lambda

    ENV_JSON="{
        \"Variables\": {
            \"ECS_CLUSTER_NAME\": \"$ECS_CLUSTER_NAME\",
            \"ECS_SERVICE_NAME\": \"$ECS_SERVICE_NAME\",
            \"CLOUDFLARE_SECRET_ARN\": \"$SECRET_ARN\"
        }
    }"

    if aws lambda get-function \
        --region "$AWS_REGION" \
        --function-name "$LAMBDA_FUNCTION_NAME" >/dev/null 2>&1; then

        echo "Updating Lambda code: $LAMBDA_FUNCTION_NAME" >&2

        aws lambda update-function-code \
            --region "$AWS_REGION" \
            --function-name "$LAMBDA_FUNCTION_NAME" \
            --zip-file "fileb://$LAMBDA_ZIP" \
            >/dev/null

        aws lambda wait function-updated \
            --region "$AWS_REGION" \
            --function-name "$LAMBDA_FUNCTION_NAME"

        echo "Updating Lambda configuration: $LAMBDA_FUNCTION_NAME" >&2

        aws lambda update-function-configuration \
            --region "$AWS_REGION" \
            --function-name "$LAMBDA_FUNCTION_NAME" \
            --role "$ROLE_ARN" \
            --timeout 30 \
            --environment "$ENV_JSON" \
            >/dev/null

        aws lambda wait function-updated \
            --region "$AWS_REGION" \
            --function-name "$LAMBDA_FUNCTION_NAME"

    else

        echo "Creating Lambda function: $LAMBDA_FUNCTION_NAME" >&2

        aws lambda create-function \
            --region "$AWS_REGION" \
            --function-name "$LAMBDA_FUNCTION_NAME" \
            --runtime provided.al2023 \
            --architectures arm64 \
            --handler bootstrap \
            --role "$ROLE_ARN" \
            --timeout 30 \
            --memory-size 128 \
            --zip-file "fileb://$LAMBDA_ZIP" \
            --environment "$ENV_JSON" \
            --tags \
                Project="$PROJECT_NAME",Environment="$ENVIRONMENT",ManagedBy="$MANAGED_BY" \
            >/dev/null

        aws lambda wait function-active \
            --region "$AWS_REGION" \
            --function-name "$LAMBDA_FUNCTION_NAME"

    fi

    FUNCTION_ARN=$(aws lambda get-function \
        --region "$AWS_REGION" \
        --function-name "$LAMBDA_FUNCTION_NAME" \
        --query "Configuration.FunctionArn" \
        --output text)

    append_output "DNS_SYNC_LAMBDA_ARN" "$FUNCTION_ARN"
    echo "$FUNCTION_ARN"
}

# --- EventBridge: trigger on ECS task state change --------------------------

wire_eventbridge_rule() {

    local FUNCTION_ARN="$1"
    local RULE_ARN

    echo "Creating/updating EventBridge rule: $EVENTBRIDGE_RULE_NAME" >&2

    RULE_ARN=$(aws events put-rule \
        --region "$AWS_REGION" \
        --name "$EVENTBRIDGE_RULE_NAME" \
        --description "Triggers dns-sync when a task for ${ECS_SERVICE_NAME} becomes RUNNING" \
        --event-pattern "{
            \"source\": [\"aws.ecs\"],
            \"detail-type\": [\"ECS Task State Change\"],
            \"detail\": {
                \"clusterArn\": [\"${ECS_CLUSTER_ARN}\"],
                \"lastStatus\": [\"RUNNING\"]
            }
        }" \
        --state ENABLED \
        --tags \
            Key=Project,Value="$PROJECT_NAME" \
            Key=Environment,Value="$ENVIRONMENT" \
            Key=ManagedBy,Value="$MANAGED_BY" \
        --query "RuleArn" \
        --output text)

    aws lambda add-permission \
        --region "$AWS_REGION" \
        --function-name "$LAMBDA_FUNCTION_NAME" \
        --statement-id "AllowEventBridgeInvoke" \
        --action "lambda:InvokeFunction" \
        --principal "events.amazonaws.com" \
        --source-arn "$RULE_ARN" \
        >/dev/null 2>&1 || echo "Lambda permission already granted." >&2

    aws events put-targets \
        --region "$AWS_REGION" \
        --rule "$EVENTBRIDGE_RULE_NAME" \
        --targets "Id=1,Arn=$FUNCTION_ARN" \
        >/dev/null

    append_output "DNS_SYNC_RULE_ARN" "$RULE_ARN"
}

SECRET_ARN=$(create_or_update_cloudflare_secret)
ROLE_ARN=$(create_lambda_role "$SECRET_ARN")
FUNCTION_ARN=$(deploy_lambda "$ROLE_ARN" "$SECRET_ARN")
wire_eventbridge_rule "$FUNCTION_ARN"

echo "DNS sync ready."
echo "Any RUNNING task for '${ECS_SERVICE_NAME}' will now update ${CLOUDFLARE_DNS_RECORD_NAME} automatically, independently of deploy.yml."