package bflow.common.aws.service;

import java.io.InputStream;

/**
 * Represents a downloaded storage object along with the metadata
 * needed to stream it back to a client.
 *
 * @param content the object content stream; the caller is
 *                responsible for closing it
 * @param contentType the MIME type reported by the storage provider
 * @param contentLength the size in bytes of the object
 */
public record StorageObject(
        InputStream content,
        String contentType,
        long contentLength
) {
}
