package bflow.auth.controllers;

import bflow.auth.DTO.Record.SyncUserRequest;
import bflow.auth.DTO.Record.SyncUserResponse;
import bflow.auth.services.AuthSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for authentication synchronization operations.
 */
@Tag(name = "Authentication", description = "Synchronization of authenticated users via Cognito")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    /**
     * Service responsible for synchronizing Cognito users.
     */
    private final AuthSyncService authSyncService;

    /**
     * Synchronizes the authenticated Cognito user with the local database
     * and returns the initial session data required by the client.
     *
     * @param jwt authenticated JWT token
     * @param request synchronization request payload
     * @return synchronization result with initial session data
     */
    @Operation(
            summary = "Synchronizes the authenticated user",
            description = "Takes the JWT issued by Cognito, creates or updates the corresponding "
                    + "user in the local database, and returns the initial session data needed "
                    + "by the client (profile, wallets, etc.)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User synchronized successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token")
    })
    @PostMapping("/sync")
    public SyncUserResponse sync(
            @AuthenticationPrincipal final Jwt jwt,
            @RequestBody final SyncUserRequest request
    ) {
        log.debug("Auth sync initiated");
        return authSyncService.synchronize(jwt, request);
    }
}
