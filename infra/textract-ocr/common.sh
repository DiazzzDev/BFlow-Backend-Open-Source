#!/usr/bin/env bash
# common.sh — Shared logging and preflight checks. Sourced by every
# script in this module; never executed directly.

SCRIPT_DIR_COMMON="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./config.sh
source "${SCRIPT_DIR_COMMON}/config.sh"

log_info()  { printf '[INFO] %s\n' "$*" >&2; }
log_ok()    { printf '[OK] %s\n' "$*" >&2; }
log_warn()  { printf '[WARN] %s\n' "$*" >&2; }
log_error() { printf '[ERROR] %s\n' "$*" >&2; }

require_aws_cli() {
  if ! command -v aws >/dev/null 2>&1; then
    log_error "AWS CLI is not installed. Install it before running this script."
    exit 1
  fi
}

require_aws_identity() {
  local caller_identity
  if ! caller_identity="$(aws sts get-caller-identity --region "${AWS_REGION}" 2>&1)"; then
    log_error "Could not verify AWS identity. Check your credentials."
    log_error "${caller_identity}"
    exit 1
  fi

  ACCOUNT_ID="$(printf '%s' "${caller_identity}" | grep -o '"Account": *"[0-9]*"' | grep -o '[0-9]*')"
  export ACCOUNT_ID

  log_info "AWS Account: ${ACCOUNT_ID:-unknown}"
  log_info "AWS Region:  ${AWS_REGION}"

  if [[ -n "${AWS_EXPECTED_ACCOUNT_ID}" && "${ACCOUNT_ID}" != "${AWS_EXPECTED_ACCOUNT_ID}" ]]; then
    log_error "Active AWS account (${ACCOUNT_ID}) does not match AWS_EXPECTED_ACCOUNT_ID (${AWS_EXPECTED_ACCOUNT_ID})."
    log_error "Aborting to avoid running against the wrong account."
    exit 1
  fi
}

# Run all preflight checks. Call this at the top of every script's main().
preflight() {
  require_aws_cli
  require_aws_identity
}

queue_url_if_exists() {
  local queue_name="$1"
  aws sqs get-queue-url \
    --queue-name "${queue_name}" \
    --region "${AWS_REGION}" \
    --query 'QueueUrl' --output text 2>/dev/null || true
}

queue_arn_from_url() {
  local queue_url="$1"
  aws sqs get-queue-attributes \
    --queue-url "${queue_url}" \
    --attribute-names QueueArn \
    --region "${AWS_REGION}" \
    --query 'Attributes.QueueArn' --output text
}

topic_arn_if_exists() {
  local topic_name="$1"
  aws sns list-topics --region "${AWS_REGION}" \
    --query "Topics[?ends_with(TopicArn, ':${topic_name}')].TopicArn | [0]" \
    --output text 2>/dev/null | grep -v '^None$' || true
}
