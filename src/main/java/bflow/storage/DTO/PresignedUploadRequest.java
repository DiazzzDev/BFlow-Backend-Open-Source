package bflow.storage.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request to obtain a presigned S3 upload URL for a new file.
 *
 * <p>{@code sizeBytes} and {@code contentType} are the values the
 * client declares it will upload; they are validated against the
 * application's configured constraints before a URL is issued, but
 * the actual object is only verified once uploaded, during upload
 * completion (see {@code bflow.storage.service.FileUploadService
 * #completeUpload}).</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUploadRequest {

    /** Maximum allowed length for the original file name. */
    private static final int FILENAME_MAX_LENGTH = 255;

    /** Maximum allowed length for the content type value. */
    private static final int CONTENT_TYPE_MAX_LENGTH = 255;

    /** The original file name as provided by the client. */
    @NotBlank(message = "The file name is required")
    @Size(
        max = FILENAME_MAX_LENGTH,
        message = "The file name must be at most 255 characters"
    )
    private String originalFilename;

    /** The declared MIME type of the file to upload. */
    @NotBlank(message = "The content type is required")
    @Size(
        max = CONTENT_TYPE_MAX_LENGTH,
        message = "The content type must be at most 255 characters"
    )
    private String contentType;

    /** The declared size in bytes of the file to upload. */
    @NotNull(message = "The file size is required")
    @Positive(message = "The file size must be greater than zero")
    private Long sizeBytes;
}
