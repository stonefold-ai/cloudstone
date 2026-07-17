package ai.stonefold.gateway.tenancy;

/**
 * Command-line helper: prints a freshly generated API key and its hash.
 * The hash goes into the gateway's tenant configuration; the key goes to
 * the caller's secret store and is never seen by the gateway again.
 *
 * Exists so that minting a proper full-entropy key is easier than
 * improvising one — the security of the scheme depends on keys coming
 * from a CSPRNG, and the easy path must be the correct path.
 */
public final class GenerateApiKey {

    private GenerateApiKey() {
    }

    public static void main(String[] args) {
        String key = ApiKeys.generate();
        System.out.println("api key (give to the caller, never stored): " + key);
        System.out.println("key hash (put in gateway tenant config):    " + ApiKeys.hash(key));
    }
}
