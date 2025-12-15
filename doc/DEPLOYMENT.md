# 배포 가이드

## 목차
1. [환경 요구사항](#환경-요구사항)
2. [로컬 개발 환경 설정](#로컬-개발-환경-설정)
3. [Docker를 사용한 배포](#docker를-사용한-배포)
4. [프로덕션 배포](#프로덕션-배포)
5. [환경변수 설정](#환경변수-설정)
6. [모니터링 및 로깅](#모니터링-및-로깅)
7. [트러블슈팅](#트러블슈팅)

## 환경 요구사항

### 필수 요구사항
- **Java**: 17 이상
- **Maven**: 3.6 이상
- **PostgreSQL**: 12 이상
- **Redis**: 6.0 이상
- **Docker**: 20.10 이상 (선택사항)
- **Docker Compose**: 2.0 이상 (선택사항)
- **Node.js**: 20 이상 (프론트엔드)
- **npm**: 9 이상 (프론트엔드)

### 권장 사양
- **CPU**: 2코어 이상
- **메모리**: 4GB 이상
- **디스크**: 20GB 이상 (로그 및 업로드 파일 포함)

## 로컬 개발 환경 설정

### 1. 데이터베이스 및 Redis 실행

```bash
# Docker Compose로 실행
cd ~/travel-app
docker compose up -d postgres redis

# 또는 개별 실행
docker run -d \
  --name travel-postgres \
  -e POSTGRES_DB=travel \
  -e POSTGRES_USER=travel \
  -e POSTGRES_PASSWORD=travel \
  -p 5432:5432 \
  postgres:15-alpine

docker run -d \
  --name travel-redis \
  -p 6379:6379 \
  redis:7-alpine
```

### 2. 환경변수 설정

```bash
# 자동 설정 스크립트 사용 (권장)
./scripts/setup-env.sh dev

# 또는 수동 설정
cp travel-backend/env.example .env
# .env 파일을 편집하여 필요한 값 수정

# 환경변수 검증
./scripts/validate-env.sh dev
```

### 3. 백엔드 실행

```bash
cd travel-backend

# 개발 환경으로 실행
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 또는 JAR 파일로 실행
mvn clean package
java -jar target/travel-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### 4. 프론트엔드 실행

```bash
cd travel-front
npm install
npm run dev
```

## Docker를 사용한 배포

### 전체 스택 실행

```bash
# 프로젝트 루트에서
docker compose up -d

# 로그 확인
docker compose logs -f backend

# 중지
docker compose down

# 데이터까지 삭제
docker compose down -v
```

### 개별 서비스 실행

```bash
# 데이터베이스만 실행
docker compose up -d postgres redis

# 백엔드만 실행
docker compose up -d backend
```

### 환경변수 설정

`.env` 파일을 프로젝트 루트에 생성:

```bash
# .env 파일
POSTGRES_DB=travel
POSTGRES_USER=travel
POSTGRES_PASSWORD=your-secure-password
POSTGRES_PORT=5432

REDIS_PORT=6379

JWT_SECRET=your-very-long-secret-key-minimum-64-characters
UUID_CRYPTO_SECRET=your-32-character-secret-key

SPRING_PROFILES_ACTIVE=prod

FILE_UPLOAD_PATH=/app/upload/file/
IMAGE_UPLOAD_PATH=/app/upload/image/
THUMBNAIL_UPLOAD_PATH=/app/upload/thumbnail/

BACKEND_PORT=8080
```

## 프로덕션 배포

### 1. Docker 이미지 빌드

```bash
cd travel-backend

# 이미지 빌드
docker build -t travel-backend:latest .

# 태그 지정
docker tag travel-backend:latest your-registry/travel-backend:1.0.0

# 레지스트리에 푸시
docker push your-registry/travel-backend:1.0.0
```

### 2. 프로덕션 환경변수 설정

프로덕션 서버에서 `.env` 파일 생성:

```bash
# 필수 환경변수
JWT_SECRET=매우_긴_랜덤_시크릿_키_최소_64자_이상
UUID_CRYPTO_SECRET=32자_정확한_암호화_키
DB_PASSWORD=매우_강력한_데이터베이스_비밀번호
REDIS_PASSWORD=redis_비밀번호

# 프로덕션 설정
SPRING_PROFILES_ACTIVE=prod
SSL_ENABLED=true
CORS_ALLOWED_ORIGINS=https://your-domain.com
```

### 3. Docker Compose로 배포

```bash
# 프로덕션 설정으로 실행
docker compose -f docker-compose.yml up -d

# 헬스체크 확인
curl http://localhost:8080/actuator/health
```

### 4. Nginx 리버스 프록시 설정 (선택사항)

```nginx
# /etc/nginx/sites-available/travel-app
server {
    listen 80;
    server_name api.your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 5. SSL 인증서 설정

```bash
# Let's Encrypt 사용
sudo certbot --nginx -d api.your-domain.com

# 또는 수동 설정
# SSL 인증서를 서버에 복사 후 application-prod.properties에 경로 설정
```

## 환경변수 설정

### 필수 환경변수 (프로덕션)

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `JWT_SECRET` | JWT 서명 키 | 최소 64자 랜덤 문자열 |
| `UUID_CRYPTO_SECRET` | UUID 암호화 키 | 정확히 32자 |
| `DB_PASSWORD` | 데이터베이스 비밀번호 | 강력한 비밀번호 |
| `DB_URL` | 데이터베이스 연결 URL | jdbc:postgresql://... |
| `REDIS_HOST` | Redis 호스트 | localhost 또는 IP |
| `REDIS_PASSWORD` | Redis 비밀번호 | (선택사항) |

### 선택 환경변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `SERVER_PORT` | 서버 포트 | 8080 |
| `SSL_ENABLED` | HTTPS 활성화 | false |
| `CORS_ALLOWED_ORIGINS` | 허용된 CORS 오리진 | - |
| `LOG_LEVEL` | 로그 레벨 | INFO |
| `RATE_LIMIT_API_COUNT` | API Rate Limit | 100 |
| `MAX_FILE_SIZE` | 최대 파일 크기 | 10MB |

## 모니터링 및 로깅

### 헬스체크

```bash
# 기본 헬스체크
curl http://localhost:8080/actuator/health

# 상세 정보
curl http://localhost:8080/api/monitoring/status
```

### 로그 확인

```bash
# Docker 로그
docker compose logs -f backend

# 파일 로그
tail -f travel-backend/logs/travel-backend.log

# JSON 로그
tail -f travel-backend/logs/travel-backend.json
```

### 메트릭 수집

```bash
# Prometheus 메트릭
curl http://localhost:8080/actuator/prometheus

# 커스텀 메트릭
curl http://localhost:8080/api/monitoring/metrics
```

## 데이터베이스 마이그레이션

### Flyway 마이그레이션

프로젝트는 Flyway를 사용하여 데이터베이스 스키마를 관리합니다.

```bash
# 마이그레이션 상태 확인
mvn flyway:info

# 마이그레이션 실행
mvn flyway:migrate

# 마이그레이션 되돌리기 (주의: 프로덕션에서는 사용 금지)
mvn flyway:repair
```

### 마이그레이션 스크립트 작성

새 마이그레이션 스크립트는 `travel-backend/src/main/resources/db/migration/` 디렉토리에 추가합니다.

파일명 형식: `V{version}__{description}.sql`

예시:
- `V1__Initial_schema.sql`
- `V2__Add_comments.sql`
- `V3__Add_user_roles.sql`

## 트러블슈팅

### 데이터베이스 연결 실패

```bash
# 연결 확인
docker compose exec postgres psql -U travel -d travel -c "SELECT 1;"

# 로그 확인
docker compose logs postgres
```

### Redis 연결 실패

```bash
# 연결 확인
docker compose exec redis redis-cli ping

# 로그 확인
docker compose logs redis
```

### 포트 충돌

```bash
# 포트 사용 확인
lsof -i :8080
netstat -an | grep 8080

# docker-compose.yml에서 포트 변경
```

### 메모리 부족

```bash
# JVM 메모리 설정 조정
# Dockerfile 또는 환경변수에서
JAVA_OPTS="-Xmx512m -Xms256m"
```

### 캐시 문제

```bash
# Redis 캐시 초기화
docker compose exec redis redis-cli FLUSHALL
```

### 마이그레이션 문제

```bash
# 마이그레이션 상태 확인
mvn flyway:info

# 마이그레이션 수정 (checksum 오류 시)
mvn flyway:repair

# 특정 버전으로 마이그레이션
mvn flyway:migrate -Dflyway.target=2
```

## 백업 및 복구

### 데이터베이스 백업

```bash
# 백업
docker compose exec postgres pg_dump -U travel travel > backup.sql

# 복구
docker compose exec -T postgres psql -U travel travel < backup.sql
```

### 업로드 파일 백업

```bash
# 백업
tar -czf upload-backup.tar.gz travel-backend/upload/

# 복구
tar -xzf upload-backup.tar.gz
```

## 업데이트 절차

1. **백업 생성**
   ```bash
   docker compose exec postgres pg_dump -U travel travel > backup-$(date +%Y%m%d).sql
   ```

2. **마이그레이션 확인**
   ```bash
   mvn flyway:info
   ```

3. **새 이미지 빌드**
   ```bash
   docker build -t travel-backend:new-version .
   ```

4. **롤링 업데이트**
   ```bash
   docker compose up -d --no-deps backend
   ```

5. **마이그레이션 자동 실행**
   - 애플리케이션 시작 시 Flyway가 자동으로 마이그레이션을 실행합니다
   - 수동 실행이 필요한 경우:
     ```bash
     docker compose exec backend mvn flyway:migrate
     ```

6. **헬스체크 확인**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

7. **롤백 (필요시)**
   ```bash
   docker compose down
   docker compose up -d
   ```

## 보안 체크리스트

- [ ] JWT_SECRET이 강력한 랜덤 문자열인지 확인
- [ ] 데이터베이스 비밀번호가 강력한지 확인
- [ ] HTTPS가 활성화되어 있는지 확인
- [ ] CORS 설정이 올바른지 확인
- [ ] 불필요한 Actuator 엔드포인트가 비활성화되어 있는지 확인
- [ ] 파일 업로드 경로가 안전한지 확인
- [ ] 로그에 민감한 정보가 포함되지 않는지 확인

