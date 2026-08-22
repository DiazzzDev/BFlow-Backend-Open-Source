package bflow.storage.enums;

/**
 * Lifecycle status of a {@link bflow.storage.entity.StoredFile}.
 */
public enum FileStatus {

    /**
     * A presigned upload was issued but the client has not yet
     * confirmed (or the object has not yet been verified in S3).
     */
    PENDING,

    /**
     * The upload was confirmed and the object exists in S3.
     */
    UPLOADED,

    /**
     * The upload never completed (e.g. the presigned URL expired
     * or the object could not be verified in S3).
     */
    FAILED
}
