// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.gateway.core;

/** Identifier of an agent principal within a tenant. */
public record AgentId(String value) {
    public AgentId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("agent id must be non-blank");
        }
    }
}
