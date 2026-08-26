package bflow.receipts.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The payload Textract publishes to SNS when an async job finishes,
 * carried as the {@code Message} field of {@link SnsEnvelope}. Only
 * the fields this application reads are declared; {@code
 * DocumentLocation} and {@code JobTag} are ignored.
 *
 * @param jobId the Textract JobId this notification refers to
 * @param status the job's final status: {@code SUCCEEDED}, {@code
 *         FAILED} or {@code PARTIAL_SUCCESS}
 * @param api the Textract API that was used, expected to be {@code
 *         StartExpenseAnalysis} for every job this application
 *         submits
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TextractCompletionNotification(
        @JsonProperty("JobId") String jobId,
        @JsonProperty("Status") String status,
        @JsonProperty("API") String api
) {
}
