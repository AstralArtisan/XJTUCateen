package com.xjtu.canteen.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            RANDOM.nextBytes(salt);
            byte[] digest = pbkdf2(password, salt);
            byte[] raw = new byte[salt.length + digest.length];
            System.arraycopy(salt, 0, raw, 0, salt.length);
            System.arraycopy(digest, 0, raw, salt.length, digest.length);
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new IllegalStateException("hash password failed", e);
        }
    }

    public static boolean verifyPassword(String password, String hashedValue) {
        try {
            byte[] raw = Base64.getDecoder().decode(hashedValue.getBytes(StandardCharsets.US_ASCII));
            byte[] salt = java.util.Arrays.copyOfRange(raw, 0, 16);
            byte[] digest = java.util.Arrays.copyOfRange(raw, 16, raw.length);
            byte[] candidate = pbkdf2(password, salt);
            return java.security.MessageDigest.isEqual(candidate, digest);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 120000, 256);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    }
}
