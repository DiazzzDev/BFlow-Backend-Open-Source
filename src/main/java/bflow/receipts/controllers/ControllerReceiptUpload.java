package bflow.receipts.controllers;

import bflow.auth.services.CurrentUserService;
import bflow.common.response.ApiResponse;
import bflow.receipts.DTO.ReceiptConfirmRequest;
import bflow.receipts.DTO.ReceiptUploadRequest;
import bflow.receipts.DTO.ReceiptUploadResponse;
import bflow.receipts.service.ReceiptUploadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/receipts")
@RequiredArgsConstructor
public final class ControllerReceiptUpload {

    /** Service responsible for receipt upload and processing operations. */
    private final ReceiptUploadService receiptUploadService;

    /** Service responsible for resolving the authenticated user. */
    private final CurrentUserService currentUserService;

    /**
     * Registers an uploaded photo as a receipt for a wallet.
     * Camera-first flow: file already uploaded via the existing
     * presigned-upload endpoints; this is the only extra input the
     * user provides — everything else comes from OCR later.
     *
     * @param body request containing the uploaded receipt information
     * @param authentication authentication information of the current user
     * @param request HTTP request used to obtain the request URI
     * @return response containing the registered receipt information
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

    /**
     * Retrieves the current processing status of a receipt.
     * Lets the frontend poll while OCR processes the receipt.
     *
     * @param id identifier of the receipt
     * @param authentication authentication information of the current user
     * @param request HTTP request used to obtain the request URI
     * @return response containing the current receipt processing status
     */
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

    /**
     * Confirms a receipt's suggested data — as edited by the user —
     * into a new Expense or Income.
     *
     * @param id identifier of the receipt being confirmed
     * @param body the confirmed transaction data
     * @param authentication authentication information of the current user
     * @param request HTTP request used to obtain the request URI
     * @return response containing the confirmed receipt, now
     *         CONFIRMED and linked to the resulting Expense/Income
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<ReceiptUploadResponse>> confirm(
            @PathVariable final UUID id,
            @Valid @RequestBody final ReceiptConfirmRequest body,
            final Authentication authentication,
            final HttpServletRequest request
    ) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        ReceiptUploadResponse response =
                receiptUploadService.confirm(userId, id, body);

        return ResponseEntity.ok(ApiResponse.success(
                "Receipt confirmed", response,
                request.getRequestURI()));
    }

    /**
     * Discards a receipt the user doesn't want to keep, deleting
     * its underlying file immediately.
     *
     * @param id identifier of the receipt being discarded
     * @param authentication authentication information of the current user
     * @param request HTTP request used to obtain the request URI
     * @return an empty response confirming the receipt was discarded
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> discard(
            @PathVariable final UUID id,
            final Authentication authentication,
            final HttpServletRequest request
    ) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        receiptUploadService.discard(userId, id);

        return ResponseEntity.ok(ApiResponse.success(
                "Receipt discarded", null,
                request.getRequestURI()));
    }
}
