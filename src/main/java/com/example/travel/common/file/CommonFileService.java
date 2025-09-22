package com.example.travel.common.file;

import com.example.travel.common.util.CryptoUtil;
import com.example.travel.common.util.RandomGeneratorUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * CommonFileService 클래스는 파일 관리 작업(저장, 검색, 삭제)을 처리하기 위한 서비스입니다.
 * 파일 시스템 상의 파일 저장 작업, 데이터베이스의 파일 메타데이터 관리 및 암호화 처리를 수행합니다.
 * <br/>
 * 주요 기능:
 * - 파일 엔터티의 UUID 암호화 및 복호화
 * - 파일 저장 디렉터리 생성 및 관리
 * - 파일 읽기, 쓰기, 삭제
 * - 데이터베이스와의 상호작용을 통한 파일 메타데이터 관리
 * <br/>
 * 이 클래스는 {@link CommonFileRepository}와 {@link CryptoUtil}을 활용하며, 파일 저장 경로는 고정값을 사용합니다.
 */
@Component
public class CommonFileService {

    private static final Logger log = LogManager.getLogger(CommonFileService.class);

    private final CommonFileRepository commonFileRepository;
    private final CryptoUtil cryptoUtil;

    @Value("${file.upload.path}")
    private String FILE_UPLOAD_PATH;

    @Autowired
    public CommonFileService(CommonFileRepository commonFileRepository, CryptoUtil cryptoUtil) {
        this.commonFileRepository = commonFileRepository;
        this.cryptoUtil = cryptoUtil;
    }

    /**
     * UUID로 {@link CommonFile} 엔터티를 검색하고 엔터티를 반환하기 전에 UUID를 암호화합니다.
     *
     * @param encryptedUuid 검색할 {@link CommonFile}의 고유 식별자
     * @return UUID로 암호화된 {@link CommonFile} 엔터티
     * @throws RuntimeException 지정된 UUID가 있는 {@link CommonFile}을 찾을 수 없는 경우 발생
     */
    public CommonFile getCommonFileByUuid(String encryptedUuid) {
        CommonFile commonFile = commonFileRepository.findByUuid(cryptoUtil.decrypt(encryptedUuid))
            .orElseThrow(() -> new RuntimeException("첨부파일을 찾을 수 없습니다."));
        // 조회 시 UUID를 암호화하여 반환
        commonFile.setUuid(cryptoUtil.encrypt(commonFile.getUuid()));
        return commonFile;
    }

    /**
     * 지정된 파일의 삭제 상태와 삭제 타임스탬프를 업데이트하여 데이터베이스에서 해당 파일을 "소프트 삭제됨"으로 표시합니다.
     * 파일은 UUID, 연결된 테이블 이름 및 테이블 ID를 사용하여 식별됩니다.
     *
     * @param uuid 소프트 삭제할 파일의 고유 식별자
     * @param tableName 파일과 연관된 테이블의 이름
     * @param tableId 파일과 연관된 테이블 ID
     */
    public void softDeleteCommonFile(String uuid, String tableName, Long tableId) {
        commonFileRepository.updateDeleteStatusByUuidAndTableNameAndTableId(uuid, tableName,
            tableId);
    }

    /**
     * 특정 테이블 이름 및 테이블 ID와 연결된 파일을 데이터베이스에서 "소프트 삭제됨"으로 표시합니다.
     * 이 메서드는 `deleteYn` 필드를 'Y'로 수정하고
     * `CommonFile` 테이블의 해당 레코드에 대한 `deletedAt` 타임스탬프를 업데이트합니다.
     *
     * @param tableName 소프트 삭제할 파일과 연관된 테이블의 이름입니다.
     * @param tableId 소프트 삭제할 파일과 연관된 테이블의 ID입니다.
     */
    public void softDeleteCommonFileBulk(String tableName, Long tableId) {
        commonFileRepository.updateDeleteStatusByTableNameAndTableId(tableName, tableId);
    }

    /**
     * 지정된 디렉터리에 파일을 저장하고 파일 메타데이터로 {@code CommonFile} 엔터티를 업데이트합니다.
     * 이 메서드는 디렉터리 존재 여부를 확인하고 저장하기 전에 고유한 파일 이름을 지정합니다.
     * 처리 중 오류가 발생하면 파일이 삭제되고 {@code RuntimeException}이 발생합니다.
     *
     * @param file 저장할 다중 파트 파일입니다. 비어 있을 수 없습니다.
     * @param commonFile 파일 메타데이터로 업데이트할 {@code CommonFile} 엔터티
     * @throws RuntimeException 파일이 비어 있거나, 경로 생성 중 오류가 발생하거나, 파일 확장자를 확인할 수 없는 경우
     */
    public void writeFile(MultipartFile file, CommonFile commonFile) throws RuntimeException {

        if (file.isEmpty()) {
            log.error("업로드할 파일을 선택해 주세요.");
            throw new RuntimeException("업로드할 파일을 선택해 주세요.");
        }

        String originalFileName = file.getOriginalFilename();
        if (Objects.requireNonNull(originalFileName).lastIndexOf(".") < 0) {
            log.error("파일 확장자를 찾을 수 없습니다.");
            throw new RuntimeException("파일 확장자를 찾을 수 없습니다.");
        }

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        String formattedDate = today.format(formatter);
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        String changeFileName = RandomGeneratorUtil.generateRandomString(30);
        String sDirPath = formattedDate + "/";
        String sFilePath = sDirPath + changeFileName + "." + fileExtension;
        // 디렉토리 경로
        Path path = Path.of(FILE_UPLOAD_PATH +  sDirPath);
        // 디렉토리 + 파일 경로
        Path filePath = Paths.get(FILE_UPLOAD_PATH + sFilePath);

        try {

            // 디렉토리 체크
            if (!Files.isDirectory(path)) {
                // 디렉토리 없으면 생성
                Files.createDirectories(path);
            }

            // 저장할 파일정보 설정
            commonFile.setUuid(UUID.randomUUID().toString());
            commonFile.setFilePath(sFilePath);
            commonFile.setFileSize(file.getSize());
            commonFile.setChangeFileName(changeFileName);
            commonFile.setOriginalFileName(originalFileName);
            commonFile.setFileExtension(fileExtension);

            commonFileRepository.save(commonFile);

            Files.write(filePath, file.getBytes());

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            try {
                Files.delete(filePath);
            } catch (IOException ioe) {
                log.error(ioe.getMessage());
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * 제공된 암호화된 UUID와 연결된 파일을 삭제합니다. 이 메서드는 관련 파일이 파일 시스템에 있는지 확인하고 제거합니다.
     * 저장소에서 파일을 찾을 수 없거나 디렉토리가 없으면 예외가 발생합니다.
     *
     * @param encryptedUuid 삭제할 파일의 암호화된 고유 식별자, 저장소 및 파일 시스템에서 파일을 찾는 데 사용됩니다.
     */
    public void deleteFile(String encryptedUuid) {
        try {

            String dirPath = "/path/to/file/";

            Path path = Path.of(dirPath);
            // 디렉토리 체크
            if (!Files.isDirectory(path)) {
                // 디렉토리 없으면 생성
                throw new IOException("디렉토리가 없습니다.");
            }

            CommonFile commonFile = commonFileRepository.findByUuid(
                    cryptoUtil.decrypt(encryptedUuid))
                .orElseThrow(() -> new RuntimeException("등록된 파일을 찾을 수 없습니다."));

            Files.delete(Path.of(commonFile.getFilePath()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 특정 테이블 이름 및 테이블 ID와 관련된 파일을 파일 시스템에서 해당 항목을 제거하여 삭제합니다.
     * 디렉토리가 없으면 예외가 발생합니다. 프로세스 중 {@link IOException}이 발생하면 오류를 기록합니다.
     *
     * @param tableName 삭제할 파일과 연관된 테이블의 이름
     * @param tableId 삭제할 파일과 연관된 테이블의 ID
     */
    public void deleteFile(String tableName, Long tableId) {
        try {

            String dirPath = "/path/to/file/";

            Path path = Path.of(dirPath);
            // 디렉토리 체크
            if (!Files.isDirectory(path)) {
                // 디렉토리 없으면 생성
                throw new IOException("디렉토리가 없습니다.");
            }

            List<CommonFile> commonFiles = commonFileRepository.findByTableNameAndTableId(tableName,
                tableId);

            for (CommonFile commonFile : commonFiles) {
                Files.delete(Path.of(commonFile.getFilePath()));
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
