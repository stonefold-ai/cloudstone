# Cloudstone

Cloudstone is the production-grade, multitenant Java implementation of the
Stonefold gateway.
Status: **skeleton** — module boundaries and interfaces are laid out, most
implementations throw `UnsupportedOperationException`.

The [spec repo](../spec) is normative for protocol shapes, the Stele grammar
and decision semantics. The Python implementation in [`../stonefold`](../stonefold)
is the reference; on divergence, the spec wins. Every behaviour implemented
here must eventually be covered by a TCK check.

## Modules

| Module | Depends on | Purpose |
|---|---|---|
| `gateway-core` | — | Framework-free domain model: tenants, sessions, intercepted actions, policy decisions, audit log and ledger interfaces. |
| `gateway-stele` | core | Stele policy language: parse, validate, compile to an executable `PolicyEngine`. |
| `gateway-tenancy` | core | Tenant registry, credential resolution, per-tenant configuration and limits. |
| `gateway-persistence` | core, tenancy | JDBC implementations (Postgres / H2), Flyway migrations. |
| `gateway-server` | all | Spring Boot executable: HTTP boundary, tenant auth filter, wiring, observability. |

Design rules the skeleton encodes:

- **Deterministic machinery.** Policy evaluation is pure and deterministic;
  no model is ever consulted to decide allow/deny. Ledger debits are atomic
  at the database so budgets remain hard bounds across replicas.
- **Tenant isolation everywhere.** Tenant id is resolved once in a filter,
  is the first column of every table, and scopes every interface method.
  There is no default tenant.
- **Audit before release.** An action's audit event is written before its
  result is released. The audit table is append-only.

## Running locally (no external services)

Requires JDK 17+ (21 recommended). The Maven wrapper downloads the pinned
Maven version on first use (`mvnw.cmd` on Windows).

```
./mvnw package
cd gateway-server && ../mvnw spring-boot:run
```

On machines with TLS-intercepting antivirus/proxy, point the JVM at the
Windows certificate store instead of the JDK truststore:

```
MAVEN_OPTS="-Djavax.net.ssl.trustStoreType=Windows-ROOT" ./mvnw package
```

The default `local` profile uses file-backed H2 (state in `.local/`) and a
config-seeded tenant registry. Health: `http://localhost:8080/actuator/health`.
While the schema is unreleased, editing a migration invalidates the dev
database's Flyway checksums — delete `gateway-server/.local/` and restart.

On first start with no tenants configured, the gateway auto-seeds a `dev`
tenant and prints its API key once to the console — copy it into your
caller's environment (`Authorization: Bearer <key>`). It is not stored and
cannot be recovered; restarting with an empty registry mints a new one.

To seed real tenants, generate a key/hash pair and put the **hash** in
configuration (see the commented `stonefold.tenants` block in
`application.yaml`); the plaintext key stays with the caller:

```
java -cp gateway-tenancy/target/classes ai.stonefold.gateway.tenancy.GenerateApiKey
```

A mis-seeded config refuses to boot: duplicate tenant ids, the same key hash
on two tenants, a tenant without key hashes, or a malformed hash value —
including a plaintext key pasted where the hash belongs — are all startup
errors. One misconfiguration the gateway cannot detect: if the whole
`stonefold.tenants` block is misspelled or misplaced, it binds as empty and
the dev auto-seed kicks in — so if you configured tenants but the startup
log shows the auto-seeded `dev` tenant instead of them, fix the config. The
startup log always lists the tenants actually seeded.

The `cloud` profile never auto-seeds and does not yet start at all — its
persistence-backed tenant registry is still to be implemented.

## Running cloud-shaped

```
docker compose up --build
```

Gateway + Postgres. The `cloud` profile takes all configuration from the
environment (`GATEWAY_DB_URL`, `GATEWAY_DB_USER`, `GATEWAY_DB_PASSWORD`);
the image contains no secrets. Kubernetes probes are exposed at
`/actuator/health/liveness` and `/actuator/health/readiness`, metrics at
`/actuator/prometheus`.

## Roadmap

Development proceeds in phases; see [ROADMAP.md](ROADMAP.md) for the current
phase and how design documentation is published as behaviour ships.

## License

[Apache License 2.0](LICENSE). Provided as is, without warranty of any kind;
see the LICENSE for the full disclaimer.
