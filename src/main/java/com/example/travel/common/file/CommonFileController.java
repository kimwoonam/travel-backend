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

/**
 * 이 컨트롤러는 파일 관련 작업을 위한 API 엔드포인트를 제공합니다.
 * 제공된 토큰과 UUID를 기반으로 파일을 다운로드할 수 있도록 지원합니다.
 */
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
     * 토큰과 UUID를 포함하는 제공된 매개변수를 기반으로 파일을 다운로드합니다.
     * 토큰의 유효성을 검사하고 매개변수를 분할하여 토큰과 UUID를 가져옵니다.
     * 오류 발생 시 파일을 다운로드 가능한 리소스로 반환하거나 잘못된 요청 응답으로 반환합니다.
     *
     * @param param 토큰과 UUID를 쉼표로 구분하여 포함하는 문자열입니다.
     *              첫 번째 값은 토큰을 나타내고 두 번째 값은 UUID를 나타냅니다.
     * @return 작업이 성공하면 요청된 파일을 리소스로 포함하는 ResponseEntity를 반환하고,
     *         처리 중 오류가 발생하면 잘못된 요청 응답을 반환합니다.
     */
    @GetMapping("/{param}")
    public ResponseEntity<Resource> download(@PathVariable String param) {

        try {

            String[] params = param.split(",");
            if (params.length != 2) {
                log.error("param is invalid");
                return ResponseEntity.badRequest().build();
            }

            String token = params[0];
            String uuid = params[1];

            return commonFileService.downloadFile(token, uuid);
        } catch (RuntimeException | IOException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
