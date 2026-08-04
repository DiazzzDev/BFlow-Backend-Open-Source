package bflow.common.financial;

import bflow.auth.services.CurrentUserService;
import bflow.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ControllerTransactionHistory {

    private final ServiceTransactionHistory serviceTransactionHistory;
    private final CurrentUserService currentUserService;

    /** Global unified transaction history for the authenticated user. */
    @GetMapping("/api/v1/transactions")
    public ApiResponse<Page<TransactionResponse>> getGlobalHistory(
            final Authentication authentication,
            @RequestParam(required = false) final String query,
            @RequestParam(required = false) final TransactionType type,
            final Pageable pageable,
            final HttpServletRequest request
    ) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        Page<TransactionResponse> history = serviceTransactionHistory
                .getGlobalHistory(userId, query, type, pageable);

        return ApiResponse.success(
                "Transaction history retrieved successfully",
                history,
                request.getRequestURI()
        );
    }

    @GetMapping("/api/v1/wallets/{walletId}/transactions")
    public ApiResponse<Page<TransactionResponse>> getWalletHistory(
            @PathVariable final UUID walletId,
            final Authentication authentication,
            @RequestParam(required = false) final String query,
            @RequestParam(required = false) final TransactionType type,
            final Pageable pageable,
            final HttpServletRequest request
    ) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        Page<TransactionResponse> history = serviceTransactionHistory
                .getWalletHistory(walletId, userId, query, type, pageable);

        return ApiResponse.success(
                "Wallet transaction history retrieved successfully",
                history,
                request.getRequestURI()
        );
    }
}