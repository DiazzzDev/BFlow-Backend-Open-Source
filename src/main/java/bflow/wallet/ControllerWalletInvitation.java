package bflow.wallet;

import bflow.auth.services.CurrentUserService;
import bflow.common.response.ApiResponse;
import bflow.wallet.DTO.WalletInvitationRequest;
import bflow.wallet.DTO.WalletInvitationResponse;
import bflow.wallet.DTO.WalletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class ControllerWalletInvitation {

    /**
     * Service responsible for wallet sharing operations.
     */
    private final ServiceWalletSharing serviceWalletSharing;

    /**
     * Service used to retrieve the authenticated user.
     */
    private final CurrentUserService currentUserService;

    /**
     * Sends a wallet invitation to a user.
     *
     * @param walletId the wallet UUID
     * @param request the invitation request
     * @param authentication the authenticated user
     * @param httpRequest the current HTTP request
     * @return the created invitation
     */
    @PostMapping("/{walletId}/invitations")
    public ApiResponse<WalletInvitationResponse> inviteMember(
        @PathVariable final UUID walletId,
        @Valid @RequestBody final WalletInvitationRequest request,
        final Authentication authentication,
        final HttpServletRequest httpRequest
    ) {
        UUID userId = currentUserService.getCurrentUserId(authentication);

        WalletInvitationResponse response =
            serviceWalletSharing.inviteMember(
                    walletId,
                    userId,
                    request.getInvitedEmail()
            );

        return ApiResponse.success(
            "Invitation sent successfully.",
            response,
            httpRequest.getRequestURI()
        );
    }

    /**
     * Accepts a wallet invitation.
     *
     * @param token the invitation token
     * @param authentication the authenticated user
     * @param httpRequest the current HTTP request
     * @return the updated wallet information
     */
    @PostMapping("/invitations/{token}/accept")
    public ApiResponse<WalletResponse> acceptInvitation(
            @PathVariable final String token,
            final Authentication authentication,
            final HttpServletRequest httpRequest
    ) {

        UUID userId = currentUserService.getCurrentUserId(authentication);

        WalletResponse response =
                serviceWalletSharing.acceptInvitation(
                        token,
                        userId
                );

        return ApiResponse.success(
                "Invitation accepted successfully.",
                response,
                httpRequest.getRequestURI()
        );
    }

    /**
     * Rejects a wallet invitation.
     *
     * @param token the invitation token
     * @param authentication the authenticated user
     * @param httpRequest the current HTTP request
     * @return a successful response
     */
    @PostMapping("/invitations/{token}/reject")
    public ApiResponse<Void> rejectInvitation(
            @PathVariable final String token,
            final Authentication authentication,
            final HttpServletRequest httpRequest
    ) {

        UUID userId = currentUserService.getCurrentUserId(authentication);

        serviceWalletSharing.rejectInvitation(
                token,
                userId
        );

        return ApiResponse.success(
                "Invitation rejected successfully.",
                null,
                httpRequest.getRequestURI()
        );
    }

    /**
     * Cancels a pending wallet invitation.
     *
     * @param invitationId the invitation UUID
     * @param authentication the authenticated user
     * @param httpRequest the current HTTP request
     * @return a successful response
     */
    @DeleteMapping("/invitations/{invitationId}")
    public ApiResponse<Void> cancelInvitation(
            @PathVariable final UUID invitationId,
            final Authentication authentication,
            final HttpServletRequest httpRequest
    ) {

        UUID userId = currentUserService.getCurrentUserId(authentication);

        serviceWalletSharing.cancelInvitation(
                invitationId,
                userId
        );

        return ApiResponse.success(
                "Invitation canceled successfully.",
                null,
                httpRequest.getRequestURI()
        );
    }

    /**
     * Removes a member from a shared wallet.
     *
     * @param walletId the wallet UUID
     * @param memberId the member UUID
     * @param authentication the authenticated user
     * @param httpRequest the current HTTP request
     * @return a successful response
     */
    @DeleteMapping("/{walletId}/members/{memberId}")
    public ApiResponse<Void> removeMember(
            @PathVariable final UUID walletId,
            @PathVariable final UUID memberId,
            final Authentication authentication,
            final HttpServletRequest httpRequest
    ) {

        UUID userId = currentUserService.getCurrentUserId(authentication);

        serviceWalletSharing.removeMember(
                walletId,
                userId,
                memberId
        );

        return ApiResponse.success(
                "Member removed successfully.",
                null,
                httpRequest.getRequestURI()
        );
    }

    /**
     * Retrieves all pending invitations for the authenticated user.
     *
     * @param authentication the authenticated user
     * @param httpRequest the current HTTP request
     * @return the list of pending invitations
     */
    @GetMapping("/invitations")
    public ApiResponse<List<WalletInvitationResponse>> getPendingInvitations(
            final Authentication authentication,
            final HttpServletRequest httpRequest
    ) {

        UUID userId = currentUserService.getCurrentUserId(authentication);

        List<WalletInvitationResponse> response =
                serviceWalletSharing.getPendingInvitations(userId);

        return ApiResponse.success(
                "Pending invitations retrieved successfully.",
                response,
                httpRequest.getRequestURI()
        );
    }
}
