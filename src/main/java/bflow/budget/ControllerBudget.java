package bflow.budget;

import bflow.budget.DTO.BudgetRequest;
import bflow.budget.DTO.BudgetResponse;
import bflow.budget.DTO.BudgetSummaryResponse;
import bflow.budget.DTO.BudgetSearchCriteria;
import bflow.auth.services.CurrentUserService;
import bflow.budget.services.BudgetService;
import bflow.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing budgets.
 */
@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public final class ControllerBudget {
    /** Maximum number of budgets returned by one search request. */
    private static final int MAX_SEARCH_PAGE_SIZE = 100;

    /**
     * The budget service.
     */
    private final BudgetService budgetService;

    /** Service used to resolve the authenticated Cognito user. */
    private final CurrentUserService currentUserService;

    /**
     * Searches budgets owned by the authenticated user.
     * Query parameters include name, walletId, period, scope, startDateFrom,
     * startDateTo, page, size and sort. For example:
     * {@code ?name=holiday&scope=CATEGORY&sort=createdAt,desc}.
     *
     * @param criteria optional dynamic filters
     * @param pageable requested pagination and sorting
     * @param authentication current authenticated user
     * @param request current HTTP request
     * @return a paginated budget search result
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BudgetResponse>>> searchBudgets(
            @ModelAttribute final BudgetSearchCriteria criteria,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC)
            final Pageable pageable,
            final Authentication authentication,
            final HttpServletRequest request
    ) {
        UUID userId = currentUserService.getCurrentUserId(authentication);

        Pageable boundedPageable = PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), MAX_SEARCH_PAGE_SIZE),
                pageable.getSort());
        Page<BudgetResponse> budgets = budgetService.searchBudgets(
                userId, criteria, boundedPageable);

        return ResponseEntity.ok(ApiResponse.success(
                "Budgets retrieved successfully",
                budgets,
                request.getRequestURI()));
    }

    /**
     * Get all budgets for a specific wallet.
     *
     * @param walletId the wallet ID
     * @param authentication the authentication object
     * @return response containing list of budgets
     */
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgetsByWallet(
            @PathVariable final UUID walletId,
            final Authentication authentication) {

        String userIdString = (String) authentication.getPrincipal();
        UUID userId = UUID.fromString(userIdString);

        List<BudgetResponse> budgets =
                budgetService.getBudgetsByWallet(walletId, userId);

        return ResponseEntity.ok(ApiResponse.success(
                "Budgets retrieved successfully",
                budgets,
                "/api/v1/budgets/wallet/" + walletId));
    }

    /**
     * Get the status of a specific budget.
     *
     * @param id the budget ID
     * @param authentication the authentication object
     * @return response containing budget status
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudgetStatus(
            @PathVariable final UUID id,
            final Authentication authentication) {

        String userIdString = (String) authentication.getPrincipal();
        UUID userId = UUID.fromString(userIdString);

        BudgetResponse response = budgetService.getBudgetStatus(id, userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Budget status retrieved successfully",
                        response,
                        "/api/v1/budgets/" + id + "/status"
                )
        );
    }

    /**
     * Create a new budget.
     *
     * @param request the budget request
     * @param authentication the authentication object
     * @return response containing created budget
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @RequestBody @Valid final BudgetRequest request,
            final Authentication authentication) {

        String userIdString = (String) authentication.getPrincipal();
        UUID userId = UUID.fromString(userIdString);

        BudgetResponse response =
                budgetService.createBudget(
                        request, userId, request.getWalletId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Budget created successfully", response,
                        "/api/v1/budgets"));
    }

    /**
     * Get budget summary for a specific wallet.
     *
     * @param walletId the wallet ID
     * @param authentication the authentication object
     * @return response containing budget summary
     */
    @GetMapping("/wallet/{walletId}/summary")
    public ResponseEntity<ApiResponse<BudgetSummaryResponse>> getSummary(
            @PathVariable final UUID walletId,
            final Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        BudgetSummaryResponse summary =
                budgetService.getBudgetSummary(walletId, userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Budget summary retrieved successfully",
                        summary,
                        "/api/v1/budgets/wallet/" + walletId + "/summary"
                )
        );
    }
}
