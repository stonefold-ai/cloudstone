// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.cloudstone.server.web;

import java.util.Map;

/**
 * The uniform error envelope every gateway error response carries:
 * <pre>{"error": {"code": ..., "message": ...}}</pre>
 * {@code code} is a stable machine string; {@code message} is a human hint and
 * must reveal nothing about tenant existence or credential validity (the 401
 * path depends on this). A batch refusal adds a {@code pointer} naming the
 * failing operation ({@code operations[i]}); that form lands with the batch
 * route, not here.
 */
final class ApiError {

    private ApiError() {
    }

    static Map<String, Object> of(String code, String message) {
        return Map.of("error", Map.of("code", code, "message", message));
    }
}
