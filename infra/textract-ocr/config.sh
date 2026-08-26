#!/usr/bin/env bash
# config.sh — Centralized configuration for infra/textract-ocr/*.sh
#
# Provisions the SQS queues, SNS topic, and IAM roles/policies the
# async receipt OCR pipeline needs: S3 upload -> SQS request queue ->
# Textract StartExpenseAnalysis -> SNS -> SQS result queue.
#
# Sourced by every script in this module; never executed directly.
# All values can be overridden via environment variables.

# --- Core AWS settings -------------------------------------------------
export AWS_REGION="${AWS_REGION:-us-east-1}"
export AWS_EXPECTED_ACCOUNT_ID="${AWS_EXPECTED_ACCOUNT_ID:-}"

# --- Queues and topic ----------------------------------------------------
export OCR_REQUESTS_QUEUE_NAME="${OCR_REQUESTS_QUEUE_NAME:-bflow-receipt-ocr-requests}"
export OCR_REQUESTS_DLQ_NAME="${OCR_REQUESTS_DLQ_NAME:-bflow-receipt-ocr-requests-dlq}"
export OCR_RESULTS_QUEUE_NAME="${OCR_RESULTS_QUEUE_NAME:-bflow-receipt-ocr-results}"
export OCR_RESULTS_DLQ_NAME="${OCR_RESULTS_DLQ_NAME:-bflow-receipt-ocr-results-dlq}"
export OCR_RESULTS_TOPIC_NAME="${OCR_RESULTS_TOPIC_NAME:-bflow-receipt-ocr-results}"

# Times a message may be redelivered before it moves to its DLQ.
export OCR_MAX_RECEIVE_COUNT="${OCR_MAX_RECEIVE_COUNT:-5}"

# Must be >= the app's app.ocr.*-poll-wait-seconds plus processing
# time, or a message can become visible again mid-processing.
export OCR_VISIBILITY_TIMEOUT_SECONDS="${OCR_VISIBILITY_TIMEOUT_SECONDS:-120}"

# --- IAM / ECS ------------------------------------------------------------
# The ECS Task Role must already exist (created by your ECS/infra
# stack). This module attaches a policy letting it send/receive on
# both queues, and creates a separate role Textract itself assumes
# to publish completion notifications to SNS.
export ECS_TASK_ROLE_NAME="${ECS_TASK_ROLE_NAME:-CHANGE_ME-ecs-task-role}"
export ECS_OCR_POLICY_NAME="${ECS_OCR_POLICY_NAME:-receipt-ocr-messaging-policy}"
export TEXTRACT_SNS_ROLE_NAME="${TEXTRACT_SNS_ROLE_NAME:-bflow-textract-sns-publisher}"
