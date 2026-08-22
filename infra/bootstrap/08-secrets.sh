#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

source "$SCRIPT_DIR/../config.env"
source "$SCRIPT_DIR/../outputs.env"
source "$SCRIPT_DIR/../lib/helpers.sh"

if [[ -f "$SCRIPT_DIR/../secrets.env" ]]; then
    source "$SCRIPT_DIR/../secrets.env"
fi

if [[ -f "$SCRIPT_DIR/../supabase.env" ]]; then
    source "$SCRIPT_DIR/../supabase.env"
fi

OUTPUT_FILE="$SCRIPT_DIR/../outputs.env"

DB_PROVIDER="${DB_PROVIDER:-rds}"

SECRET_NAME="${PROJECT_NAME}/database"

# Resolve the active PostgreSQL connection values from the provider selected
# in config.env. This is the single point where "supabase" vs "rds" branches;
# everything downstream (Secrets Manager, IAM, ECS task definition) stays
# provider-agnostic because it only ever sees DB_HOST/DB_PORT/DB_NAME/
# DB_USER/DB_PASSWORD.
resolve_connection_values() {

    if [[ "$DB_PROVIDER" == "supabase" ]]; then

        echo "DB_PROVIDER=supabase — reading infra/supabase.env"

        for VAR in SUPABASE_DB_HOST SUPABASE_DB_PORT SUPABASE_DB_NAME SUPABASE_DB_USER SUPABASE_DB_PASSWORD; do
            if [[ -z "${!VAR:-}" ]]; then
                echo "Missing $VAR. Copy infra/supabase.env.example to infra/supabase.env and fill it in."
                exit 1
            fi
        done

        RESOLVED_DB_HOST="$SUPABASE_DB_HOST"
        RESOLVED_DB_PORT="$SUPABASE_DB_PORT"
        RESOLVED_DB_NAME="$SUPABASE_DB_NAME"
        RESOLVED_DB_USER="$SUPABASE_DB_USER"
        RESOLVED_DB_PASSWORD="$SUPABASE_DB_PASSWORD"

    elif [[ "$DB_PROVIDER" == "rds" ]]; then

        echo "DB_PROVIDER=rds — reading RDS outputs/secrets.env"

        if [[ -z "${RDS_PASSWORD:-}" ]]; then
            echo "Cannot resolve RDS connection values."
            echo "RDS_PASSWORD not found in secrets.env"
            exit 1
        fi

        RESOLVED_DB_HOST=$(require_output RDS_ENDPOINT)
        RESOLVED_DB_PORT="$DB_PORT"
        RESOLVED_DB_NAME="$DB_NAME"
        RESOLVED_DB_USER="$DB_USERNAME"
        RESOLVED_DB_PASSWORD="$RDS_PASSWORD"

    else
        echo "Unknown DB_PROVIDER: $DB_PROVIDER (expected 'supabase' or 'rds')"
        exit 1
    fi
}

create_or_update_secret() {

    local SECRET_ARN

    if aws secretsmanager describe-secret \
        --region "$AWS_REGION" \
        --secret-id "$SECRET_NAME" >/dev/null 2>&1; then

        SECRET_ARN=$(aws secretsmanager describe-secret \
            --region "$AWS_REGION" \
            --secret-id "$SECRET_NAME" \
            --query ARN \
            --output text)

    else
        SECRET_ARN=""
    fi

    SECRET_VALUE=$(jq -n \
        --arg username "$RESOLVED_DB_USER" \
        --arg password "$RESOLVED_DB_PASSWORD" \
        --arg host "$RESOLVED_DB_HOST" \
        --arg port "$RESOLVED_DB_PORT" \
        --arg dbname "$RESOLVED_DB_NAME" \
    '{
        "DB_USER": $username,
        "DB_PASSWORD": $password,
        "DB_HOST": $host,
        "DB_PORT": $port,
        "DB_NAME": $dbname
    }')

    if [[ -z "$SECRET_ARN" || "$SECRET_ARN" == "None" ]]; then

        echo "Creating secret..."

        SECRET_ARN=$(aws secretsmanager create-secret \
            --region "$AWS_REGION" \
            --name "$SECRET_NAME" \
            --description "Database credentials for ${PROJECT_NAME} (provider: ${DB_PROVIDER})" \
            --secret-string "$SECRET_VALUE" \
            --tags \
                Key=Project,Value="$PROJECT_NAME" \
                Key=Environment,Value="$ENVIRONMENT" \
                Key=ManagedBy,Value="$MANAGED_BY" \
            --query "ARN" \
            --output text)

    else

        echo "Secret already exists. Updating value for provider=${DB_PROVIDER}..."

        aws secretsmanager put-secret-value \
            --region "$AWS_REGION" \
            --secret-id "$SECRET_NAME" \
            --secret-string "$SECRET_VALUE" \
            >/dev/null

        aws secretsmanager tag-resource \
            --region "$AWS_REGION" \
            --secret-id "$SECRET_NAME" \
            --tags \
                Key=Project,Value="$PROJECT_NAME" \
                Key=Environment,Value="$ENVIRONMENT" \
                Key=ManagedBy,Value="$MANAGED_BY" \
                Key=DbProvider,Value="$DB_PROVIDER" \
            >/dev/null

    fi

    # Kept as RDS_SECRET_ARN on purpose: this is the existing output/GitHub
    # secret name already wired through 09-iam.sh, the ECS task definition
    # template, and the deploy workflow. Renaming it would require also
    # renaming the GitHub environment secret for zero functional benefit —
    # the value itself is always the ARN of the generic "${PROJECT_NAME}/database"
    # secret, regardless of which provider is active.
    append_output "RDS_SECRET_ARN" "$SECRET_ARN"

}

remove_password_from_outputs() {

    grep -v "^RDS_PASSWORD=" "$OUTPUT_FILE" \
        > "${OUTPUT_FILE}.tmp" || true

    mv "${OUTPUT_FILE}.tmp" "$OUTPUT_FILE"

}

resolve_connection_values

create_or_update_secret

remove_password_from_outputs

echo "Secret ready (provider: ${DB_PROVIDER})."
