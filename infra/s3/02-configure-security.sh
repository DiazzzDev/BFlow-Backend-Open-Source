#!/usr/bin/env bash
# 02-configure-security.sh — Public Access Block, Object Ownership,
# SSE-S3 encryption, and versioning.
#
# Idempotency: every call here is a declarative "put" against bucket
# config — AWS accepts repeated identical puts without error, so
# re-running this script always converges to the same state.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"

configure_public_access_block() {
  log_info "Configuring Public Access Block (all settings true)..."
  aws s3api put-public-access-block \
    --bucket "${S3_BUCKET}" \
    --region "${AWS_REGION}" \
    --public-access-block-configuration \
      BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
  log_ok "Public Access Block configured."
}

configure_object_ownership() {
  log_info "Configuring Object Ownership -> BucketOwnerEnforced (ACLs disabled)..."
  aws s3api put-bucket-ownership-controls \
    --bucket "${S3_BUCKET}" \
    --region "${AWS_REGION}" \
    --ownership-controls Rules='[{ObjectOwnership=BucketOwnerEnforced}]'
  log_ok "Object Ownership configured."
}

configure_encryption() {
  log_info "Configuring SSE-S3 encryption (AES256)..."
  aws s3api put-bucket-encryption \
    --bucket "${S3_BUCKET}" \
    --region "${AWS_REGION}" \
    --server-side-encryption-configuration \
      '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'
  log_ok "SSE-S3 encryption configured."
}

configure_versioning() {
  log_info "Setting versioning to Suspended (kept simple for now)..."
  aws s3api put-bucket-versioning \
    --bucket "${S3_BUCKET}" \
    --region "${AWS_REGION}" \
    --versioning-configuration Status=Suspended
  log_ok "Versioning set to Suspended."
}

main() {
  preflight
  require_bucket_exists

  configure_public_access_block
  configure_object_ownership
  configure_encryption
  configure_versioning

  log_ok "Security configuration complete for '${S3_BUCKET}'."
}

main "$@"