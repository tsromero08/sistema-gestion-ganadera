package com.sistemaganadero.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordHash {

    private static final String SALT = "SG-2026-CORP-SALT";

    private PasswordHash() {}

    public static String hash(String plain) {
        if (plain == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest((SALT + plain).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString((SALT + plain).hashCode());
        }
    }

    public static boolean matches(String plain, String hash) {
        if (plain == null || hash == null) return false;
        return hash.equals(hash(plain));
    }
}
