// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.cloudstone.kernel.policy;

import java.util.Map;

/**
 * Result of evaluating one intercepted action against the tenant's compiled
 * policy. Deterministic: same policy + same action = same decision. The
 * gateway never asks a model to make this call.
 *
 * @param decision   outcome kind
 * @param ruleId     id of the Stele rule that produced the outcome, or null
 *                   for the default verdict
 * @param detail     machine-readable context for the audit log (never shown
 *                   to the intercepted agent verbatim)
 */
public record PolicyDecision(Decision decision, String ruleId, Map<String, Object> detail) {

    public static PolicyDecision defaultDeny() {
        return new PolicyDecision(Decision.DENY, null, Map.of("reason", "no rule matched"));
    }
}
