package bflow.storage.entity;

import bflow.auth.entities.User;
import bflow.storage.enums.FileStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks a file stored in the application's S3 bucket and the user
 * who owns it.
 *
 * <p>{@code objectKey} is expected to already be scoped under the
 * application's trusted key namespace ({@code users/{userId}/...}
 * or {@code tmp/{uuid}}), as enforced by
 * {@link bflow.common.aws.service.S3StorageService} before any
 * object reaches S3. This entity does not re-validate the key
 * shape; it only records it.</p>
 *
 * <p>Ownership must always be checked through
 * {@link bflow.storage.repository.RepositoryStoredFile
 * #findByIdAndUserId(UUID, UUID)} rather than a plain
 * {@code findById}, so a caller can never resolve another user's
 * file by guessing its identifier.</p>
 */
@Entity
@Table(name = "stored_files")
@Getter
@Setter
public class StoredFile {

    /**
     * Unique identifier of the stored file record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * The user who owns this file.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    /**
     * The S3 object key. Immutable once assigned.
     */
    @Column(name = "object_key", nullable = false, updatable = false)
    private String objectKey;

    /**
     * The original file name supplied by the client at upload time.
     */
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    /**
     * The MIME type of the file.
     */
    @Column(name = "content_type", nullable = false)
    private String contentType;

    /**
     * The size of the file in bytes.
     */
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    /**
     * The current lifecycle status of this file.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileStatus status;

    /**
     * The timestamp when this record was created.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * The timestamp when this record was last updated
     * (e.g. on status transitions).
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
