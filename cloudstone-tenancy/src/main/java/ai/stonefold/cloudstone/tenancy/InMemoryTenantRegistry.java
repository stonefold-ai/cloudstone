// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.cloudstone.tenancy;

import ai.stonefold.cloudstone.runtime.TenantId;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Config-seeded registry for local single-node runs and tests. Cloud
 * deployments replace this with a persistence-backed implementation; the
 * authentication semantics reviewed here are the contract both must meet.
 *
 * A tenant may hold several active key hashes at once — that is what makes
 * zero-downtime rotation possible (issue new key, migrate callers, revoke
 * old). Plaintext keys never enter this class.
 */
public final class InMemoryTenantRegistry implements TenantRegistry {

    /** Stored hash kept alongside the owner for the constant-time re-check. */
    private record KeyEntry(String storedHash, TenantId tenant) {
    }

    private final Map<TenantId, Tenant> tenants = new ConcurrentHashMap<>();
    private final Map<String, KeyEntry> keysByHash = new ConcurrentHashMap<>();

    public void register(Tenant tenant, String... apiKeyHashes) {
        tenants.put(tenant.id(), tenant);
        for (String hash : apiKeyHashes) {
            KeyEntry previous = keysByHash.putIfAbsent(hash, new KeyEntry(hash, tenant.id()));
            if (previous != null && !previous.tenant().equals(tenant.id())) {
                // The same 256-bit hash for two tenants does not happen by
                // chance; this is a mis-seeded config and must not boot.
                throw new IllegalStateException(
                        "api key hash already registered to a different tenant");
            }
        }
    }

    public void revokeKey(String apiKeyHash) {
        keysByHash.remove(apiKeyHash);
    }

    @Override
    public Optional<Tenant> find(TenantId id) {
        return Optional.ofNullable(tenants.get(id));
    }

    @Override
    public Optional<Tenant> authenticate(String presentedApiKey) {
        if (presentedApiKey == null || presentedApiKey.isEmpty()) {
            return Optional.empty();
        }
        // Hash first, then look up by hash. The cost is identical for valid,
        // unknown and malformed keys, and lookup timing cannot correlate
        // with how many characters of a real key a guess shares — the map
        // key is the digest, not the secret.
        String presentedHash = ApiKeys.hash(presentedApiKey);
        KeyEntry entry = keysByHash.get(presentedHash);
        if (entry == null) {
            return Optional.empty();
        }
        // Belt-and-braces: the map hit already implies equality, but the
        // explicit constant-time digest comparison pins the intent so a
        // future refactor (e.g. to a prefix-indexed store) cannot quietly
        // introduce a variable-time compare over secret-derived bytes.
        if (!MessageDigest.isEqual(
                entry.storedHash().getBytes(StandardCharsets.UTF_8),
                presentedHash.getBytes(StandardCharsets.UTF_8))) {
            return Optional.empty();
        }
        return find(entry.tenant());
    }
}
