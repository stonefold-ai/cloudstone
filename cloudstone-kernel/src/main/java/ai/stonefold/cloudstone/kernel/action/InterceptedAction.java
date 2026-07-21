// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.cloudstone.kernel.action;

import java.util.Map;

/**
 * One operation an agent submitted, assembled at the gateway boundary — the
 * internal view enforcement sees. The field vocabulary is the spec wire op
 * (SIF; TCK {@code op}): resource, action, data, target, sink, context.
 *
 * <p>The action's {@code kind} is not carried here — it is a property of the
 * declared action in the registry (SIF's five kinds), resolved during
 * evaluation, not something the agent sends.
 *
 * <p>{@code sink} and {@code context} are supplied by the session/transport,
 * never by the agent body (scope below the model): the agent cannot name its
 * own disclosure destination or set ambient flags. The routes assemble those
 * from the authenticated session; the agent body carries only resource,
 * action, and data.
 *
 * @param resource the declared resource the operation addresses
 * @param action   the declared action name; {@code null} means the implicit
 *                 {@code read} (observe)
 * @param data     the operation's parameters, as parsed JSON
 * @param target   id of an existing row the operation addresses, resolved
 *                 below the model; {@code null} if none
 * @param sink     the requested disclosure destination; {@code null} if none
 * @param context  ambient session state (e.g. {@code breakGlass}), supplied by
 *                 the session, not the agent
 */
public record InterceptedAction(
        String resource,
        String action,
        Map<String, Object> data,
        String target,
        String sink,
        Map<String, Object> context) {
}
