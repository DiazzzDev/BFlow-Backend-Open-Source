#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

source "$SCRIPT_DIR/../config.env"
source "$SCRIPT_DIR/../outputs.env"
source "$SCRIPT_DIR/../lib/helpers.sh"

LAMBDA_ROLE_NAME="${PROJECT_NAME}-dns-sync-lambda-role"
LAMBDA_FUNCTION_NAME="${PROJECT_NAME}-dns-sync"
EVENTBRIDGE_RULE_NAME="${PROJECT_NAME}-ecs-task-state-change"
CLOUDFLARE_SECRET_NAME="${PROJECT_NAME}/cloudflare-dns"

echo "Removing EventBridge rule..."

RULE_EXISTS=$(aws events describe-rule \
    --region "$AWS_REGION" \
    --name "$EVENTBRIDGE_RULE_NAME" \
    --query "Name" \
    --output text 2>/dev/null || true)

if [[ -n "$RULE_EXISTS" && "$RULE_EXISTS" != "None" ]]; then

    TARGET_IDS=$(aws events list-targets-by-rule \
        --region "$AWS_REGION" \
        --rule "$EVENTBRIDGE_RULE_NAME" \
        --query "Targets[].Id" \
        --output text)

    if [[ -n "$TARGET_IDS" ]]; then
        aws events remove-targets \
            --region "$AWS_REGION" \
            --rule "$EVENTBRIDGE_RULE_NAME" \
            --ids $TARGET_IDS \
            >/dev/null
    fi

    aws events delete-rule \
        --region "$AWS_REGION" \
        --name "$EVENTBRIDGE_RULE_NAME"

    echo "EventBridge rule deleted."

else
    echo "EventBridge rule already deleted."
fi

echo "Removing Lambda function..."

if aws lambda get-function \
    --region "$AWS_REGION" \
    --function-name "$LAMBDA_FUNCTION_NAME" >/dev/null 2>&1; then

    aws lambda delete-function \
        --region "$AWS_REGION" \
        --function-name "$LAMBDA_FUNCTION_NAME"

    echo "Lambda function deleted."

else
    echo "Lambda function already deleted."
fi

echo "Removing Lambda IAM role..."

ROLE_EXISTS=$(aws iam get-role \
    --role-name "$LAMBDA_ROLE_NAME" \
    --query "Role.RoleName" \
    --output text 2>/dev/null || true)

if [[ -n "$ROLE_EXISTS" && "$ROLE_EXISTS" != "None" ]]; then

    INLINE_POLICIES=$(aws iam list-role-policies \
        --role-name "$LAMBDA_ROLE_NAME" \
        --query "PolicyNames[]" \
        --output text)

    if [[ -n "$INLINE_POLICIES" ]]; then
        while read -r POLICY_NAME; do
            [[ -n "$POLICY_NAME" ]] && aws iam delete-role-policy \
                --role-name "$LAMBDA_ROLE_NAME" \
                --policy-name "$POLICY_NAME"
        done <<< "$INLINE_POLICIES"
    fi

    MANAGED_POLICIES=$(aws iam list-attached-role-policies \
        --role-name "$LAMBDA_ROLE_NAME" \
        --query "AttachedPolicies[].PolicyArn" \
        --output text)

    if [[ -n "$MANAGED_POLICIES" ]]; then
        while read -r POLICY_ARN; do
            [[ -n "$POLICY_ARN" ]] && aws iam detach-role-policy \
                --role-name "$LAMBDA_ROLE_NAME" \
                --policy-arn "$POLICY_ARN"
        done <<< "$MANAGED_POLICIES"
    fi

    aws iam delete-role --role-name "$LAMBDA_ROLE_NAME"

    echo "Lambda role deleted."

else
    echo "Lambda role already deleted."
fi

echo "Removing Cloudflare secret..."

SECRET_EXISTS=$(aws secretsmanager describe-secret \
    --region "$AWS_REGION" \
    --secret-id "$CLOUDFLARE_SECRET_NAME" \
    --query "Name" \
    --output text 2>/dev/null || true)

if [[ -n "$SECRET_EXISTS" && "$SECRET_EXISTS" != "None" ]]; then

    aws secretsmanager delete-secret \
        --region "$AWS_REGION" \
        --secret-id "$CLOUDFLARE_SECRET_NAME" \
        --force-delete-without-recovery

    echo "Cloudflare secret deleted."

else
    echo "Cloudflare secret already deleted."
fi

rm -rf "$SCRIPT_DIR/../dns-sync/.build"

echo "DNS sync teardown complete."