#!/bin/bash

# 환경변수 설정 스크립트
# 사용법: ./scripts/setup-env.sh [dev|prod|test]

ENV=${1:-dev}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "Setting up environment: $ENV"

# .env 파일 생성
ENV_FILE="$PROJECT_ROOT/.env"

case $ENV in
  dev)
    cat > "$ENV_FILE" << EOF
# Development Environment Variables
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:postgresql://127.0.0.1:5432/travel
DB_USER=travel
DB_PASSWORD=travel
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=dev-secret-key-for-development-only-change-in-production
UUID_CRYPTO_SECRET=dev-uuid-secret-key-32bit
FILE_UPLOAD_PATH=./upload/file/
IMAGE_UPLOAD_PATH=./upload/image/
THUMBNAIL_UPLOAD_PATH=./upload/thumbnail/
EOF
    ;;
  prod)
    echo "Production environment requires manual configuration."
    echo "Please set the following environment variables:"
    echo "  - JWT_SECRET (minimum 64 characters)"
    echo "  - UUID_CRYPTO_SECRET (exactly 32 characters)"
    echo "  - DB_PASSWORD"
    echo "  - REDIS_PASSWORD (optional)"
    echo "  - CORS_ALLOWED_ORIGINS"
    exit 1
    ;;
  test)
    cat > "$ENV_FILE" << EOF
# Test Environment Variables
SPRING_PROFILES_ACTIVE=test
DB_URL=jdbc:h2:mem:testdb
DB_USER=sa
DB_PASSWORD=
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=test-secret-key-for-testing-only
UUID_CRYPTO_SECRET=test-uuid-secret-key-32
EOF
    ;;
  *)
    echo "Unknown environment: $ENV"
    echo "Usage: $0 [dev|prod|test]"
    exit 1
    ;;
esac

echo "Environment file created at: $ENV_FILE"
echo "Please review and update the values as needed."
