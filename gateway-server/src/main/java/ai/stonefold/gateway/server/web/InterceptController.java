// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.gateway.server.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * The interception boundary agents talk to. Route shapes must follow the
 * wire protocol in the spec repo; these are placeholders to hang the wiring
 * on. Order on the hot path is fixed: resolve session -> evaluate policy ->
 * append audit -> debit ledger -> forward/refuse.
 */
@RestController
@RequestMapping("/v1")
public class InterceptController {

    @PostMapping("/sessions")
    public ResponseEntity<Map<String, Object>> openSession(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(501).body(Map.of("error", "not implemented"));
    }

    @PostMapping("/sessions/{sessionId}/actions")
    public ResponseEntity<Map<String, Object>> submitAction(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(501).body(Map.of("error", "not implemented"));
    }
}
