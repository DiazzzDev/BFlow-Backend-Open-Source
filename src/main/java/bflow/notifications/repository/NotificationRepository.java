package bflow.notifications.repository;

import bflow.notifications.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Notification entities.
 */
@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {
    /**
     * Find all notifications for a user, ordered by creation date.
     *
     * @param userId the user ID
     * @return list of notifications
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Count unread notifications for a user.
     *
     * @param userId the user ID
     * @return count of unread notifications
     */
    Long countByUserIdAndReadFalse(UUID userId);
}
