#!/usr/bin/env bash
# 03-verify.sh — Sanity-checks everything the previous two scripts
# were supposed to create. Doesn't modify anything.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"

check() {
  local description="$1"
  local ok="$2"
  if [[ "${ok}" == "true" ]]; then
    log_ok "${description}"
  else
    log_error "${description}"
    FAILED=1
  fi
}

main() {
  preflight
  FAILED=0

  requests_queue_url="$(queue_url_if_exists "${OCR_REQUESTS_QUEUE_NAME}")"
  check "Requests queue '${OCR_REQUESTS_QUEUE_NAME}' exists" \
    "$([[ -n "${requests_queue_url}" ]] && echo true || echo false)"

  results_queue_url="$(queue_url_if_exists "${OCR_RESULTS_QUEUE_NAME}")"
  check "Results queue '${OCR_RESULTS_QUEUE_NAME}' exists" \
    "$([[ -n "${results_queue_url}" ]] && echo true || echo false)"

  requests_dlq_url="$(queue_url_if_exists "${OCR_REQUESTS_DLQ_NAME}")"
  check "Requests DLQ '${OCR_REQUESTS_DLQ_NAME}' exists" \
    "$([[ -n "${requests_dlq_url}" ]] && echo true || echo false)"

  results_dlq_url="$(queue_url_if_exists "${OCR_RESULTS_DLQ_NAME}")"
  check "Results DLQ '${OCR_RESULTS_DLQ_NAME}' exists" \
    "$([[ -n "${results_dlq_url}" ]] && echo true || echo false)"

  topic_arn="$(topic_arn_if_exists "${OCR_RESULTS_TOPIC_NAME}")"
  check "SNS topic '${OCR_RESULTS_TOPIC_NAME}' exists" \
    "$([[ -n "${topic_arn}" ]] && echo true || echo false)"

  if [[ -n "${results_queue_url}" && -n "${topic_arn}" ]]; then
    results_queue_arn="$(queue_arn_from_url "${results_queue_url}")"
    subscribed=$(aws sns list-subscriptions-by-topic \
      --topic-arn "${topic_arn}" --region "${AWS_REGION}" \
      --query "Subscriptions[?Endpoint=='${results_queue_arn}'] | length(@)" \
      --output text)
    check "Results queue is subscribed to the topic" \
      "$([[ "${subscribed}" -gt 0 ]] && echo true || echo false)"
  fi

  ecs_policy_present=$(aws iam get-role-policy \
    --role-name "${ECS_TASK_ROLE_NAME}" \
    --policy-name "${ECS_OCR_POLICY_NAME}" \
    --region "${AWS_REGION}" >/dev/null 2>&1 && echo true || echo false)
  check "ECS Task Role has policy '${ECS_OCR_POLICY_NAME}'" "${ecs_policy_present}"

  textract_role_present=$(aws iam get-role \
    --role-name "${TEXTRACT_SNS_ROLE_NAME}" >/dev/null 2>&1 && echo true || echo false)
  check "Textract SNS publisher role '${TEXTRACT_SNS_ROLE_NAME}' exists" "${textract_role_present}"

  echo
  if [[ "${FAILED}" -eq 0 ]]; then
    log_ok "All checks passed."
  else
    log_error "One or more checks failed — see above."
    exit 1
  fi
}

main "$@"
