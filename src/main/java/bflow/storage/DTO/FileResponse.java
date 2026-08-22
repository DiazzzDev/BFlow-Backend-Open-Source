package bflow.storage.DTO;

import bflow.storage.enums.FileStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Public representation of a {@code StoredFile} record.
 */
@Getter
@AllArgsConstructor
public class FileResponse {

    /** Identifier of the stored file record. */
    private final UUID id;

    /** The S3 object key the file is stored under. */
    private final String objectKey;

    /** The original file name supplied by the client. */
    private final String originalFilename;

    /** The MIME type of the file. */
    private final String contentType;

    /** The size of the file in bytes. */
    private final Long sizeBytes;

    /** The current lifecycle status of the file. */
    private final FileStatus status;

    /** The moment this record was created. */
    private final Instant createdAt;
}
