package com.moodo.travel.board;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * BoardRepository는 Board 엔터티에 대한 데이터 액세스 레이어를 제공합니다.
 * Spring Data JPA의 JpaRepository를 확장하여 기본적인 CRUD 작업과
 * 사용자 정의 쿼리 메서드를 구현합니다.
 */
@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    /**
     * 저장소에서 모든 Board 엔터티를 검색하여 생성 시간을 기준으로 내림차순으로 정렬합니다.
     *
     * @return  생성 날짜별로 내림차순으로 정렬된 Board 엔터티 목록을 반환
     */
    List<Board> findAllByOrderByCreatedAtDesc();

    /**
     * 페이징을 적용하여 Board 엔터티를 생성 시간 기준 내림차순으로 검색합니다.
     *
     * @param pageable 페이징 정보
     * @return 페이징된 Board 엔터티 목록
     */
    Page<Board> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 특정 계정의 게시판을 페이징하여 검색합니다.
     *
     * @param accountUuid 계정 UUID
     * @param pageable 페이징 정보
     * @return 페이징된 Board 엔터티 목록
     */
    Page<Board> findByAccountUuidOrderByCreatedAtDesc(String accountUuid, Pageable pageable);

    /**
     * 고유한 UUID를 사용하여 저장소에서 Board 엔터티를 검색합니다.
     * 인덱스를 활용한 최적화된 쿼리입니다.
     *
     * @param uuid 검색할 Board 엔터티의 고유 식별자
     * @return 찾은 경우 Board 엔터티를 포함하는 Optional을 반환하고,
     *         주어진 UUID를 가진 엔터티가 없는 경우 빈 Optional을 반환
     */
    @Query("SELECT b FROM Board b WHERE b.uuid = :uuid")
    Optional<Board> findByUuid(@Param("uuid") String uuid);

    /**
     * 제공된 UUID 및 계정 UUID와 일치하는 Board 엔터티를 포함하는 Optional을 검색합니다(해당 엔터티가 저장소에 있는 경우).
     * 복합 인덱스를 활용한 최적화된 쿼리입니다.
     *
     * @param uuid 찾으려는 Board 엔터티의 고유 식별자
     * @param accountUuid 연관된 계정의 고유 식별자
     * @return 일치하는 Board 엔터티가 발견되면 해당 엔터티를 포함하는 Optional을 반환하고, 일치하는 엔터티가 발견되지 않으면 빈 Optional을 반환
     */
    @Query("SELECT b FROM Board b WHERE b.uuid = :uuid AND b.accountUuid = :accountUuid")
    Optional<Board> findByUuidAndAccountUuid(@Param("uuid") String uuid, @Param("accountUuid") String accountUuid);

    /**
     * 제목으로 게시판을 검색합니다 (페이징 지원).
     *
     * @param title 검색할 제목 (부분 일치)
     * @param pageable 페이징 정보
     * @return 페이징된 Board 엔터티 목록
     */
    @Query("SELECT b FROM Board b WHERE b.title LIKE %:title% ORDER BY b.createdAt DESC")
    Page<Board> findByTitleContainingOrderByCreatedAtDesc(@Param("title") String title, Pageable pageable);

    /**
     * 전체 게시판 수를 반환합니다.
     *
     * @return 전체 게시판 수
     */
    @Query("SELECT COUNT(b) FROM Board b")
    long countAll();
}
