// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.gateway.core.policy;

import ai.stonefold.gateway.core.session.SessionContext;
import ai.stonefold.gateway.core.action.InterceptedAction;

/**
 * Evaluates an intercepted action within a session against the tenant's
 * policy. Implementations must be deterministic and side-effect free;
 * auditing is the caller's job.
 */
public interface PolicyEngine {

    PolicyDecision evaluate(SessionContext session, InterceptedAction action);
}
