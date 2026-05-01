package org.xhy.community.domain.oauth2.entity;

import org.junit.jupiter.api.Test;
import org.xhy.community.domain.oauth2.valueobject.ClientAuthenticationMethod;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OAuth2ClientEntityTest {

    @Test
    void publicClientAllowsLoopbackRedirectWithRandomPort() {
        OAuth2ClientEntity client = new OAuth2ClientEntity();
        client.setClientAuthenticationMethods(List.of(ClientAuthenticationMethod.NONE.getValue()));
        client.setRedirectUris(List.of("http://127.0.0.1/callback", "http://localhost/callback"));

        assertTrue(client.isValidRedirectUri("http://127.0.0.1:49152/callback"));
        assertTrue(client.isValidRedirectUri("http://localhost:53123/callback"));
    }

    @Test
    void loopbackRedirectKeepsStrictHostPathAndSchemeRules() {
        OAuth2ClientEntity client = new OAuth2ClientEntity();
        client.setClientAuthenticationMethods(List.of(ClientAuthenticationMethod.NONE.getValue()));
        client.setRedirectUris(List.of("http://127.0.0.1/callback"));

        assertFalse(client.isValidRedirectUri("https://127.0.0.1:49152/callback"));
        assertFalse(client.isValidRedirectUri("http://127.0.0.1:49152/other"));
        assertFalse(client.isValidRedirectUri("http://example.com:49152/callback"));
        assertFalse(client.isValidRedirectUri("http://localhost:49152/callback"));
    }

    @Test
    void confidentialClientDoesNotAllowRandomLoopbackPort() {
        OAuth2ClientEntity client = new OAuth2ClientEntity();
        client.setClientAuthenticationMethods(List.of(ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue()));
        client.setRedirectUris(List.of("http://127.0.0.1/callback"));

        assertFalse(client.isValidRedirectUri("http://127.0.0.1:49152/callback"));
    }
}
