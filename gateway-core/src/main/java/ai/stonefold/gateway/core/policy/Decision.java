// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.gateway.core.policy;

/**
 * Outcome kinds a policy evaluation can produce. The authoritative list and
 * semantics live in the spec repo (spec wins on divergence).
 *
 * <p>The wire submit-decision set is {@code allow | hold | deny | halt}
 * (RFC §2) — the four values the conformance kit asserts on over the wire.
 * {@code HOLD} stages for approval/precondition release; {@code HALT} is the
 * kill-switch outcome, distinct from an ordinary {@code DENY}.
 *
 * <p>{@code ASSESS} is a policy authoring kind, frozen in the spec: do not
 * extend, reinterpret, or fold it into the wire decision without explicit
 * sign-off.
 */
public enum Decision {
    ALLOW,
    HOLD,
    DENY,
    HALT,
    ASSESS
}
