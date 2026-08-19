package bflow.receipts.repository;

import bflow.receipts.entity.ReceiptUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for receipt upload records.
 */
@Repository
public interface RepositoryReceiptUpload
        extends JpaRepository<ReceiptUpload, UUID> {

    /**
     * Finds a receipt upload by its identifier and owner.
     *
     * @param id the receipt upload identifier
     * @param userId the identifier of the owning user
     * @return an optional containing the receipt upload if found
     */
    Optional<ReceiptUpload> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Checks whether a receipt upload exists for the given stored file.
     *
     * @param storedFileId the identifier of the stored file
     * @return true if a receipt upload exists for the stored file
     */
    boolean existsByStoredFileId(UUID storedFileId);
}
