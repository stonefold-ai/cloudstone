// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.cloudstone.kernel.stele;

/**
 * Parses Stele source (YAML) into a {@link SteleDocument}, rejecting
 * documents that don't conform to the grammar. Parse errors must carry
 * source positions — policy authors debug through these messages.
 */
public final class SteleParser {

    public SteleDocument parse(String source) {
        // TODO: implement against the normative grammar in ../spec
        throw new UnsupportedOperationException("not implemented yet");
    }
}
