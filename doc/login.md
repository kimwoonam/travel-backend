# Login

## 개요

* 로그인 프로세스를 정리한 문서입니다.

## 설명
* 로그인 성공 시 JWT 생성하며 SESSION 대체로 사용한다.
* JWT에는 EMAIL, DISPLAY_NAME, JWT 유효시간 저장된다.
* REDIS에 JWT를 저장한다.
    * 실제로 서버에서 생성한 JWT인지 검증용으로 사용한다.
* 로그인 성공시 EMAIL, NAME, JWT를 프론트엔드에 반환한다.
* 백엔드에서 받은 데이터를 기반으로 프론트엔드에서 쿠키로 생성하여 등록한다.

## 구조

### REQUEST

* URL: /api/auth/login
* METHOD: POST
* HEADER: {'Content-Type': 'application/json'}
* PAYLOAD
    * EMAIL
    * PASSWORD

### RESPONSE
#### 성공

* CODE: 200
* MESSAGE: OK
* TOKEN
* EMAIL
* DISPLAY_NAME

#### 실패

* CODE: 401
* MESSAGE: UNAUTHORIZED