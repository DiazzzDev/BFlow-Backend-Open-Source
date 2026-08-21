package bflow.storage.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Response containing a presigned S3 upload URL along with the
 * headers the client must send with the PUT request and the id of
 * the {@code StoredFile} record created for it.
 */
@Getter
@AllArgsConstructor
public class PresignedUploadResponse {

    /** Identifier of the StoredFile record, in PENDING status. */
    private final UUID fileId;

    /** The presigned URL the client must PUT the file content to. */
    private final String uploadUrl;

    /** The S3 object key the file will be stored under. */
    private final String objectKey;

    /** The moment the presigned URL stops being valid. */
    private final Instant expiresAt;

    /**
     * Headers the client must include on the PUT request for the
     * signature to validate (at minimum, Content-Type).
     */
    private final Map<String, String> requiredHeaders;
}
