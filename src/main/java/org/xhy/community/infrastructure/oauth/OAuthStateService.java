package org.xhy.community.infrastructure.oauth;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class OAuthStateService {
    private static final long DEFAULT_TTL_MILLIS = 5 * 60 * 1000; // 5 minutes
    private final SecureRandom random = new SecureRandom();

    public String generateState() {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        long ts = System.currentTimeMillis() + DEFAULT_TTL_MILLIS;
        String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String payload = nonce + ":" + ts;
        String sig = sha256(payload);
        return Base64.getUrlEncoder().withoutPadding().encodeToString((payload + ":" + sig).getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateState(String state) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length != 3) return false;
            String payload = parts[0] + ":" + parts[1];
            String sig = parts[2];
            if (!sha256(payload).equals(sig)) return false;
            long ts = Long.parseLong(parts[1]);
            return System.currentTimeMillis() <= ts;
        } catch (Exception e) {
            return false;
        }
    }

    private String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

