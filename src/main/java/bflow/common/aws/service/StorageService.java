package bflow.common.aws.service;

import java.io.InputStream;

/**
 * Abstraction over the application's object storage provider.
 * Controllers and domain services must depend on this interface
 * instead of any AWS SDK client directly.
 *
 * <p>Implementations must treat every key as untrusted input: keys
 * are expected to be scoped under {@code users/{userId}/...} or
 * {@code tmp/{uuid}} and must be validated before use. Ownership of a
 * key against the currently authenticated user is a concern of the
 * calling application layer, not of this abstraction.</p>
 */
public interface StorageService {

    /**
     * Uploads an object to storage. The object is always stored
     * privately; no public access or ACL is ever configured.
     *
     * @param key the destination object key
     * @param inputStream the file content
     * @param contentLength the exact length in bytes of the content
     * @param contentType the MIME type to associate with the object
     */
    void upload(
            String key,
            InputStream inputStream,
            long contentLength,
            String contentType
    );

    /**
     * Downloads an object from storage.
     *
     * @param key the object key to retrieve
     * @return the object content along with its metadata
     */
    StorageObject download(String key);

    /**
     * Deletes an object from storage. Deleting a key that does not
     * exist is not treated as an error.
     *
     * @param key the object key to delete
     */
    void delete(String key);

    /**
     * Checks whether an object exists in storage.
     *
     * @param key the object key to check
     * @return true if the object exists, false otherwise
     */
    boolean exists(String key);
}
