package ai.stonefold.gateway.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Tenants seeded from configuration for the local profile. Config carries
 * only key hashes, never plaintext keys: a leaked config file (committed,
 * backed up, screenshotted) must expose nothing usable. Plaintext keys live
 * exclusively on the caller's side.
 *
 * @param tenants declared tenants; empty means "no tenants configured" and
 *                triggers the dev auto-seed (local profile only)
 */
@ConfigurationProperties(prefix = "stonefold")
public record TenantSeedProperties(List<TenantSeed> tenants) {

    public TenantSeedProperties {
        tenants = tenants == null ? List.of() : List.copyOf(tenants);
    }

    /**
     * @param id          tenant id, must be non-blank
     * @param displayName human-readable name; defaults to the id
     * @param activePolicyRef policy version reference, may be null for now
     * @param keyHashes   base64url SHA-256 hashes of the tenant's API keys
     */
    public record TenantSeed(
            String id,
            String displayName,
            String activePolicyRef,
            List<String> keyHashes) {

        public TenantSeed {
            keyHashes = keyHashes == null ? List.of() : List.copyOf(keyHashes);
        }
    }
}
