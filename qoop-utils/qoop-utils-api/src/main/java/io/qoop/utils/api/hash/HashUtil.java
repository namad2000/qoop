package io.qoop.utils.api.hash;

import java.security.MessageDigest;

public final class HashUtil {

    /**
     * Calculates SHA-256 hash of the file bytes
     */
    public static String calculateHash(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
