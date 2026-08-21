#!/usr/bin/env bash
# 04-configure-lifecycle.sh — Conservative lifecycle rules.
#
# Only the configured temp prefix (S3_TMP_PREFIX) expires automatically.
# Permanent content (invoices, documents, attachments, profile photos,
# etc.) is left untouched — no rule ever targets it, so nothing there can
# be deleted by this script, accidentally or otherwise.
#
# Idempotency: put-bucket-lifecycle-configuration replaces the entire
# lifecycle policy with the payload below, so re-running converges to the
# same state.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"

main() {
  preflight
  require_bucket_exists

  log_info "Configuring lifecycle: only '${S3_TMP_PREFIX}*' expires, after ${S3_TMP_EXPIRATION_DAYS} day(s)."

  local lifecycle_config
  lifecycle_config=$(cat <<EOF
{
  "Rules": [
    {
      "ID": "expire-tmp-objects",
      "Filter": { "Prefix": "${S3_TMP_PREFIX}" },
      "Status": "Enabled",
      "Expiration": { "Days": ${S3_TMP_EXPIRATION_DAYS} }
    }
  ]
}
EOF
)

  aws s3api put-bucket-lifecycle-configuration \
    --bucket "${S3_BUCKET}" \
    --region "${AWS_REGION}" \
    --lifecycle-configuration "${lifecycle_config}"

  log_ok "Lifecycle configured. Only '${S3_TMP_PREFIX}*' has an expiration rule."
}

main "$@"