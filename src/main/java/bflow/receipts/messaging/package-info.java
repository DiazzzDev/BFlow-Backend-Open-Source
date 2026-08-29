/**
 * Async messaging plumbing for the receipt OCR pipeline: the SQS
 * request/result listeners, the SNS/Textract notification DTOs, and
 * the domain-event bridge that enqueues a request once a receipt's
 * registration commits.
 */
package bflow.receipts.messaging;
