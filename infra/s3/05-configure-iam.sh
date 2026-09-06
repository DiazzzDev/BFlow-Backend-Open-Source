#!/usr/bin/env bash
# 05-configure-iam.sh — Attach a least-privilege inline S3 policy to the
# ECS Task Role. No access keys are created — ECS authenticates to S3
# through the Task Role via the AWS SDK's default credential chain:
#
#   ECS Task Role -> AWS SDK -> S3
#
# Idempotency: put-role-policy overwrites the named inline policy in
# place, so re-running this script with the same inputs always produces
# the same policy document.
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"

main() {
  preflight
  require_bucket_exists

  log_info "Checking that ECS Task Role '${ECS_TASK_ROLE_NAME}' exists..."
  if ! aws iam get-role --role-name "${ECS_TASK_ROLE_NAME}" >/dev/null 2>&1; then
    log_error "Role '${ECS_TASK_ROLE_NAME}' does not exist."
    log_error "This script only attaches an S3 policy to an existing Task Role —"
    log_error "create the role in your ECS/infra stack first, or set"
    log_error "ECS_TASK_ROLE_NAME to the correct role name in config.sh."
    exit 1
  fi

  log_info "Attaching least-privilege inline policy '${ECS_S3_POLICY_NAME}'..."

  local policy_document
  policy_document=$(cat <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "S3ObjectAccess",
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject"
      ],
      "Resource": "arn:aws:s3:::${S3_BUCKET}/${S3_OBJECT_PREFIX}"
    },
    {
      "Sid": "S3ListBucket",
      "Effect": "Allow",
      "Action": "s3:ListBucket",
      "Resource": "arn:aws:s3:::${S3_BUCKET}",
      "Condition": {
        "StringLike": {
          "s3:prefix": ["${S3_OBJECT_PREFIX}"]
        }
      }
    }
  ]
}
EOF
)

  # put-role-policy is idempotent by design: it overwrites the inline
  # policy with this exact name if it already exists.
  aws iam put-role-policy \
    --role-name "${ECS_TASK_ROLE_NAME}" \
    --policy-name "${ECS_S3_POLICY_NAME}" \
    --policy-document "${policy_document}"

  log_ok "Policy '${ECS_S3_POLICY_NAME}' attached to role '${ECS_TASK_ROLE_NAME}'."
  log_ok "No s3:*, no Resource: \"*\", no static access keys."
}

main "$@"