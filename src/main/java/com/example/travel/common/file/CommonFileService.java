package com.example.travel.common.file;

import com.example.travel.common.util.CryptoUtil;
import com.example.travel.common.util.RandomGeneratorUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CommonFileService {

    private static final Logger log = LogManager.getLogger(CommonFileService.class);

    private final CommonFileRepository commonFileRepository;
    private final CryptoUtil cryptoUtil;

    @Autowired
    public CommonFileService(CommonFileRepository commonFileRepository, CryptoUtil cryptoUtil) {
        this.commonFileRepository = commonFileRepository;
        this.cryptoUtil = cryptoUtil;
    }

    public CommonFile getCommonFileByUuid(String uuid) {
        CommonFile commonFile = commonFileRepository.findByUuid(uuid)
            .orElseThrow(() -> new RuntimeException("첨부파일을 찾을 수 없습니다."));
        // 조회 시 UUID를 암호화하여 반환
        commonFile.setUuid(cryptoUtil.encrypt(commonFile.getUuid()));
        return commonFile;
    }

    public void saveFile(MultipartFile file, CommonFile commonFile) throws RuntimeException {

        if (file.isEmpty()) {
            log.error("업로드할 파일을 선택해 주세요.");
            throw new RuntimeException("업로드할 파일을 선택해 주세요.");
        }

        String dirPath = "/path/to/file/";
        try {

            Path path = Path.of(dirPath);
            // 디렉토리 체크
            if (!Files.isDirectory(path)) {
                // 디렉토리 없으면 생성
                Files.createDirectories(path);
            }

            if (Objects.requireNonNull(file.getOriginalFilename()).lastIndexOf(".") < 0) {
                log.error("파일 확장자를 찾을 수 없습니다.");
                throw new RuntimeException("파일 확장자를 찾을 수 없습니다.");
            }

            Path filePath = Paths.get(dirPath + commonFile.getChangeFileName());
            Files.write(filePath, file.getBytes());
            commonFile.setFilePath(filePath.toString());
            commonFile.setFileSize(file.getSize());
            commonFile.setChangeFileName(RandomGeneratorUtil.generateRandomString(30));
            commonFile.setOriginalFileName(file.getOriginalFilename());
            commonFile.setFileExtension(file.getOriginalFilename()
                .substring(file.getOriginalFilename().lastIndexOf(".") + 1));

            commonFileRepository.save(commonFile);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            log.error("RuntimeException : {}", e.getMessage());
            try {
                Files.delete(Path.of(dirPath + commonFile.getChangeFileName()));
            } catch (IOException ioe) {
                log.error("IOException : {}", ioe.getMessage());
            }
            throw new RuntimeException(e);
        }
    }
}
