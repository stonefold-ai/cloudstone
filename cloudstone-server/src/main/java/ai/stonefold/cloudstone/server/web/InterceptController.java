// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.cloudstone.server.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * The agent-facing interception boundary. The single production chokepoint is
 * {@code POST /submit_intent}: the agent submits one operation (resource,
 * action, data); identity — actor and session — arrives in headers from the
 * authenticated transport, never in the body (scope below the model: the agent
 * cannot set its own scope).
 *
 * <p>The hot path this fronts (resolve session, evaluate policy, append audit,
 * debit ledger, forward/refuse, release) is wired in the walking skeleton
 * (ROADMAP 1.8). Until then this returns a uniform 501 so the route shape,
 * headers, and error envelope are fixed for callers and the conformance kit.
 *
 * <p>Identity headers are accepted but not yet enforced here — the session
 * model (ROADMAP 1.5) decides how {@code X-Session-Id} is opened and how the
 * actor binds to the tenant; requiring their presence belongs with that step,
 * not this shape-only alignment. The tenant is already resolved upstream by
 * {@link TenantAuthFilter} and must not be re-derived from headers here.
 */
@RestController
public class InterceptController {

    @PostMapping("/submit_intent")
    public ResponseEntity<Object> submitIntent(
            @RequestBody SubmitIntentBody body,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        return ResponseEntity.status(501)
                .body(ApiError.of("not_implemented", "enforcement not yet wired"));
    }
}
