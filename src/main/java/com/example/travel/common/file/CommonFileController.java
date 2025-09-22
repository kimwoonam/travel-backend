package com.example.travel.common.file;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/file")
public class CommonFileController {

    private static final Logger log = LogManager.getLogger(CommonFileController.class);
    private final CommonFileService commonFileService;

    @Autowired
    public CommonFileController(CommonFileService commonFileService) {
        this.commonFileService = commonFileService;
    }

    /**
     * UUID를 기반으로 보드 엔터티를 검색합니다.
     *
     * @param uuid 검색할 게시판의 고유 식별자
     * @return 성공 시 검색된 보드을 포함하는 ResponseEntity를 반환하고, 오류가 발생하면 잘못된 요청 응답을 반환합니다.
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<Resource> download(@PathVariable String uuid) {

        try {
            return commonFileService.downloadFile(uuid);
        } catch (RuntimeException | IOException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
