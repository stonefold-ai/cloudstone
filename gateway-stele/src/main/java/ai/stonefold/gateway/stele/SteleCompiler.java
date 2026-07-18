// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.gateway.stele;

import ai.stonefold.gateway.core.policy.PolicyEngine;

/**
 * Compiles a validated {@link SteleDocument} into an executable, immutable
 * {@link PolicyEngine}. Compilation happens once per policy version at load
 * time; evaluation on the hot path does no parsing.
 */
public final class SteleCompiler {

    public PolicyEngine compile(SteleDocument document) {
        // TODO: rule matching, condition evaluation, default verdict
        throw new UnsupportedOperationException("not implemented yet");
    }
}
