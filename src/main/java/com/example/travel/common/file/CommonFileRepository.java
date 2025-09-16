package com.example.travel.common.file;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@link CommonFile} 엔터티를 관리하기 위한 저장소 인터페이스입니다.
 * CommonFile 엔터티에 대한 CRUD 작업과 사용자 지정 쿼리 메서드를 제공합니다.
 */
public interface CommonFileRepository extends JpaRepository<CommonFile, Long> {

    /**
     * 고유한 UUID를 기반으로 {@link CommonFile} 엔터티를 검색합니다.
     *
     * @param uuid 검색할 {@link CommonFile}의 고유 식별자입니다.
     * @return {@link CommonFile}이 있으면 이를 포함하는 {@code Optional}을 반환하고,
     *         지정된 UUID를 가진 엔터티를 찾을 수 없으면 빈 {@code Optional}을 반환합니다.
     */
    Optional<CommonFile> findByUuid(String uuid);

    /**
     * 지정된 테이블 이름 및 테이블 ID와 연결된 {@link CommonFile} 엔터티 목록을 검색합니다.
     *
     * @param tableName 일치시킬 테이블의 이름입니다.
     * @param tableId 일치시킬 테이블의 ID입니다.
     * @return 주어진 테이블 이름과 테이블 ID에 해당하는 {@link CommonFile} 엔터티 목록을 반환합니다.
     *         일치하는 엔터티가 없으면 빈 목록이 반환
     */
    List<CommonFile> findByTableNameAndTableId(String tableName, Long tableId);
}
