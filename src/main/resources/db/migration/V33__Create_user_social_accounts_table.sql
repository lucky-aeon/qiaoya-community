-- Create third-party social accounts table for OAuth bindings
-- PostgreSQL syntax; soft delete enabled via deleted boolean

create table if not exists user_social_accounts (
  id                 varchar(36) primary key,
  user_id            varchar(36) not null,
  provider           varchar(32) not null,
  open_id            varchar(128) not null,
  login              varchar(128),
  avatar_url         varchar(512),
  access_token_enc   varchar(512),
  refresh_token_enc  varchar(512),
  expires_at         timestamp,
  create_time        timestamp not null default now(),
  update_time        timestamp not null default now(),
  deleted            boolean not null default false
);

create index if not exists idx_user_social_user_id on user_social_accounts (user_id);

-- Partial unique index to enforce (provider, open_id) uniqueness on non-deleted rows
create unique index if not exists uniq_user_social_provider_open
  on user_social_accounts (provider, open_id)
  where deleted = false;

