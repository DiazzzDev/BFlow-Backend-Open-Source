package bflow.recurring;

import bflow.auth.services.CurrentUserService;
import bflow.recurring.DTO.RecurringRequest;
import bflow.recurring.DTO.RecurringResponse;
import bflow.recurring.services.RecurringExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for managing recurring transactions.
 */
@Tag(name = "Recurring Movements", description = "Management of scheduled recurring income and expenses")
@RestController
@RequestMapping("/api/v1/recurring")
@RequiredArgsConstructor
public class ControllerRecurring {

    /**
     * The recurring execution service.
     */
    private final RecurringExecutionService recurringService;

    /** Service used to resolve the authenticated user. */
    private final CurrentUserService currentUserService;

    /**
     * Create a new recurring transaction.
     *
     * @param request the recurring request
     * @param authentication the authentication object
     * @return the created recurring response
     */
    @Operation(
            summary = "Create a new recurring transaction.",
            description = "Create a new recurring transaction."
    )
    @PostMapping
    public RecurringResponse create(
            @RequestBody final RecurringRequest request,
            final Authentication authentication
    ) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return recurringService.createRecurring(request, userId);
    }

    /**
     * Get all recurring transactions for the authenticated user.
     *
     * @param authentication the authentication object
     * @return list of recurring responses
     */
    @Operation(
            summary = "Get all recurring transactions for the authenticated user.",
            description = "Get all recurring transactions for the authenticated user."
    )
    @GetMapping
    public List<RecurringResponse> getUserRecurring(
            final Authentication authentication
    ) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return recurringService.getUserRecurring(userId);
    }

    /**
     * Activate a recurring transaction.
     *
     * @param id the recurring transaction ID
     * @param authentication the authentication object
     */
    @Operation(
            summary = "Activate a recurring transaction.",
            description = "Activate a recurring transaction."
    )
    @PatchMapping("/{id}/activate")
    public void activate(
            @PathVariable final UUID id,
            final Authentication authentication
    ) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        recurringService.toggleRecurring(id, userId, true);
    }

    /**
     * Deactivate a recurring transaction.
     *
     * @param id the recurring transaction ID
     * @param authentication the authentication object
     */
    @Operation(
            summary = "Deactivate a recurring transaction.",
            description = "Deactivate a recurring transaction."
    )
    @PatchMapping("/{id}/deactivate")
    public void deactivate(
            @PathVariable final UUID id,
            final Authentication authentication
    ) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        recurringService.toggleRecurring(id, userId, false);
    }

    /**
     * Delete a recurring transaction.
     *
     * @param id the recurring transaction ID
     * @param authentication the authentication object
     */
    @Operation(
            summary = "Delete a recurring transaction.",
            description = "Delete a recurring transaction."
    )
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable final UUID id,
            final Authentication authentication
    ) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        recurringService.deleteRecurring(id, userId);
    }
}
