package Diaz.Dev.BFlow.auth.services;

import bflow.auth.enums.AuthProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void testAuthProviderEnumIsValid() {
        AuthProvider provider = AuthProvider.GOOGLE;
        assertNotNull(provider);
        assertEquals("GOOGLE", provider.name());
    }

    @Test
    void testLocalAuthProviderExists() {
        AuthProvider local = AuthProvider.LOCAL;
        assertNotNull(local);
        assertEquals("LOCAL", local.name());
    }
}
