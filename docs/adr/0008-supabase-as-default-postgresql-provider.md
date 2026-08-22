# ADR-0008: Supabase as the Default Managed PostgreSQL Provider

- **Status:** Accepted
- **Date:** 2026-08-21

## Context

BFlow is a pre-revenue SaaS. Amazon RDS (even a single-AZ `db.t4g.micro`)
carries a fixed monthly cost from the moment it is provisioned, regardless
of traffic. For a project with no paying customers yet, that fixed cost is
not justified while a managed Postgres provider with a usable free tier
(Supabase) can serve the same role during this stage.

At the same time, the project must not lose the ability to move to RDS
later — e.g. once traffic, compliance, or VPC-isolation requirements justify
it — without a rewrite.

ADR-0005 already established that infrastructure is provisioned through
modular, idempotent Bash scripts, with `config.env` holding user-defined
configuration and `outputs.env` holding generated identifiers. That design
made this change straightforward: the database connection was already fully
externalized into generic `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` /
`DB_PASSWORD` values consumed via Spring's standard `spring.datasource.*`
properties, sourced from a single AWS Secrets Manager secret
(`${PROJECT_NAME}/database`). Nothing in the application, the ECS task
definition, or the IAM policy grants was ever coupled to "RDS" beyond that
secret's contents.

## Decision

`config.env` gains one new variable, `DB_PROVIDER` (`supabase` | `rds`).

`infra/bootstrap/08-secrets.sh` is the single script that branches on it: it
resolves connection values either from `infra/supabase.env` (Supabase) or
from the existing RDS outputs/`secrets.env` (RDS), then creates or **updates
in place** the same generic Secrets Manager secret. `infra/deploy.sh` skips
`infra/bootstrap/07-rds.sh` when the provider is not `rds`.

No other script is modified. `07-rds.sh` and every `infra/destroy/*.sh`
script are untouched and remain fully functional — `destroy/07-rds.sh`
already no-ops safely when no RDS instance exists, so no RDS capability was
removed from the repository, only left unprovisioned by default.

Explicitly out of scope for this change: Supabase Auth, Supabase Storage,
and the Supabase client SDK. Supabase is used strictly as managed
PostgreSQL, reached over the standard `postgresql://` JDBC URL. Cognito,
S3, SES, and the ECS/ECR/CloudWatch/IAM/OIDC layers are unaffected.

## Consequences

### Positive

- $0 additional AWS cost for the database layer while pre-revenue (Supabase
  free tier), versus a fixed RDS bill from day one.
- Spring Boot, Flyway, ECS, and IAM remain completely unaware of which
  provider is active — the abstraction ADR-0005 already provided turned out
  to be sufficient, so no new abstraction layer was introduced.
- Reverting to RDS is a config change plus rerunning two scripts
  (`07-rds.sh`, `08-secrets.sh`), documented step by step in
  `infra/README.md`. No downtime-inducing IAM/ECS/task-definition edits are
  needed because the secret ARN never changes.
- ECS tasks already run in public subnets with `assignPublicIp: ENABLED`
  (for the existing Cloudflare dynamic DNS flow), so outbound connectivity
  to Supabase's public endpoint requires no NAT Gateway, no VPC peering, and
  no security group changes.

### Negative

- Supabase-managed Postgres is outside the VPC, so the network-level
  isolation RDS provided (private subnet, security-group-only ingress) does
  not apply to this provider. This is an accepted trade-off for the
  pre-revenue stage; DB credentials still flow only through Secrets Manager
  and TLS (`sslmode=require`) is enforced on the connection.
- Data migration between the two Postgres instances (`pg_dump`/`pg_restore`
  or logical replication) is a manual step, not automated by this toggle.
- Two credential files now exist locally (`secrets.env` for RDS,
  `supabase.env` for Supabase); both are gitignored, but this is one more
  file for a developer to keep track of than a single-provider setup would
  need.

## Implementation Notes

- `infra/config.env.example` documents `DB_PROVIDER` with inline comments.
- `infra/supabase.env.example` mirrors the existing `secrets.env` pattern
  used for the RDS master password: copy it, fill in real values, never
  commit it.
- The Secrets Manager output/GitHub secret name `RDS_SECRET_ARN` was
  deliberately **not** renamed. It is plumbing internal to the bootstrap
  scripts, IAM policy, and ECS task definition template, not
  application-facing configuration — renaming it would require manually
  recreating a GitHub environment secret for no functional benefit.
