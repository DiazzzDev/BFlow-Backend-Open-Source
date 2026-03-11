package Diaz.Dev.BFlow.auth.security.jwt;

import bflow.auth.security.jwt.JwtServiceImpl;
import bflow.auth.security.jwt.RsaKeyPair;
import bflow.auth.security.jwt.RsaKeyProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @Mock
    private RsaKeyProvider rsaKeyProvider;

    @InjectMocks
    private JwtServiceImpl jwtService;

    private RsaKeyPair rsaKeyPair;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        rsaKeyPair = new RsaKeyPair(
                "test-kid",
                keyPair.getPrivate(),
                (RSAPublicKey) keyPair.getPublic()
        );
    }

    @Test
    void shouldGenerateAndValidateToken() {
        when(rsaKeyProvider.getActive()).thenReturn(rsaKeyPair);
        when(rsaKeyProvider.getPublicKey("test-kid"))
                .thenReturn(rsaKeyPair.publicKey());

        UUID userId = UUID.randomUUID();
        String email = "test@bflow.com";
        List<String> roles = List.of("USER");

        String token = jwtService.generateToken(userId, email, roles);

        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void shouldExtractClaimsCorrectly() {
        when(rsaKeyProvider.getActive()).thenReturn(rsaKeyPair);

        UUID userId = UUID.randomUUID();
        String email = "claims@bflow.com";
        List<String> roles = List.of("ADMIN");

        String token = jwtService.generateToken(userId, email, roles);

        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(email, jwtService.extractEmail(token));
        assertEquals(roles, jwtService.extractRoles(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(jwtService.validateToken("this.is.not.a.jwt"));
    }

    @Test
    void shouldRejectTamperedToken() {
        when(rsaKeyProvider.getActive()).thenReturn(rsaKeyPair);
        when(rsaKeyProvider.getPublicKey("test-kid"))
                .thenReturn(rsaKeyPair.publicKey());

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "a@b.com",
                List.of("USER")
        );

        String tampered = token.substring(0, token.length() - 10) + "abcdef";

        assertFalse(jwtService.validateToken(tampered));
    }
}