package bflow.storage.controllers;

import bflow.auth.services.CurrentUserService;
import bflow.common.response.ApiResponse;
import bflow.storage.DTO.FileResponse;
import bflow.storage.DTO.PresignedDownloadResponse;
import bflow.storage.DTO.PresignedUploadRequest;
import bflow.storage.DTO.PresignedUploadResponse;
import bflow.storage.service.FileUploadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.UUID;

/**
 * Controller for the file upload lifecycle.
 * Provides REST endpoints for requesting presigned S3 upload URLs
 * and confirming upload completion.
 */
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public final class ControllerFileUpload {

    /** The service handling file upload business logic. */
    private final FileUploadService fileUploadService;

    /** Service used to resolve the authenticated user. */
    private final CurrentUserService currentUserService;

    /**
     * Requests a presigned S3 upload URL for a new file. Creates the
     * corresponding {@code StoredFile} record in {@code PENDING}
     * status.
     *
     * @param body the declared file metadata.
     * @param authentication the authenticated user's principal.
     * @param request the HTTP request for path information.
     * @return a standard API response containing the presigned
     *         upload URL and related metadata.
     */
    @PostMapping("/presigned-upload")
    public ResponseEntity<ApiResponse<PresignedUploadResponse>>
    createPresignedUpload(
            @Valid @RequestBody final PresignedUploadRequest body,
            final Authentication authentication,
            final HttpServletRequest request
    ) {
        UUID userId = currentUserService.getCurrentUserId(authentication);

        PresignedUploadResponse response = fileUploadService
                .createPresignedUpload(userId, body);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Presigned upload URL generated successfully",
                        response,
                        request.getRequestURI()
                ));
    }

    /**
     * Confirms that a previously requested upload actually reached
     * S3, transitioning the file's status accordingly.
     *
     * @param id the stored file identifier.
     * @param authentication the authenticated user's principal.
     * @param request the HTTP request for path information.
     * @return a standard API response containing the file's current
     *         state.
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<FileResponse>> completeUpload(
            @PathVariable final UUID id,
            final Authentication authentication,
            final HttpServletRequest request
    ) {
        UUID userId = currentUserService.getCurrentUserId(authentication);

        FileResponse response = fileUploadService
                .completeUpload(userId, id);

        return ResponseEntity.ok(ApiResponse.success(
                "File upload completed successfully",
                response,
                request.getRequestURI()
        ));
    }

    /**
     * Issues a presigned S3 download URL for a file the
     * authenticated user owns.
     *
     * @param id the stored file identifier.
     * @param authentication the authenticated user's principal.
     * @param request the HTTP request for path information.
     * @return a standard API response containing the presigned
     *         download URL and file metadata.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<ApiResponse<PresignedDownloadResponse>>
    createDownloadUrl(
            @PathVariable final UUID id,
            final Authentication authentication,
            final HttpServletRequest request
    ) {
        UUID userId = currentUserService.getCurrentUserId(authentication);

        PresignedDownloadResponse response = fileUploadService
                .createDownloadUrl(userId, id);

        return ResponseEntity.ok(ApiResponse.success(
                "Presigned download URL generated successfully",
                response,
                request.getRequestURI()
        ));
    }
}
