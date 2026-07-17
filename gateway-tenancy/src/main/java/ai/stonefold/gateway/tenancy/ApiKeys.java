package ai.stonefold.gateway.tenancy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * API key generation and hashing.
 *
 * Keys are gateway-generated, 32 bytes from a CSPRNG, so they carry 256 bits
 * of real entropy. That is why storage is plain unsalted SHA-256 rather than
 * a slow password hash: no dictionary exists for random 256-bit values, so
 * slow hashing would add per-request CPU cost (a DoS surface on the hot
 * path) and no security. The hash doubles as the lookup index.
 */
public final class ApiKeys {

    public static final String PREFIX = "sfk_";

    private static final SecureRandom RANDOM = new SecureRandom();

    private ApiKeys() {
    }

    /** Generate a fresh key. Shown once at issuance; only its hash is stored. */
    public static String generate() {
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
    }

    /**
     * Hash of a presented key, base64url. Deliberately no format
     * pre-validation: a malformed key is hashed like any other and simply
     * misses on lookup, so the malformed and unknown paths cost the same
     * and there is no early-exit timing signal.
     */
    public static String hash(String presentedKey) {
        byte[] digest = sha256().digest(presentedKey.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    /**
     * True if the value has the shape {@link #hash} produces: exactly 43
     * base64url characters, no padding. Used to fail fast on configured
     * hashes that cannot possibly match anything — a plaintext key, a
     * truncated paste, a hex digest from the wrong tool.
     */
    public static boolean isHashShaped(String value) {
        if (value == null || value.length() != 43) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            boolean base64url = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9') || c == '-' || c == '_';
            if (!base64url) {
                return false;
            }
        }
        return true;
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // Mandated by the JCA spec to exist on every JVM.
            throw new AssertionError(e);
        }
    }
}
