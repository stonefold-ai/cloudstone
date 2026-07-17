-- Initial schema. Written for Postgres; H2 runs in Postgres compatibility
-- mode locally and must accept the very same migration, so types
-- stick to what both engines share. ${json_type} resolves per profile
-- (jsonb on Postgres, text on H2) — see application.yaml.
-- Every tenant-scoped table carries tenant_id as the first indexed column;
-- api_key is the enumerated exception (looked up by hash before the tenant
-- is known).

create table tenant (
    tenant_id         varchar(64)  primary key,
    display_name      varchar(256) not null,
    active_policy_ref varchar(256),
    created_at        timestamp with time zone not null default now()
);

-- One row per active key hash; a tenant may hold several at once, which is
-- what makes zero-downtime rotation possible. The primary key doubles as
-- the lookup index and makes registering the same hash for two tenants
-- impossible.
create table api_key (
    key_hash   varchar(64) primary key,
    tenant_id  varchar(64) not null references tenant (tenant_id),
    created_at timestamp with time zone not null default now(),
    revoked_at timestamp with time zone
);
create index api_key_tenant on api_key (tenant_id);

create table session (
    tenant_id  varchar(64) not null references tenant (tenant_id),
    session_id varchar(64) not null,
    agent_id   varchar(64) not null,
    started_at timestamp with time zone not null,
    closed_at  timestamp with time zone,
    primary key (tenant_id, session_id)
);

-- Append-only. No UPDATE/DELETE grants in production.
create table audit_log (
    seq        bigint generated always as identity primary key,
    tenant_id  varchar(64) not null,
    session_id varchar(64) not null,
    at         timestamp with time zone not null,
    kind       varchar(64) not null,
    body       ${json_type} not null
);
create index audit_log_session on audit_log (tenant_id, session_id, seq);

create table ledger (
    tenant_id varchar(64)   not null,
    account   varchar(128)  not null,
    balance   numeric(20,6) not null,
    primary key (tenant_id, account)
);

create table ledger_entry (
    seq       bigint generated always as identity primary key,
    tenant_id varchar(64)   not null,
    account   varchar(128)  not null,
    amount    numeric(20,6) not null,
    reference varchar(256)  not null,
    at        timestamp with time zone not null default now()
);
create index ledger_entry_account on ledger_entry (tenant_id, account, seq);
