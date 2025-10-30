# travel-backend

## 개요

* Cursor AI를 통한 게시판 FrontEnd/BackEnd 개발

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
## github

* frontend : https://github.com/kimwoonam/travel-front
* backend : https://github.com/kimwoonam/travel-backend