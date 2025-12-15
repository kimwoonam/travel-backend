#!/bin/bash

# 환경변수 검증 스크립트
# 사용법: ./scripts/validate-env.sh [dev|prod|test]

ENV=${1:-dev}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "Validating environment: $ENV"

# .env 파일 로드
if [ -f "$PROJECT_ROOT/.env" ]; then
    set -a
    source "$PROJECT_ROOT/.env"
    set +a
else
    echo "Error: .env file not found"
    exit 1
fi

ERRORS=0

# 공통 검증
if [ -z "$JWT_SECRET" ]; then
    echo "ERROR: JWT_SECRET is not set"
    ERRORS=$((ERRORS + 1))
elif [ ${#JWT_SECRET} -lt 32 ]; then
    echo "WARNING: JWT_SECRET should be at least 32 characters long"
fi

if [ -z "$UUID_CRYPTO_SECRET" ]; then
    echo "ERROR: UUID_CRYPTO_SECRET is not set"
    ERRORS=$((ERRORS + 1))
elif [ ${#UUID_CRYPTO_SECRET} -ne 32 ]; then
    echo "ERROR: UUID_CRYPTO_SECRET must be exactly 32 characters"
    ERRORS=$((ERRORS + 1))
fi

# 환경별 검증
case $ENV in
  prod)
    if [ -z "$DB_PASSWORD" ] || [ "$DB_PASSWORD" = "travel" ]; then
        echo "ERROR: DB_PASSWORD must be set to a secure value in production"
        ERRORS=$((ERRORS + 1))
    fi
    
    if [ -z "$CORS_ALLOWED_ORIGINS" ]; then
        echo "WARNING: CORS_ALLOWED_ORIGINS is not set"
    fi
    
    if [ "$JWT_SECRET" = "dev-secret-key-for-development-only-change-in-production" ]; then
        echo "ERROR: JWT_SECRET must be changed from default value in production"
        ERRORS=$((ERRORS + 1))
    fi
    ;;
esac

if [ $ERRORS -eq 0 ]; then
    echo "Environment validation passed!"
    exit 0
else
    echo "Environment validation failed with $ERRORS error(s)"
    exit 1
fi
