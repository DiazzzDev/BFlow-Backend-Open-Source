package bflow.notifications.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for notification response.
 */
@Getter
@Setter
public class NotificationResponse {
    /**
     * The notification ID.
     */
    private UUID id;
    /**
     * The notification title.
     */
    private String title;
    /**
     * The notification message.
     */
    private String message;
    /**
     * The notification type.
     */
    private String type;
    /**
     * Whether the notification has been read.
     */
    private Boolean read;
    /**
     * The creation timestamp.
     */
    private Instant createdAt;
}
