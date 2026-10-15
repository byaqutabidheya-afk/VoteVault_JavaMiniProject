package voting.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class providing cryptographic SHA-256 hashing functions.
 */
public final class HashUtil {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private HashUtil() {
    }

    /**
     * Computes the SHA-256 hash of the given string input.
     *
     * @param input the input string to hash
     * @return a lowercase 64-character hexadecimal SHA-256 hash
     * @throws RuntimeException if the SHA-256 algorithm is not supported
     */
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(64);
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
