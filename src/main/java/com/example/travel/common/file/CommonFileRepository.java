package com.example.travel.common.file;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonFileRepository extends JpaRepository<CommonFile, Long> {

    Optional<CommonFile> findByUuid(String uuid);
}
