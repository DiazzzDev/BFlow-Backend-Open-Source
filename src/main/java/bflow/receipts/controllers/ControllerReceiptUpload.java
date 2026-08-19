package bflow.receipts.controllers;

import bflow.auth.services.CurrentUserService;
import bflow.common.response.ApiResponse;
import bflow.receipts.DTO.ReceiptUploadRequest;
import bflow.receipts.DTO.ReceiptUploadResponse;
import bflow.receipts.service.ReceiptUploadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/receipts")
@RequiredArgsConstructor
public final class ControllerReceiptUpload {

    private final ReceiptUploadService receiptUploadService;
    private final CurrentUserService currentUserService;

    /**
     * Registers an uploaded photo as a receipt for a wallet.
     * Camera-first flow: file already uploaded via the existing
     * presigned-upload endpoints; this is the only extra input the
     * user provides — everything else comes from OCR later.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReceiptUploadResponse>> register(
            @Valid @RequestBody final ReceiptUploadRequest body,
            final Authentication authentication,
            final HttpServletRequest request
    ) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        ReceiptUploadResponse response =
                receiptUploadService.register(userId, body);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Receipt registered; awaiting processing",
                        response, request.getRequestURI()));
    }

    /** Lets the frontend poll while OCR processes the receipt. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReceiptUploadResponse>> getStatus(
            @PathVariable final UUID id,
            final Authentication authentication,
            final HttpServletRequest request
    ) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        ReceiptUploadResponse response =
                receiptUploadService.getStatus(userId, id);

        return ResponseEntity.ok(ApiResponse.success(
                "Receipt status retrieved", response,
                request.getRequestURI()));
    }
}
