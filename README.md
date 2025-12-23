# travel-backend

## 개요

* Cursor AI를 통한 게시판 FrontEnd/BackEnd 개발

## github

* frontend : https://github.com/kimwoonam/travel-front
* backend : https://github.com/kimwoonam/travel-backend

## 개발스킬

### FrontEnd

* @types/react: "^18.3.5"
* @types/react-dom: "^18.3.0"
* @vitejs/plugin-react: "^4.3.1"
* typescript: "^5.5.4"
* vite: "^5.3.4"

### BackEnd

* JDK 21
* Spring Boot 3.3.3
    * web
    * jpa
    * security
* jjwt
* maven 3.11

### DBMS

* PostgreSQL 16
* Docker를 사용하여 생성함

### ETC
* REDIS


## 구조
```bash
├── java
│   │   ├── com/moodo/travel
│   │   │   ├── board
│   │   │   │   ├── Board.java
│   │   │   │   ├── BoardController.java
│   │   │   │   ├── BoardRepository.java
│   │   │   │   └── BoardService.java
│   │   │   ├── config
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── JwtInterceptor.java
│   │   │   │   ├── JwtUtil.java
│   │   │   │   ├── UuidCryptoUtil.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── account
│   │   │   │   ├── dto
│   │   │   │   │   └── AuthDtos.java // Jwt Respose DTO
│   │   │   │   ├── User.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── UserService.java
│   │   │   └── TravelApplication.java
├── resources
│   └── application.properties
```

## Backend (Spring Boot)
- Path: `travel-backend`
- Config: `src/main/resources/application-{profile}.properties`
- Profiles: `dev`, `prod`, `test`
- Endpoints:
  - POST `/api/auth/signup` { email, password, displayName }
  - POST `/api/auth/login` { email, password }
  - DELETE `/api/auth/delete?email=...&password=...`
  - GET `/api/board` (페이징 지원)

## Database (PostgreSQL via Docker)
- Run: `docker compose up -d` in project root
- Default creds: user `travel`, password `travel`, db `travel`

## Run locally

### 방법 1: Docker Compose (권장)
```bash
# 전체 스택 실행
cd ~/travel-app
docker compose up -d

# 로그 확인
docker compose logs -f backend
```

### 방법 2: 개별 실행
1. Start DB & Redis
   ```bash
   cd ~/travel-app
   docker compose up -d postgres redis
   ```
2. Start backend
   ```bash
   cd ~/travel-app/travel-backend
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```
3. Start frontend
   ```bash
   cd ~/travel-app/travel-front
   npm install
   npm run dev
   ```

### 환경변수 설정
프론트엔드 `.env` 파일에 `VITE_API_BASE` 설정 (기본값: `http://127.0.0.1:8080`)

## API Documentation

### 📚 Swagger/OpenAPI
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs (JSON)**: `http://localhost:8080/api-docs`
- **API Docs (YAML)**: `http://localhost:8080/api-docs.yaml`

### 🔐 API 인증
Swagger UI에서 API를 테스트하려면:
1. Swagger UI 페이지 접속
2. 우측 상단의 **Authorize** 버튼 클릭
3. JWT 토큰 입력 (형식: `Bearer {token}`)
4. 로그인 API로 토큰을 먼저 발급받아야 합니다

## Docker Deployment

### 🐳 Docker Compose로 전체 스택 실행

```bash
# 프로젝트 루트에서
docker compose up -d

# 로그 확인
docker compose logs -f backend

# 중지
docker compose down
```

### 📦 개별 Docker 이미지 빌드

```bash
cd travel-backend
docker build -t travel-backend:latest .
docker run -p 8080:8080 travel-backend:latest
```

## Environment Configuration

### 🌍 환경별 설정 파일

프로젝트는 환경별로 설정 파일을 분리하여 관리합니다:

- **`application.properties`**: 기본 설정 (프로파일 선택)
- **`application-common.properties`**: 공통 설정 (모든 환경)
- **`application-dev.properties`**: 개발 환경 설정
- **`application-prod.properties`**: 프로덕션 환경 설정
- **`application-test.properties`**: 테스트 환경 설정

### 🔧 프로파일 활성화

```bash
# 개발 환경
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 프로덕션 환경
java -jar app.jar --spring.profiles.active=prod

# 환경변수로 설정
export SPRING_PROFILES_ACTIVE=prod
```

### 📝 환경변수 설정

환경변수 설정 스크립트를 사용할 수 있습니다:

```bash
# 개발 환경 설정
./scripts/setup-env.sh dev

# 환경변수 검증
./scripts/validate-env.sh dev
```

각 환경별로 필요한 환경변수는 `travel-backend/env.example` 파일을 참조하세요.

## Database Migration

### 🗄️ Flyway 마이그레이션

프로젝트는 Flyway를 사용하여 데이터베이스 스키마를 관리합니다.

```bash
# 마이그레이션 실행 (애플리케이션 시작 시 자동 실행)
mvn spring-boot:run

# 마이그레이션 상태 확인
mvn flyway:info

# 마이그레이션 수동 실행
mvn flyway:migrate
```

마이그레이션 스크립트는 `travel-backend/src/main/resources/db/migration/` 디렉토리에 있습니다.

## CI/CD Pipeline

### 🔄 GitHub Actions

프로젝트는 GitHub Actions를 사용하여 CI/CD 파이프라인을 구성합니다:

- **백엔드 빌드 및 테스트**: Maven을 사용한 빌드 및 단위 테스트
- **프론트엔드 빌드 및 테스트**: npm을 사용한 빌드 및 테스트
- **Docker 이미지 빌드**: 자동으로 Docker 이미지 빌드 및 레지스트리 푸시
- **Docker Compose 테스트**: 전체 스택 통합 테스트

자세한 내용은 `.github/workflows/ci-cd.yml`을 참조하세요.

자세한 배포 가이드는 [DEPLOYMENT.md](DEPLOYMENT.md)를 참조하세요.

## Security Features

### 🔒 Implemented Security Measures
- **Rate Limiting**: API 요청 빈도 제한 (일반 API: 100회/분, 인증 API: 10회/분)
- **HTTPS 강제**: 프로덕션 환경에서 HTTPS 강제 설정
- **CSRF 보호**: Cross-Site Request Forgery 공격 방지
- **XSS 방지**: Cross-Site Scripting 공격 방지 헤더
- **보안 헤더**: HSTS, CSP, X-Frame-Options 등 보안 헤더 적용
- **글로벌 예외 처리**: 표준화된 에러 응답 및 로깅
- **환경변수 분리**: 민감한 정보 환경변수로 분리

### 🛡️ Security Configuration
1. **환경변수 설정**: `travel-backend/env.example` 파일 참조
2. **프로덕션 보안 설정**:
   ```bash
   export SSL_ENABLED=true
   export JWT_SECRET=매우_긴_랜덤_시크릿_키_최소_64자_이상
   export UUID_CRYPTO_SECRET=32자_정확한_암호화_키
   export DB_PASSWORD=매우_강력한_데이터베이스_비밀번호
   ```

### 📊 Monitoring & Logging
- **Health Check**: `/actuator/health` - 데이터베이스, Redis 상태 확인
- **Application Info**: `/actuator/info` - 애플리케이션 정보
- **Prometheus Metrics**: `/actuator/prometheus` - Prometheus 형식 메트릭
- **Custom Metrics**: `/api/monitoring/metrics` - API 성능 메트릭
- **Application Status**: `/api/monitoring/status` - 애플리케이션 상태
- **System Info**: `/api/monitoring/system` - 시스템 정보
- **JSON Logging**: 구조화된 JSON 로그 (프로덕션 환경)
- **Rate Limit Headers**: `X-RateLimit-Limit`, `X-RateLimit-Remaining`

## Input Validation

### 🔍 Enhanced Validation Features
- **Password Strength**: 최소 8자, 대소문자, 숫자, 특수문자 필수
- **Korean Name Validation**: 한글 2-10자 또는 영문 2-20자만 허용
- **File Upload Security**: 파일 확장자, 크기, 안전성 검증
- **SQL Injection Prevention**: 입력값에서 SQL 인젝션 공격 차단
- **XSS Prevention**: 입력값에서 XSS 공격 차단
- **Input Length Limits**: 모든 입력 필드에 길이 제한 적용

### 🛡️ Validation Rules
- **Email**: 최대 100자, 유효한 이메일 형식
- **Password**: 최대 128자, 복잡도 요구사항 충족
- **Name/Nickname**: 최대 20자, 한글 또는 영문만
- **Board Title**: 최대 255자, 필수 입력
- **Board Content**: 최대 10,000자
- **File Upload**: 최대 10MB, 허용된 확장자만

### 🧪 Validation Testing
```bash
# 검증 로직 테스트 실행
mvn test -Dtest=ValidationTest
```

## Logging & Monitoring

### 📝 Structured Logging (JSON)
- **개발 환경**: 콘솔에 읽기 쉬운 형식으로 출력
- **프로덕션 환경**: JSON 형식으로 로그 파일 저장
- **로그 파일 위치**: `logs/travel-backend.json`
- **로그 로테이션**: 일별, 최대 100MB, 30일 보관

### 📈 Performance Metrics
- **API 요청 카운터**: 엔드포인트별, 메서드별, 상태코드별 요청 수
- **API 응답 시간**: 평균, 최대 응답 시간 측정
- **에러 카운터**: 에러 타입별, 엔드포인트별 에러 수
- **JVM 메모리 메트릭**: 메모리 사용률, 힙 메모리 정보
- **스레드 정보**: 활성 스레드 수

### 🏥 Health Check Endpoints
- **`GET /actuator/health`**: 기본 헬스체크 (데이터베이스, Redis 상태)
- **`GET /api/monitoring/status`**: 상세 애플리케이션 상태
- **`GET /api/monitoring/metrics`**: 실시간 메트릭 정보
- **`GET /api/monitoring/system`**: 시스템 및 JVM 정보

### 🔍 Monitoring Features
- **Prometheus 통합**: `/actuator/prometheus` 엔드포인트 제공
- **커스텀 헬스체크**: 데이터베이스, Redis 연결 상태 확인
- **메모리 모니터링**: 메모리 사용률 90% 이상 시 경고
- **자동 메트릭 수집**: 1분마다 JVM 메트릭 자동 수집

### 📊 Log Levels
- **Root**: INFO (기본)
- **Application**: DEBUG (개발 환경)
- **SQL**: DEBUG (개발 환경만)
- **Spring Security**: INFO

### 🚀 Usage Examples
```bash
# 헬스체크 확인
curl http://localhost:8080/actuator/health

# 메트릭 확인
curl http://localhost:8080/api/monitoring/metrics

# Prometheus 메트릭
curl http://localhost:8080/actuator/prometheus

# 애플리케이션 상태
curl http://localhost:8080/api/monitoring/status
```

## Testing

### 🧪 Test Coverage
- ✅ **단위 테스트**: Service, Controller 레이어 테스트
- ✅ **통합 테스트**: 전체 플로우 테스트
- ✅ **API 테스트**: 엔드포인트 및 인증 테스트
- ✅ **프론트엔드 테스트**: React 컴포넌트 테스트

### 📋 Backend Tests

#### 단위 테스트
```bash
# 모든 테스트 실행
mvn test

# 특정 테스트 클래스 실행
mvn test -Dtest=AccountServiceTest
mvn test -Dtest=AccountControllerTest

# 통합 테스트 실행
mvn test -Dtest=AccountIntegrationTest

# API 테스트 실행
mvn test -Dtest=ApiTest
```

#### 테스트 구조
```
src/test/java/com/moodo/travel/
├── account/
│   ├── AccountServiceTest.java      # Service 단위 테스트
│   └── AccountControllerTest.java   # Controller 단위 테스트
├── integration/
│   └── AccountIntegrationTest.java  # 통합 테스트
├── api/
│   └── ApiTest.java                 # API 엔드포인트 테스트
└── common/
    ├── util/
    │   └── CryptoUtilTest.java      # 유틸리티 테스트
    └── validation/
        └── ValidationTest.java      # 검증 로직 테스트
```

### 📊 Test Examples

#### Service 단위 테스트
- 회원가입 성공/실패 케이스
- 로그인 성공/실패 케이스
- 계정 삭제 및 로그아웃 테스트

#### Controller 단위 테스트
- HTTP 상태 코드 검증
- 응답 본문 검증
- 쿠키 설정 검증

#### 통합 테스트
- 회원가입 → 로그인 → 로그아웃 전체 플로우
- 중복 이메일 검증
- 인증 실패 케이스

#### API 테스트
- 인증 없이 보호된 API 접근
- 유효성 검사 실패 케이스
- CORS 헤더 확인
- Rate Limiting 헤더 확인

## Performance Optimization

### 🚀 Performance Features
- ✅ **페이징 처리**: 게시판 목록 조회 시 페이징 지원
- ✅ **데이터베이스 인덱스**: 자주 조회되는 컬럼에 인덱스 추가
- ✅ **쿼리 최적화**: @Query를 사용한 최적화된 쿼리
- ✅ **캐시 전략**: Redis 기반 캐싱으로 조회 성능 향상
- ✅ **배치 처리**: JPA 배치 처리 설정

### 📄 Pagination
게시판 목록 조회 시 페이징을 지원합니다:

```bash
# 페이징 파라미터
GET /api/board?page=0&size=20&sort=createdAt,desc

# 파라미터 설명
# - page: 페이지 번호 (0부터 시작, 기본값: 0)
# - size: 페이지 크기 (기본값: 20)
# - sort: 정렬 기준 (기본값: createdAt,desc)
```

**응답 형식:**
```json
{
  "content": [...],
  "pageable": {...},
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0
}
```

### 🗄️ Database Indexes
다음 인덱스들이 자동으로 생성됩니다:

**Board 테이블:**
- `idx_board_uuid`: UUID 조회 최적화
- `idx_board_account_uuid`: 계정별 조회 최적화
- `idx_board_created_at`: 생성일 기준 정렬 최적화
- `idx_board_account_created`: 계정별 + 생성일 복합 인덱스

**Account 테이블:**
- `idx_account_email`: 이메일 조회 최적화
- `idx_account_uuid`: UUID 조회 최적화
- `idx_account_created_at`: 생성일 기준 정렬 최적화

**CommonFile 테이블:**
- `idx_common_file_uuid`: UUID 조회 최적화
- `idx_common_file_table`: 테이블별 조회 최적화
- `idx_common_file_delete_yn`: 삭제 여부 필터링 최적화
- `idx_common_file_created_at`: 생성일 기준 정렬 최적화

### 🔍 Query Optimization
- **@Query 사용**: 인덱스를 활용한 최적화된 쿼리
- **EXISTS 쿼리**: COUNT 대신 EXISTS 사용으로 성능 향상
- **정렬 최적화**: 인덱스가 있는 컬럼 기준 정렬
- **배치 처리**: 대량 데이터 처리 시 배치 사이즈 설정

### 💾 Cache Strategy
Redis를 활용한 캐시 전략:

**캐시 영역:**
- `boards`: 게시판 목록 캐시 (TTL: 30분)
- `board`: 게시판 상세 캐시 (TTL: 1시간)

**캐시 무효화:**
- 게시판 생성/수정/삭제 시 자동 캐시 무효화
- `@CacheEvict` 어노테이션으로 일관성 유지

**캐시 키 전략:**
- 목록: `travel:cache:boards:page_{pageNumber}_{pageSize}`
- 상세: `travel:cache:board:{uuid}`
- 계정별: `travel:cache:boards:account_{accountUuid}_{pageNumber}_{pageSize}`

### ⚙️ JPA Optimization
```properties
# 배치 처리 설정
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
```

### 📊 Performance Tips
1. **페이징 사용**: 대량 데이터 조회 시 반드시 페이징 사용
2. **캐시 활용**: 자주 조회되는 데이터는 캐시 활용
3. **인덱스 확인**: 쿼리 성능이 느리면 인덱스 확인
4. **배치 처리**: 대량 데이터 삽입/수정 시 배치 처리 활용

