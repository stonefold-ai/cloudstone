package ai.stonefold.gateway.core.policy;

/**
 * Outcome kinds a policy evaluation can produce. The authoritative list and
 * semantics live in the spec repo (spec wins on divergence). NOTE: the
 * "assess" kind is frozen in the spec — do not extend or reinterpret it here
 * without explicit sign-off.
 */
public enum Decision {
    ALLOW,
    DENY,
    ASSESS
}
