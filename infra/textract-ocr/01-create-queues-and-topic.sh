#!/usr/bin/env bash
# 01-create-queues-and-topic.sh — Creates the two SQS queues (each
# with its own DLQ) and the SNS topic Textract publishes completion
# notifications to, then subscribes the results queue to that topic.
#
# Idempotency: every resource is looked up by name/ARN first and
# creation is skipped if it already exists, so re-running this
# script is always safe.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"

create_queue_with_dlq() {
  local queue_name="$1"
  local dlq_name="$2"

  local dlq_url dlq_arn
  dlq_url="$(queue_url_if_exists "${dlq_name}")"
  if [[ -z "${dlq_url}" ]]; then
    log_info "Creating DLQ '${dlq_name}'..."
    dlq_url=$(aws sqs create-queue \
      --queue-name "${dlq_name}" \
      --region "${AWS_REGION}" \
      --query 'QueueUrl' --output text)
  else
    log_ok "DLQ '${dlq_name}' already exists."
  fi
  dlq_arn="$(queue_arn_from_url "${dlq_url}")"

  local queue_url
  queue_url="$(queue_url_if_exists "${queue_name}")"
  if [[ -n "${queue_url}" ]]; then
    log_ok "Queue '${queue_name}' already exists. Nothing to do (idempotent)."
    echo "${queue_url}"
    return 0
  fi

  log_info "Creating queue '${queue_name}' (visibility timeout ${OCR_VISIBILITY_TIMEOUT_SECONDS}s, redrive after ${OCR_MAX_RECEIVE_COUNT} receives)..."

  local redrive_policy
  redrive_policy=$(cat <<EOF
{"deadLetterTargetArn":"${dlq_arn}","maxReceiveCount":${OCR_MAX_RECEIVE_COUNT}}
EOF
)

  queue_url=$(aws sqs create-queue \
    --queue-name "${queue_name}" \
    --region "${AWS_REGION}" \
    --attributes "{\"VisibilityTimeout\":\"${OCR_VISIBILITY_TIMEOUT_SECONDS}\",\"RedrivePolicy\":\"$(printf '%s' "${redrive_policy}" | sed 's/"/\\"/g')\"}" \
    --query 'QueueUrl' --output text)

  log_ok "Queue '${queue_name}' created: ${queue_url}"
  echo "${queue_url}"
}

main() {
  preflight

  log_info "=== Request queue (S3 upload confirmed -> start Textract) ==="
  requests_queue_url="$(create_queue_with_dlq "${OCR_REQUESTS_QUEUE_NAME}" "${OCR_REQUESTS_DLQ_NAME}")"

  log_info "=== Results queue (Textract job completed, via SNS) ==="
  results_queue_url="$(create_queue_with_dlq "${OCR_RESULTS_QUEUE_NAME}" "${OCR_RESULTS_DLQ_NAME}")"
  results_queue_arn="$(queue_arn_from_url "${results_queue_url}")"

  log_info "=== SNS topic (Textract's NotificationChannel target) ==="
  topic_arn="$(topic_arn_if_exists "${OCR_RESULTS_TOPIC_NAME}")"
  if [[ -z "${topic_arn}" ]]; then
    log_info "Creating topic '${OCR_RESULTS_TOPIC_NAME}'..."
    topic_arn=$(aws sns create-topic \
      --name "${OCR_RESULTS_TOPIC_NAME}" \
      --region "${AWS_REGION}" \
      --query 'TopicArn' --output text)
    log_ok "Topic created: ${topic_arn}"
  else
    log_ok "Topic '${OCR_RESULTS_TOPIC_NAME}' already exists: ${topic_arn}"
  fi

  log_info "Allowing topic '${OCR_RESULTS_TOPIC_NAME}' to send to results queue..."
  local_queue_policy=$(cat <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowSnsTopicToSendMessage",
      "Effect": "Allow",
      "Principal": {"Service": "sns.amazonaws.com"},
      "Action": "sqs:SendMessage",
      "Resource": "${results_queue_arn}",
      "Condition": {"ArnEquals": {"aws:SourceArn": "${topic_arn}"}}
    }
  ]
}
EOF
)
  aws sqs set-queue-attributes \
    --queue-url "${results_queue_url}" \
    --region "${AWS_REGION}" \
    --attributes "{\"Policy\":\"$(printf '%s' "${local_queue_policy}" | sed 's/"/\\"/g' | tr -d '\n')\"}"

  log_info "Subscribing results queue to the topic (if not already subscribed)..."
  existing_subscription=$(aws sns list-subscriptions-by-topic \
    --topic-arn "${topic_arn}" \
    --region "${AWS_REGION}" \
    --query "Subscriptions[?Endpoint=='${results_queue_arn}'].SubscriptionArn | [0]" \
    --output text)

  if [[ -n "${existing_subscription}" && "${existing_subscription}" != "None" ]]; then
    log_ok "Already subscribed: ${existing_subscription}"
  else
    aws sns subscribe \
      --topic-arn "${topic_arn}" \
      --protocol sqs \
      --notification-endpoint "${results_queue_arn}" \
      --region "${AWS_REGION}" >/dev/null
    log_ok "Results queue subscribed to '${OCR_RESULTS_TOPIC_NAME}'."
  fi

  echo
  log_ok "Set these in your environment / ECS task definition:"
  echo "  RECEIPT_OCR_REQUESTS_QUEUE_URL=${requests_queue_url}"
  echo "  RECEIPT_OCR_RESULTS_QUEUE_URL=${results_queue_url}"
  echo "  RECEIPT_OCR_RESULTS_TOPIC_ARN=${topic_arn}"
}

main "$@"
