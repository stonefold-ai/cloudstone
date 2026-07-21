// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.cloudstone.kernel.stele;

import java.util.List;
import java.util.Map;

/**
 * Parsed but not yet compiled Stele policy document. Shape is a placeholder;
 * fields must be brought in line with the Stele grammar in the spec repo
 * before this module leaves skeleton status.
 */
public record SteleDocument(
        String version,
        String name,
        List<Map<String, Object>> rules) {
}
