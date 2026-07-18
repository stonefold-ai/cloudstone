// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.gateway.server.web;

import java.util.Map;

/**
 * The agent's submitted intent — <em>what</em>, never <em>who</em> (scope
 * below the model). The body carries only the operation; identity arrives in
 * headers from the authenticated transport. Any {@code actor}/{@code owner}
 * keys inside {@code data} are opaque parameters, never identity.
 *
 * @param resource the declared resource the operation addresses
 * @param action   the declared action; {@code null} means the implicit
 *                 {@code read} (observe)
 * @param data     the operation's parameters, as parsed JSON
 */
public record SubmitIntentBody(String resource, String action, Map<String, Object> data) {
}
