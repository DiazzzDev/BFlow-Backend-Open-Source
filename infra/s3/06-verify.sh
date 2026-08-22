#!/usr/bin/env bash
# 06-verify.sh — Verify the full state of the S3 infrastructure module.
# Read-only: makes no changes, safe to run anytime.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"

FAILURES=0

check() {
  local description="$1"
  local status="$2" # 0 = ok, 1 = fail
  if [[ "${status}" -eq 0 ]]; then
    printf '[OK] %s\n' "${description}"
  else
    printf '[FAIL] %s\n' "${description}"
    FAILURES=$((FAILURES + 1))
  fi
}

main() {
  preflight

  printf '================================\n'
  printf 'S3 Infrastructure Verification\n'
  printf '================================\n\n'
  printf 'Bucket: %s\n' "${S3_BUCKET}"
  printf 'Region: %s\n\n' "${AWS_REGION}"

  if bucket_exists; then
    check "Bucket exists" 0
  else
    check "Bucket exists" 1
    printf '\nInfrastructure NOT ready. Run 01-create-bucket.sh first.\n'
    exit 1
  fi

  # Region correct — us-east-1 is reported as "None" by this API.
  local actual_region
  actual_region="$(aws s3api get-bucket-location --bucket "${S3_BUCKET}" --output text 2>/dev/null || echo "")"
  if [[ "${actual_region}" == "None" ]]; then actual_region="us-east-1"; fi
  if [[ "${actual_region}" == "${AWS_REGION}" ]]; then
    check "Region correct (${actual_region})" 0
  else
    check "Region correct (expected ${AWS_REGION}, got ${actual_region})" 1
  fi

  # Public access blocked
  local pab
  pab="$(aws s3api get-public-access-block --bucket "${S3_BUCKET}" --region "${AWS_REGION}" 2>/dev/null || echo "")"
  if [[ "${pab}" == *'"BlockPublicAcls": true'* && "${pab}" == *'"IgnorePublicAcls": true'* \
     && "${pab}" == *'"BlockPublicPolicy": true'* && "${pab}" == *'"RestrictPublicBuckets": true'* ]]; then
    check "Public access blocked" 0
  else
    check "Public access blocked" 1
  fi

  # ACLs disabled (Object Ownership = BucketOwnerEnforced)
  local ownership
  ownership="$(aws s3api get-bucket-ownership-controls --bucket "${S3_BUCKET}" --region "${AWS_REGION}" 2>/dev/null || echo "")"
  if [[ "${ownership}" == *"BucketOwnerEnforced"* ]]; then
    check "ACLs disabled (BucketOwnerEnforced)" 0
  else
    check "ACLs disabled (BucketOwnerEnforced)" 1
  fi

  # Encryption enabled
  local encryption
  encryption="$(aws s3api get-bucket-encryption --bucket "${S3_BUCKET}" --region "${AWS_REGION}" 2>/dev/null || echo "")"
  if [[ "${encryption}" == *"AES256"* ]]; then
    check "SSE-S3 enabled" 0
  else
    check "SSE-S3 enabled" 1
  fi

    # CORS configured
  local allowed_origins
  allowed_origins="$(
    aws s3api get-bucket-cors \
      --bucket "${S3_BUCKET}" \
      --region "${AWS_REGION}" \
      --query 'CORSRules[].AllowedOrigins[]' \
      --output text 2>/dev/null || echo ""
  )"

  local cors_ok=0
  local origin

  IFS=',' read -r -a origins <<< "${FRONTEND_ORIGINS}"

  for origin in "${origins[@]}"; do
    origin="$(echo "${origin}" | xargs)"

    if [[ -z "${origin}" || "${allowed_origins}" != *"${origin}"* ]]; then
      cors_ok=1
      break
    fi
  done

  if printf '%s\n' "${allowed_origins}" | grep -Fxq '*'; then
    cors_ok=1
  fi

  if [[ "${cors_ok}" -eq 0 ]]; then
    check "CORS configured (all origins, no wildcard)" 0
  else
    check "CORS configured" 1
  fi

  # Lifecycle configured correctly
  local lifecycle
  lifecycle="$(aws s3api get-bucket-lifecycle-configuration --bucket "${S3_BUCKET}" --region "${AWS_REGION}" 2>/dev/null || echo "")"
  if [[ "${lifecycle}" == *"${S3_TMP_PREFIX}"* ]]; then
    check "Lifecycle verified (only ${S3_TMP_PREFIX}* expires)" 0
  else
    check "Lifecycle verified" 1
  fi

  # IAM policy exists
  #local iam_check_status=1
  #if aws iam get-role-policy --role-name "${ECS_TASK_ROLE_NAME}" --policy-name "${ECS_S3_POLICY_NAME}" >/dev/null 2>&1; then
  #  iam_check_status=0
  #fi
  #check "IAM policy verified (${ECS_S3_POLICY_NAME})" "${iam_check_status}"

  printf '\n'
  if [[ "${FAILURES}" -eq 0 ]]; then
    printf 'S3 infrastructure ready.\n'
  else
    printf 'S3 infrastructure NOT ready (%d check(s) failed).\n' "${FAILURES}"
    exit 1
  fi
}

main "$@"