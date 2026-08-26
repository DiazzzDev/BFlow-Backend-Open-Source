#!/usr/bin/env bash
# 02-configure-iam.sh — Two separate IAM concerns:
#
#   1. Attaches a least-privilege inline policy to the existing ECS
#      Task Role so the app can send/receive/delete on both OCR
#      queues.
#   2. Creates (or updates) a dedicated role that Textract itself
#      assumes to publish completion notifications to the results
#      SNS topic — this is NOT the ECS Task Role; Textract's
#      NotificationChannel needs a role it can assume directly, with
#      a trust policy naming textract.amazonaws.com, not your
#      application's identity.
#
# Idempotency: put-role-policy overwrites the named inline policy in
# place; create-role is skipped if the role already exists.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"

main() {
  preflight

  requests_queue_url="$(queue_url_if_exists "${OCR_REQUESTS_QUEUE_NAME}")"
  results_queue_url="$(queue_url_if_exists "${OCR_RESULTS_QUEUE_NAME}")"
  if [[ -z "${requests_queue_url}" || -z "${results_queue_url}" ]]; then
    log_error "One or both queues don't exist yet. Run 01-create-queues-and-topic.sh first."
    exit 1
  fi
  requests_queue_arn="$(queue_arn_from_url "${requests_queue_url}")"
  results_queue_arn="$(queue_arn_from_url "${results_queue_url}")"

  topic_arn="$(topic_arn_if_exists "${OCR_RESULTS_TOPIC_NAME}")"
  if [[ -z "${topic_arn}" ]]; then
    log_error "SNS topic '${OCR_RESULTS_TOPIC_NAME}' doesn't exist yet. Run 01-create-queues-and-topic.sh first."
    exit 1
  fi

  log_info "=== 1/2: ECS Task Role messaging policy ==="
  if ! aws iam get-role --role-name "${ECS_TASK_ROLE_NAME}" >/dev/null 2>&1; then
    log_error "Role '${ECS_TASK_ROLE_NAME}' does not exist."
    log_error "Create it in your ECS/infra stack first, or set ECS_TASK_ROLE_NAME."
    exit 1
  fi

  ecs_policy_document=$(cat <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "OcrRequestsQueueAccess",
      "Effect": "Allow",
      "Action": ["sqs:SendMessage", "sqs:ReceiveMessage", "sqs:DeleteMessage", "sqs:GetQueueAttributes"],
      "Resource": "${requests_queue_arn}"
    },
    {
      "Sid": "OcrResultsQueueAccess",
      "Effect": "Allow",
      "Action": ["sqs:ReceiveMessage", "sqs:DeleteMessage", "sqs:GetQueueAttributes"],
      "Resource": "${results_queue_arn}"
    },
    {
      "Sid": "TextractStartAndGetExpenseAnalysis",
      "Effect": "Allow",
      "Action": ["textract:StartExpenseAnalysis", "textract:GetExpenseAnalysis"],
      "Resource": "*"
    }
  ]
}
EOF
)

  aws iam put-role-policy \
    --role-name "${ECS_TASK_ROLE_NAME}" \
    --policy-name "${ECS_OCR_POLICY_NAME}" \
    --policy-document "${ecs_policy_document}"

  log_ok "Policy '${ECS_OCR_POLICY_NAME}' attached to role '${ECS_TASK_ROLE_NAME}'."
  log_ok "Note: Textract actions use Resource: \"*\" because the Textract API does not support resource-level permissions for these actions — this is the AWS-documented minimum, not an oversight."

  log_info "=== 2/2: Textract -> SNS publisher role ==="

  trust_policy=$(cat <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {"Service": "textract.amazonaws.com"},
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
)

  if aws iam get-role --role-name "${TEXTRACT_SNS_ROLE_NAME}" >/dev/null 2>&1; then
    log_ok "Role '${TEXTRACT_SNS_ROLE_NAME}' already exists."
  else
    log_info "Creating role '${TEXTRACT_SNS_ROLE_NAME}'..."
    aws iam create-role \
      --role-name "${TEXTRACT_SNS_ROLE_NAME}" \
      --assume-role-policy-document "${trust_policy}" >/dev/null
    log_ok "Role created."
  fi

  publish_policy=$(cat <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "sns:Publish",
      "Resource": "${topic_arn}"
    }
  ]
}
EOF
)

  aws iam put-role-policy \
    --role-name "${TEXTRACT_SNS_ROLE_NAME}" \
    --policy-name "publish-to-ocr-results-topic" \
    --policy-document "${publish_policy}"

  role_arn=$(aws iam get-role \
    --role-name "${TEXTRACT_SNS_ROLE_NAME}" \
    --query 'Role.Arn' --output text)

  log_ok "Role '${TEXTRACT_SNS_ROLE_NAME}' can publish to '${topic_arn}'."

  echo
  log_ok "Set this in your environment / ECS task definition:"
  echo "  TEXTRACT_SNS_ROLE_ARN=${role_arn}"
}

main "$@"
