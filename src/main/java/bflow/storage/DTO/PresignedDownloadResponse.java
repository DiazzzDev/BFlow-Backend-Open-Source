package bflow.storage.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Response containing a presigned S3 download URL for a file the
 * requesting user owns.
 */
@Getter
@AllArgsConstructor
public class PresignedDownloadResponse {

    /** Identifier of the stored file record. */
    private final UUID fileId;

    /** The presigned URL the client can GET the file content from. */
    private final String downloadUrl;

    /** The original file name, for display purposes. */
    private final String originalFilename;

    /** The MIME type of the file. */
    private final String contentType;

    /** The size of the file in bytes. */
    private final Long sizeBytes;

    /** The moment the presigned URL stops being valid. */
    private final Instant expiresAt;
}
