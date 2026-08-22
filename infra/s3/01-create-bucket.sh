#!/usr/bin/env bash
# 01-create-bucket.sh — Create a private S3 bucket.
#
# Idempotency: checks for the bucket first and exits early if it already
# exists, so re-running this script is always safe.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"

main() {
  preflight

  if bucket_exists; then
    log_ok "Bucket '${S3_BUCKET}' already exists. Nothing to do (idempotent)."
    return 0
  fi

  log_info "Creating bucket '${S3_BUCKET}' in '${AWS_REGION}'..."

  # us-east-1 is a special case: create-bucket fails if you pass
  # --create-bucket-configuration LocationConstraint=us-east-1.
  # Every other region requires LocationConstraint to be set explicitly.
  if [[ "${AWS_REGION}" == "us-east-1" ]]; then
    aws s3api create-bucket \
      --bucket "${S3_BUCKET}" \
      --region "${AWS_REGION}"
  else
    aws s3api create-bucket \
      --bucket "${S3_BUCKET}" \
      --region "${AWS_REGION}" \
      --create-bucket-configuration LocationConstraint="${AWS_REGION}"
  fi

  # No objects are uploaded and no public permissions are configured here.
  # That happens in 02-configure-security.sh.

  log_ok "Bucket '${S3_BUCKET}' created."
}

main "$@"