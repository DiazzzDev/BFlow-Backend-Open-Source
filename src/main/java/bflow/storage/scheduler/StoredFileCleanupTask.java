package bflow.storage.scheduler;

import bflow.common.aws.service.StorageService;
import bflow.storage.repository.RepositoryStoredFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Removes StoredFile records (and their S3 objects) that never
 * became useful:
 *
 * <ul>
 *   <li>PENDING for longer than the presign duration plus a grace
 *       window — the client never uploaded, or completion was
 *       never confirmed.</li>
 *   <li>UPLOADED but never referenced by any domain entity after a
 *       longer grace window — the user uploaded a receipt and then
 *       abandoned the flow before saving the expense.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoredFileCleanupTask {

    private final RepositoryStoredFile repository;
    private final StorageService storageService;

    @Value("${app.storage.pending-cleanup-hours:24}")
    private long pendingCleanupHours;

    @Value("${app.storage.unreferenced-cleanup-days:7}")
    private long unreferencedCleanupDays;

    @Scheduled(cron = "0 30 * * * *")
    @Transactional
    public void purgeOrphanedFiles() {
        Instant pendingCutoff = Instant.now()
                .minus(pendingCleanupHours, ChronoUnit.HOURS);
        Instant unreferencedCutoff = Instant.now()
                .minus(unreferencedCleanupDays, ChronoUnit.DAYS);

        List<String> deletedKeys = repository
                .deleteOrphanedAndReturnKeys(pendingCutoff, unreferencedCutoff);

        deletedKeys.forEach(storageService::delete);

        log.info("Stored file cleanup: {} orphaned files removed",
                deletedKeys.size());
    }
}