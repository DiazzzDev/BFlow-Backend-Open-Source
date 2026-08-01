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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class ControllerWalletInvitation {

    private final ServiceWalletSharing serviceWalletSharing;
    private final CurrentUserService currentUserService;

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