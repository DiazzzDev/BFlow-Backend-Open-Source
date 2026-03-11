package bflow.auth.controllers;

import bflow.auth.security.jwt.RsaKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal controller for managing RSA key rotation.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/keys")
public final class KeyRotationController {

    /** Provider for RSA key operations. */
    private final RsaKeyProvider keyProvider;

    /**
     * Triggers the rotation of the current RSA key pair.
     */
    @PostMapping("/rotate")
    public void rotate() {
        keyProvider.rotate();
    }
}
