#!/usr/bin/env bash
# config.sh — Centralized configuration for infra/s3/*.sh
#
# This file is sourced by every script in this module; it is never
# executed directly. All values can be overridden via environment
# variables, so this module can be dropped into any project unchanged:
#
#   S3_BUCKET=myapp-files-prod FRONTEND_ORIGIN=https://myapp.com ./01-create-bucket.sh
#
# If your project already has a global .env / config system, source that
# instead of hardcoding values here, or export the variables before
# calling these scripts.

# --- Core AWS settings -------------------------------------------------
export AWS_REGION="${AWS_REGION:-us-east-1}"
export S3_BUCKET="${S3_BUCKET:-bflow-files-prod}"

# Optional safety net: if set, scripts abort when the active AWS account
# does not match this ID. Leave empty to skip the check.
export AWS_EXPECTED_ACCOUNT_ID="${AWS_EXPECTED_ACCOUNT_ID:-}"

# --- CORS ---------------------------------------------------------------
export FRONTEND_ORIGINS="${FRONTEND_ORIGINS:-https://bflow-studio.com,https://www.bflow-studio.com,http://localhost:5173}"
export FRONTEND_DEV_ORIGIN="${FRONTEND_DEV_ORIGIN:-http://localhost:5173}"

# --- IAM / ECS ------------------------------------------------------------
# The ECS Task Role must already exist (created by your ECS/infra stack).
# This module only attaches a least-privilege S3 policy to it.
export ECS_TASK_ROLE_NAME="${ECS_TASK_ROLE_NAME:-CHANGE_ME-ecs-task-role}"
export ECS_S3_POLICY_NAME="${ECS_S3_POLICY_NAME:-s3-access-policy}"

# Prefix under which the Task Role may read/write/delete objects.
# Use "*" to grant access to the whole bucket.
export S3_OBJECT_PREFIX="${S3_OBJECT_PREFIX:-users/*}"

# --- Lifecycle ------------------------------------------------------------
# Prefix that is safe to auto-expire (temp uploads, scratch files, etc).
export S3_TMP_PREFIX="${S3_TMP_PREFIX:-tmp/}"
export S3_TMP_EXPIRATION_DAYS="${S3_TMP_EXPIRATION_DAYS:-1}"