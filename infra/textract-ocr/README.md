# infra/textract-ocr

Provisions the messaging infrastructure for the async receipt OCR
pipeline: S3 upload confirmed → SQS request queue → Textract
`StartExpenseAnalysis` → SNS → SQS result queue.

Same conventions as `infra/s3`: plain bash + AWS CLI, no Terraform,
every script is idempotent and safe to re-run.

## Order

```bash
export ECS_TASK_ROLE_NAME=your-actual-ecs-task-role   # required
./01-create-queues-and-topic.sh
./02-configure-iam.sh
./03-verify.sh
```

Each script prints the environment variables the app needs at the
end. Copy them into your `.env` / ECS task definition:

```
RECEIPT_OCR_REQUESTS_QUEUE_URL=...
RECEIPT_OCR_RESULTS_QUEUE_URL=...
RECEIPT_OCR_RESULTS_TOPIC_ARN=...
TEXTRACT_SNS_ROLE_ARN=...
```

## What gets created

- Two SQS queues (`bflow-receipt-ocr-requests`,
  `bflow-receipt-ocr-results`), each with its own dead-letter queue.
  A message that fails processing `OCR_MAX_RECEIVE_COUNT` times
  (default 5) moves to its DLQ instead of retrying forever.
- One SNS topic (`bflow-receipt-ocr-results`), which Textract
  publishes to when a job finishes. The results queue is subscribed
  to it.
- An inline policy on your existing ECS Task Role granting
  send/receive/delete on both queues and `textract:StartExpenseAnalysis`
  / `textract:GetExpenseAnalysis`.
- A **separate** IAM role (`bflow-textract-sns-publisher`) that
  Textract itself assumes to publish to the results topic. This is
  not your app's identity — Textract's `NotificationChannel` needs a
  role with a trust policy naming `textract.amazonaws.com`.

## Not covered here

- The S3 bucket and its CORS/lifecycle config — see `infra/s3`.
- The `textract_job_id` migration and the Java pipeline itself — see
  `src/main/resources/db/migration/V20__*.sql` and
  `bflow.receipts.messaging` / `bflow.receipts.service`.
