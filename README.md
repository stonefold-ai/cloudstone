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

Requires JDK 17+ (21 recommended) and Maven.

```
mvn package
cd gateway-server && mvn spring-boot:run
```

On machines with TLS-intercepting antivirus/proxy, point the JVM at the
Windows certificate store instead of the JDK truststore:

```
MAVEN_OPTS="-Djavax.net.ssl.trustStoreType=Windows-ROOT" mvn package
```

The default `local` profile uses file-backed H2 (state in `.local/`) and a
config-seeded tenant registry. Health: `http://localhost:8080/actuator/health`.

## Running cloud-shaped

```
docker compose up --build
```

Gateway + Postgres. The `cloud` profile takes all configuration from the
environment (`GATEWAY_DB_URL`, `GATEWAY_DB_USER`, `GATEWAY_DB_PASSWORD`);
the image contains no secrets. Kubernetes probes are exposed at
`/actuator/health/liveness` and `/actuator/health/readiness`, metrics at
`/actuator/prometheus`.

## Next steps (roughly in order)

1. Bring `InterceptedAction` / route shapes in line with the spec wire format.
2. Implement `SteleParser` + `SteleCompiler` against the normative grammar.
3. Implement JDBC audit log, ledger (atomic debit), session store, tenant registry.
4. Seed the local tenant registry from configuration properties (the auth
   filter itself is in place: bearer key → tenant, fail closed, uniform 401).
5. Wire the hot path in `InterceptController`: session → policy → audit → ledger → forward/refuse.
6. Run the TCK against `gateway-server` and burn down failures.
