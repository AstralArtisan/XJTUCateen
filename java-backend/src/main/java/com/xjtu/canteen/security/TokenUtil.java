package com.xjtu.canteen.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenUtil {
    private final String secret;
    private final long expireSeconds;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TokenUtil(@Value("${app.token-secret}") String secret,
                     @Value("${app.token-expire-seconds}") long expireSeconds) {
        this.secret = secret;
        this.expireSeconds = expireSeconds;
    }

    public String createToken(long userId, int role) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("user_id", userId);
            payload.put("role", role);
            payload.put("exp", Instant.now().getEpochSecond() + expireSeconds);
            String body = urlSafeEncode(objectMapper.writeValueAsBytes(payload));
            String signature = urlSafeEncode(hmac(body));
            return body + "." + signature;
        } catch (Exception e) {
            throw new IllegalStateException("create token failed", e);
        }
    }

    public Map<String, Object> parseToken(String token) {
        try {
            String[] parts = token.split("\\.", 2);
            if (parts.length != 2) return null;
            byte[] expected = hmac(parts[0]);
            byte[] got = urlSafeDecode(parts[1]);
            if (!java.security.MessageDigest.isEqual(expected, got)) return null;
            Map<String, Object> payload = objectMapper.readValue(urlSafeDecode(parts[0]), new TypeReference<>() {});
            long exp = ((Number) payload.get("exp")).longValue();
            if (exp < Instant.now().getEpochSecond()) return null;
            return payload;
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] hmac(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(body.getBytes(StandardCharsets.US_ASCII));
    }

    private String urlSafeEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private byte[] urlSafeDecode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }
}
