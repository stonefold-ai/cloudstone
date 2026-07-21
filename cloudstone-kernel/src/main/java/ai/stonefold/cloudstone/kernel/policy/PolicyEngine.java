// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.cloudstone.kernel.policy;

import ai.stonefold.cloudstone.kernel.action.InterceptedAction;

/**
 * Evaluates an intercepted action against the compiled policy. Implementations
 * must be deterministic and side-effect free; auditing is the caller's job.
 *
 * <p>The kernel is tenant-unaware: which policy this engine was compiled from
 * is the caller's choice. The evaluation context — caller identity, ambient
 * session state, and snapshots of world state such as active kill orders —
 * enters as a value parameter when the decision surface is built out; this
 * interface then grows exactly that one parameter, never a store or clock
 * dependency.
 */
public interface PolicyEngine {

    PolicyDecision evaluate(InterceptedAction action);
}
