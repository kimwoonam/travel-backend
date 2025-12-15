package com.moodo.travel.account;

import com.moodo.travel.account.dto.AccountDto;
import com.moodo.travel.account.dto.AccountDto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AccountController는 사용자의 인증 및 계정 관리와 관련된 RESTful API를 처리하는 컨트롤러입니다.
 * 가입, 로그인, 계정 삭제, 로그아웃과 같은 작업을 지원하며, 응답에는 JWT 토큰을 쿠키에 설정하거나
 * 관련 메시지를 반환합니다. 모든 작업은 AccountService를 통해 수행됩니다.
 * <br/>
 * 요청 경로는 "/api/auth"로 기본적으로 매핑됩니다.
 * <br/>
 * 주요 기능:
 * - 사용자 가입
 * - 로그인 처리
 * - 계정 삭제
 * - 로그아웃 처리
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "인증 API", description = "사용자 인증 및 계정 관리 API")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * 가입 정보 수신, 새 계정 생성, 로그인 응답 반환을 통해 사용자 등록을 처리합니다.
     * 가입 성공 시 생성된 토큰이 포함된 응답 쿠키를 설정합니다. 이메일이 이미 등록된 경우
     * 충돌 상태 코드와 함께 오류 메시지가 반환됩니다.
     *
     * @param request 사용자의 이메일, 비밀번호, 이름을 포함하는 가입 요청
     * @return 로그인 응답을 포함하는 ResponseEntity(성공 시 토큰, 이메일, 이름이 포함된)를 반환하고,
     *         이메일이 이미 등록된 경우 충돌 상태 코드가 포함된 오류 메시지를 반환
     */
    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "회원가입 성공",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "409", description = "이미 등록된 이메일"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AccountDto.SignupRequest request) {
        try {

            LoginResponse response = accountService.signup(request);
            ResponseCookie cookie = accountService.createCookie(response.token);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, String.valueOf(cookie));

            return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * 자격 증명을 인증하고 세션 토큰을 생성하여 사용자 로그인을 처리합니다.
     * 로그인이 성공하면 토큰이 생성되어 응답에 쿠키로 포함됩니다.
     * 잘못된 자격 증명으로 인해 로그인에 실패하면 '인증되지 않음' 상태가 반환됩니다.
     *
     * @param request 사용자의 이메일과 비밀번호를 포함하는 로그인 요청
     * @return ResponseEntity는 로그인 응답을 포함하며, 성공 시 토큰, 이메일, 이름을 포함하고
     *         인증에 실패하면 권한 없음 상태의 오류 메시지를 반환
     */
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AccountDto.LoginRequest request) {
        try {

            LoginResponse response = accountService.login(request);
            ResponseCookie cookie = accountService.createCookie(response.token);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, String.valueOf(cookie));
            return ResponseEntity.ok().headers(headers).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * 제공된 이메일과 비밀번호에 연결된 계정을 삭제합니다.
     * 삭제를 수행하는 사용자를 인증하려면 유효한 권한 부여 토큰이 필요합니다.
     * 삭제가 성공하면 삭제된 세션 쿠키와 함께 "콘텐츠 없음" 응답이 반환됩니다.
     *
     * @param authHeader 인증을 위한 Bearer 토큰을 포함하는 Authorization 헤더
     * @param email 삭제할 계정의 이메일
     * @param password 소유권을 확인하기 위한 계정 비밀번호
     * @return 삭제 작업의 결과를 나타내는 ResponseEntity입니다.
     *         성공 시 "콘텐츠 없음" 응답을 반환하고, 성공 시 "잘못된 요청" 응답을 반환
     */
    @Operation(summary = "계정 삭제", description = "계정을 삭제합니다. 인증 토큰과 비밀번호 확인이 필요합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "계정 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(
        @Parameter(description = "Bearer 토큰", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "삭제할 계정의 이메일", required = true)
        @RequestParam String email,
        @Parameter(description = "계정 비밀번호", required = true)
        @RequestParam String password) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                accountService.deleteByEmail(token, email, password);

                ResponseCookie cookie = accountService.createCookie("");
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.SET_COOKIE, String.valueOf(cookie));

                return ResponseEntity.noContent().headers(headers).build();
            } else {
                return ResponseEntity.badRequest().body("유효하지 않은 토큰");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 현재 인증된 사용자의 세션 토큰을 무효화하여 로그아웃합니다.
     * 클라이언트에서 인증 쿠키를 제거하고 적절한 응답을 반환합니다.
     *
     * @param authHeader 사용자 세션에 대한 Bearer 토큰을 포함하는 Authorization 헤더입니다.
     * @return 로그아웃 작업 결과를 나타내는 ResponseEntity:
     *         - 토큰이 성공적으로 무효화되고 쿠키가 제거된 경우 204 No Content가 반환됩니다.
     *         - 제공된 토큰이 유효하지 않거나 형식이 올바르지 않은 경우 400 Bad Request가 반환됩니다.
     *         - 로그아웃 프로세스 중 오류가 발생한 경우 500 Internal Server Error가 반환됩니다.
     */
    @Operation(summary = "로그아웃", description = "현재 세션을 종료하고 토큰을 무효화합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 토큰"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
        @Parameter(description = "Bearer 토큰", required = true)
        @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                accountService.logout(token);

                ResponseCookie cookie = accountService.createCookie("");
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.SET_COOKIE, String.valueOf(cookie));
                return ResponseEntity.noContent().headers(headers).build();
            } else {
                return ResponseEntity.badRequest().body("유효하지 않은 토큰");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("로그아웃 중 오류가 발생했습니다");
        }
    }
}
