package com.example.travel.account;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * AccountRepository는 Account 엔티티에 대한 데이터 접근 작업을 처리하는
 * Spring Data JPA 리포지토리입니다. 기본적인 CRUD 연산 및 데이터베이스
 * 조작 작업을 제공합니다.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * 주어진 이메일 주소로 계정을 찾습니다.
     *
     * @param email 검색할 이메일 주소입니다. null이거나 비어 있을 수 없습니다.
     * @return 계정이 발견되면 해당 계정을 포함하는 {@code Optional<Account>}를 반환하고,
     *         지정된 이메일에 계정이 없으면 빈 {@code Optional}을 반환
     */
    Optional<Account> findByEmail(String email);

    /**
     * 주어진 UUID로 계정을 검색합니다.
     *
     * @param uuid 검색할 계정의 UUID입니다. null이거나 비어 있으면 안 됩니다.
     * @return 계정이 발견되면 해당 계정을 포함하는 {@code Optional<Account>}를 반환하고,
     *         지정된 UUID에 계정이 없으면 빈 {@code Optional}을 반환합니다.
     */
    Optional<Account> findByUuid(String uuid);
}
