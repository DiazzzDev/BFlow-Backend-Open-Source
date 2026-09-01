package bflow.notifications;

import bflow.auth.services.CurrentUserService;
import bflow.common.response.ApiResponse;
import bflow.notifications.DTO.NotificationResponse;
import bflow.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for managing notifications.
 */
@Tag(name = "Notifications", description = "Management of user notifications")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public final class ControllerNotification {

    /**
     * The notification service.
     */
    private final NotificationService service;

    /** Service used to resolve the authenticated user. */
    private final CurrentUserService currentUserService;

    /**
     * Get all notifications for the authenticated user.
     *
     * @param authentication the authentication object
     * @return response containing list of notifications
     */
    @Operation(
            summary = "Get all notifications for the authenticated user.",
            description = "Get all notifications for the authenticated user."
    )
    @GetMapping
    public ApiResponse<List<NotificationResponse>> getAll(
            final Authentication authentication
    ) {

        UUID userId = currentUserService.getCurrentUserId(authentication);

        return ApiResponse.success(
                        "Notifications retrieved",
                        service.getUserNotifications(userId),
                        "/api/v1/notifications"
        );
    }

    /**
     * Get the count of unread notifications.
     *
     * @param authentication the authentication object
     * @return response containing unread count
     */
    @Operation(
            summary = "Get the count of unread notifications.",
            description = "Get the count of unread notifications."
    )
    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(
            final Authentication authentication
    ) {

        UUID userId = currentUserService.getCurrentUserId(authentication);

        return ApiResponse.success(
                        "Unread count retrieved",
                        service.getUnreadCount(userId),
                        "/api/v1/notifications/unread-count"
        );
    }

    /**
     * Mark a notification as read.
     *
     * @param id the notification ID
     * @param authentication the authentication object
     * @return response
     */
    @Operation(
            summary = "Mark a notification as read.",
            description = "Mark a notification as read."
    )
    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable final UUID id,
            final Authentication authentication
    ) {

        UUID userId = currentUserService.getCurrentUserId(authentication);

        service.markAsRead(id, userId);

        return ApiResponse.success(
                        "Notification marked as read",
                        null,
                        "/api/v1/notifications/" + id + "/read"
        );
    }
}
