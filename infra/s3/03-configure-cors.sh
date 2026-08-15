#!/usr/bin/env bash
# 03-configure-cors.sh — Restrict CORS to known frontend origins.
#
# Idempotency: put-bucket-cors always replaces the full CORS configuration.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"

main() {
  preflight
  require_bucket_exists

  if [[ -z "${FRONTEND_ORIGINS:-}" ]]; then
    log_error "FRONTEND_ORIGINS is not set."
    log_error "Set it in config.sh or export FRONTEND_ORIGINS before running this script."
    exit 1
  fi

  log_info "Configuring CORS for origins: ${FRONTEND_ORIGINS}"

  local allowed_origins=""
  local origin

  IFS=',' read -r -a origins <<< "${FRONTEND_ORIGINS}"

  for origin in "${origins[@]}"; do
    origin="$(echo "${origin}" | xargs)"

    if [[ -z "${origin}" ]]; then
      continue
    fi

    if [[ -n "${allowed_origins}" ]]; then
      allowed_origins+=","
    fi

    allowed_origins+="\"${origin}\""
  done

  local cors_config
  cors_config=$(cat <<EOF
{
  "CORSRules": [
    {
      "AllowedOrigins": [${allowed_origins}],
      "AllowedMethods": ["GET", "PUT", "HEAD"],
      "AllowedHeaders": ["*"],
      "ExposeHeaders": ["ETag"],
      "MaxAgeSeconds": 3000
    }
  ]
}
EOF
)

  aws s3api put-bucket-cors \
    --bucket "${S3_BUCKET}" \
    --region "${AWS_REGION}" \
    --cors-configuration "${cors_config}"

  log_ok "CORS configured."
  log_ok "Allowed origins: ${FRONTEND_ORIGINS}"
}

main "$@"