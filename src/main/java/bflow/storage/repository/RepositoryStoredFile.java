package bflow.storage.repository;

import bflow.storage.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for stored file records.
 */
@Repository
public interface RepositoryStoredFile
        extends JpaRepository<StoredFile, UUID> {

    /**
     * Finds a file by id, scoped to its owner. Callers must use this
     * method instead of {@link #findById(Object)} whenever the file
     * is being resolved on behalf of an authenticated user, so that
     * a user can never access another user's file by id.
     *
     * @param id the stored file identifier
     * @param userId the identifier of the requesting user
     * @return an optional containing the file if it exists and is
     *         owned by the given user
     */
    Optional<StoredFile> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Finds a file by its S3 object key.
     *
     * @param objectKey the S3 object key
     * @return an optional containing the file if found
     */
    Optional<StoredFile> findByObjectKey(String objectKey);

    /**
     * Checks whether a file record already exists for the given
     * object key.
     *
     * @param objectKey the S3 object key
     * @return true if a record exists for that key
     */
    boolean existsByObjectKey(String objectKey);
}
