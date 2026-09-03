create table tenant (
    id bigserial primary key,
    tenant_key varchar(64) not null unique,
    code varchar(40) unique,
    name varchar(255) not null,
    slug varchar(64) not null unique,
    status varchar(40) not null check (status in ('PROVISIONING','READY','ACTIVE','SUSPENDED')),
    mode varchar(20) not null check (mode in ('LEGACY','HYBRID','TENANT_ONLY')),
    default_locale varchar(20) not null default 'id_ID',
    timezone varchar(64) not null default 'Asia/Jakarta',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    version bigint not null default 0
);

create table tenant_domain (
    id bigserial primary key,
    tenant_id bigint not null references tenant(id),
    domain varchar(255) not null,
    normalized_domain varchar(255) not null unique,
    type varchar(20) not null check (type in ('SUBDOMAIN','CUSTOM')),
    status varchar(40) not null check (status in ('ACTIVE','PENDING_VERIFICATION','DISABLED')),
    verification_token_hash varchar(128),
    verified_at timestamptz,
    primary_domain boolean not null default false,
    created_at timestamptz not null default now()
);

create unique index uq_tenant_primary_domain on tenant_domain(tenant_id) where primary_domain;

create table tenant_secret_reference (
    id bigserial primary key,
    tenant_id bigint not null references tenant(id),
    reference_key varchar(128) not null,
    provider varchar(40) not null,
    provider_path varchar(500) not null,
    created_at timestamptz not null default now(),
    unique (tenant_id, reference_key)
);

create table tenant_database (
    id bigserial primary key,
    tenant_id bigint not null references tenant(id),
    database_role varchar(10) not null check (database_role in ('CORE','FILE')),
    jdbc_url varchar(1000) not null check (jdbc_url like 'jdbc:postgresql://%'),
    credential_reference varchar(128) not null,
    read_only boolean not null default true,
    maximum_pool_size integer not null default 4 check (maximum_pool_size between 1 and 20),
    enabled boolean not null default true,
    unique (tenant_id, database_role)
);

create table tenant_module_route (
    id bigserial primary key,
    tenant_id bigint not null references tenant(id),
    module_key varchar(100) not null,
    route_pattern varchar(500) not null,
    route_owner varchar(10) not null check (route_owner in ('LEGACY','NEXT')),
    write_ownership varchar(30) not null check (write_ownership in ('LEGACY_WRITE','NEXT_READ_ONLY','NEXT_SHADOW','NEXT_WRITE','LEGACY_READ_ONLY','RETIRED')),
    version bigint not null default 0,
    unique (tenant_id, module_key, route_pattern)
);

create table tenant_feature_flag (
    id bigserial primary key,
    tenant_id bigint not null references tenant(id),
    flag_key varchar(128) not null,
    enabled boolean not null default false,
    version bigint not null default 0,
    unique (tenant_id, flag_key)
);

create table tenant_api_client (
    id bigserial primary key,
    tenant_id bigint not null references tenant(id),
    client_id varchar(128) not null unique,
    secret_hash varchar(255) not null,
    status varchar(20) not null check (status in ('ACTIVE','DISABLED','REVOKED')),
    created_at timestamptz not null default now()
);

create table tenant_schema_fingerprint (
    id bigserial primary key,
    tenant_id bigint not null references tenant(id),
    database_role varchar(10) not null check (database_role in ('CORE','FILE')),
    algorithm varchar(40) not null,
    fingerprint varchar(255) not null,
    captured_at timestamptz not null default now()
);

create table tenant_migration_state (
    id bigserial primary key,
    tenant_id bigint not null references tenant(id),
    aggregate_key varchar(160) not null,
    write_ownership varchar(30) not null check (write_ownership in ('LEGACY_WRITE','NEXT_READ_ONLY','NEXT_SHADOW','NEXT_WRITE','LEGACY_READ_ONLY','RETIRED')),
    version bigint not null default 0,
    updated_at timestamptz not null default now(),
    unique (tenant_id, aggregate_key)
);

create table security_handoff_nonce (
    id bigserial primary key,
    issuer varchar(128) not null,
    nonce_hash char(64) not null,
    expires_at timestamptz not null,
    consumed_at timestamptz not null default now(),
    unique (issuer, nonce_hash)
);

create index ix_handoff_nonce_expiry on security_handoff_nonce(expires_at);

create table audit_event (
    id bigserial primary key,
    tenant_id bigint not null references tenant(id),
    aggregate_key varchar(255) not null,
    operation varchar(80) not null,
    actor_id varchar(255) not null,
    request_id varchar(128) not null,
    before_state jsonb not null default '{}'::jsonb,
    after_state jsonb not null default '{}'::jsonb,
    rolled_back boolean not null default false,
    occurred_at timestamptz not null default now()
);

create table outbox_event (
    id uuid primary key,
    tenant_id bigint not null references tenant(id),
    aggregate_key varchar(255) not null,
    event_type varchar(255) not null,
    payload jsonb not null,
    occurred_at timestamptz not null default now(),
    published_at timestamptz,
    attempts integer not null default 0,
    last_error_code varchar(100)
);

create table file_saga (
    id uuid primary key,
    tenant_id bigint not null references tenant(id),
    aggregate_key varchar(255) not null,
    state varchar(20) not null check (state in ('PENDING_FILE','STORING','VERIFIED','AVAILABLE','FAILED','ORPHANED')),
    attempts integer not null default 0,
    checksum varchar(255),
    failure_code varchar(100),
    updated_at timestamptz not null default now()
);
