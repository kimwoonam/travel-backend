# Login

## 개요

* 지정된 Mapping 제외한 컨트롤러 호출 시 jwt의 검증관련 인터셉터를 실행한다. 

## 설명
* Http Method가 Option은 제외한다.
* URI /api/auth/ 중 /api/auth/logout이 아닌 Mapping은 제외한다.
* Request Header의 속성 Authorization의 값이 Bearer 시작되는지 확인한다.
* 서버에서 정상적으로 발급한 토큰인지 확인한다.

## 구조
