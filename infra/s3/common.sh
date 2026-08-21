#!/usr/bin/env bash
# common.sh — Shared logging and preflight checks. Sourced by every
# script in this module; never executed directly.

SCRIPT_DIR_COMMON="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./config.sh
source "${SCRIPT_DIR_COMMON}/config.sh"

log_info()  { printf '[INFO] %s\n' "$*"; }
log_ok()    { printf '[OK] %s\n' "$*"; }
log_warn()  { printf '[WARN] %s\n' "$*" >&2; }
log_error() { printf '[ERROR] %s\n' "$*" >&2; }

require_aws_cli() {
  if ! command -v aws >/dev/null 2>&1; then
    log_error "AWS CLI is not installed. Install it before running this script."
    exit 1
  fi
}

require_vars() {
  local missing=0

  if [[ -z "${AWS_REGION:-}" ]]; then
    log_error "AWS_REGION is not set."
    missing=1
  fi

  if [[ -z "${S3_BUCKET:-}" || "${S3_BUCKET}" == "CHANGE_ME-files-prod" ]]; then
    log_error "S3_BUCKET is not set (or still has the placeholder value). Set it in config.sh or as an env var."
    missing=1
  fi

  if [[ "${missing}" -eq 1 ]]; then
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

  local account_id
  account_id="$(printf '%s' "${caller_identity}" | grep -o '"Account": *"[0-9]*"' | grep -o '[0-9]*')"

  log_info "AWS Account: ${account_id:-unknown}"
  log_info "AWS Region:  ${AWS_REGION}"
  log_info "S3 Bucket:   ${S3_BUCKET}"

  if [[ -n "${AWS_EXPECTED_ACCOUNT_ID}" && "${account_id}" != "${AWS_EXPECTED_ACCOUNT_ID}" ]]; then
    log_error "Active AWS account (${account_id}) does not match AWS_EXPECTED_ACCOUNT_ID (${AWS_EXPECTED_ACCOUNT_ID})."
    log_error "Aborting to avoid running against the wrong account."
    exit 1
  fi
}

# Run all preflight checks. Call this at the top of every script's main().
preflight() {
  require_aws_cli
  require_vars
  require_aws_identity
}

bucket_exists() {
  aws s3api head-bucket --bucket "${S3_BUCKET}" --region "${AWS_REGION}" >/dev/null 2>&1
}

require_bucket_exists() {
  if ! bucket_exists; then
    log_error "Bucket '${S3_BUCKET}' does not exist. Run 01-create-bucket.sh first."
    exit 1
  fi
}