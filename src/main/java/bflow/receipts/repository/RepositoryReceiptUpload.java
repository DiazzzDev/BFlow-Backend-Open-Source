package bflow.receipts.repository;

import bflow.receipts.entity.ReceiptUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositoryReceiptUpload
        extends JpaRepository<ReceiptUpload, UUID> {

    Optional<ReceiptUpload> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByStoredFileId(UUID storedFileId);
}
