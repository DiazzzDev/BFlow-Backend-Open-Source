package bflow.receipts.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The outer SNS envelope delivered into SQS when a queue is
 * subscribed to a topic without raw message delivery enabled. Only
 * the fields this application reads are declared; everything else
 * (MessageId, TopicArn, Timestamp, signature fields...) is ignored.
 *
 * @param type the SNS message type, expected to always be {@code
 *         Notification} for an SQS-protocol subscription
 * @param message the inner payload, itself a JSON string — see
 *         {@link TextractCompletionNotification}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SnsEnvelope(
        @JsonProperty("Type") String type,
        @JsonProperty("Message") String message
) {

    /** The only SNS message type this listener expects to handle. */
    public static final String TYPE_NOTIFICATION = "Notification";
}
