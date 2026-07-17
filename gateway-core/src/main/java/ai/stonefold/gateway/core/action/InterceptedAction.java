package ai.stonefold.gateway.core.action;

import java.util.Map;

/**
 * One action an agent attempted, as seen at the gateway boundary — e.g. an
 * outbound tool call, a message to another agent, a resource mutation.
 * Field vocabulary must track the spec's wire format; the shape here is a
 * skeleton placeholder.
 *
 * @param kind      spec-defined action kind (tool_call, message, ...)
 * @param target    what the action addresses (tool name, counterparty, URI)
 * @param payload   raw action body as parsed JSON
 */
public record InterceptedAction(String kind, String target, Map<String, Object> payload) {
}
