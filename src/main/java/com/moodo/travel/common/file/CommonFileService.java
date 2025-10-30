package com.moodo.travel.common.file;

import com.moodo.travel.common.provider.JwtProvider;
import com.moodo.travel.common.provider.RedisProvider;
import com.moodo.travel.common.util.CryptoUtil;
import com.moodo.travel.common.util.RandomGeneratorUtil;
import com.moodo.travel.common.util.ValidationUtil;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 이 클래스는 파일 저장, 검색, 소프트 삭제, 다운로드 등 파일 관련 작업을 관리하는 서비스 세트를 제공합니다.
 * 파일 저장소, 토큰 기반 인증 및 암호화 유틸리티와 상호 작용하여 안전한 파일 처리를 보장합니다.
 * <br/>
 * 읽기, 쓰기, UUID 암호화, 사용자 토큰 확인, 파일 메타데이터를 포함한 적절한 응답 반환 등의 파일 작업을 관리합니다.
 * <br/>
 * CommonFileRepository, JwtProvider, RedisProvider,
 * CryptoUtil을 포함한 외부 유틸리티 클래스 및 저장소에 대한 종속성은 생성자를 통해 주입됩니다.
 */
@Component
public class CommonFileService {

    private static final Logger log = LogManager.getLogger(CommonFileService.class);

    private final CommonFileRepository commonFileRepository;
    private final JwtProvider jwtProvider;
    private final RedisProvider redisProvider;
    private final CryptoUtil cryptoUtil;

    @Value("${file.upload.path}")
    private String FILE_UPLOAD_PATH;

    @Value("${file.upload.image.path}")
    private String IMAGE_FILE_UPLOAD_PATH;

    @Value("${file.upload.allowed.extension}")
    private String ALLOWED_EXTENSIONS;

    @Value("${file.upload.image.allowed.extension}")
    private String IMAGE_ALLOWED_EXTENSIONS;

    @Autowired
    public CommonFileService(CommonFileRepository commonFileRepository, JwtProvider jwtProvider,
        RedisProvider redisProvider, CryptoUtil cryptoUtil) {
        this.commonFileRepository = commonFileRepository;
        this.jwtProvider = jwtProvider;
        this.redisProvider = redisProvider;
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
     * 토큰의 유효성을 확인한 후 제공된 암호화된 UUID를 기반으로 파일을 다운로드합니다. 이 메서드는 UUID를 복호화하고,
     * 데이터베이스에서 파일 정보를 검색하여 적절한 HTTP 헤더와 함께 다운로드 가능한 리소스로 제공합니다.
     *
     * @param token 인증 및 권한 부여에 사용되는 JWT 토큰
     * @param encryptedUuid 다운로드할 파일의 암호화된 고유 식별자
     * @return @code {ResponseEntity<Resource>}는 HTTP 헤더와 함께 파일을 리소스로 포함
     * @throws RuntimeException 토큰이 유효하지 않거나 토큰이 로그아웃으로 표시된 경우 발생
     * @throws IOException 파일 정보를 검색할 수 없거나, 파일이 존재하지 않거나, 파일을 읽는 중 오류가 발생한 경우
     */
    public ResponseEntity<Resource> downloadFile(String token, String encryptedUuid)
        throws RuntimeException, IOException {

        // 토큰 검증
        if (!jwtProvider.validateToken(token)) {
            log.error("Invalid token.");
            throw new RuntimeException("Invalid token.");
        }
        // 2. Redis JWT 확인
        if (!redisProvider.isJwt(token)) {
            log.error("Token is logged out.");
            throw new RuntimeException("Token is logged out.");
        }

        // 1. UUID 복호화 및 파일 정보 조회
        CommonFile commonFile = commonFileRepository.findByUuid(cryptoUtil.decrypt(encryptedUuid))
            .orElseThrow(() -> new IOException("파일 정보를 찾을 수 없습니다"));

        // 2. 실제 파일 경로 생성
        Path filePath = Paths.get(FILE_UPLOAD_PATH).resolve(commonFile.getFilePath()).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        // 3. 파일 존재 여부 및 가독성 확인
        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("파일을 찾을 수 없거나 읽을 수 없습니다. Path: " + filePath);
        }

        // 4. 원본 파일 이름 인코딩 (브라우저 호환성)
        String originalFileName = commonFile.getOriginalFileName();
        String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8)
            .replaceAll("\\+", "%20");

        // 5. HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + encodedFileName + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(filePath));
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(commonFile.getFileSize()));

        // 6. ResponseEntity를 통해 파일 리소스와 헤더 반환
        return ResponseEntity.ok()
            .headers(headers)
            .body(resource);
    }

    /**
     * 지정된 규칙과 허용되는 파일 유형에 따라 제공된 파일의 유효성을 검사합니다.
     * 파일이 null이거나 비어 있지 않은지, 유효한 파일 확장자를 가지고 있는지,
     * 그리고 이름에 유효하지 않은 문자가 포함되어 있지 않은지 확인합니다. 또한,
     * 허용되는 파일 확장자를 통해 지정된 유형의 파일 확장자를 확인합니다.
     *
     * @param file 검증할 멀티파트 파일입니다. null이거나 비어 있을 수 없습니다.
     * @param allowedType 허용되는 파일 유형(예: "image")은 허용되는 파일 확장자를 결정합니다.
     * @throws RuntimeException 파일이 null이거나 비어 있거나, 확장자가 잘못되었거나 누락되었거나,
     *                          이름에 안전하지 않은 문자가 포함되어 있거나, 허용된 확장자와 일치하지 않는 경우 발생
     */
    private void validation(MultipartFile file, String allowedType) throws RuntimeException {

        if (Objects.isNull(file) || file.isEmpty()) {
            log.error("업로드할 파일을 선택해 주세요.");
            throw new RuntimeException("업로드할 파일을 선택해 주세요.");
        }

        String originalFileName = file.getOriginalFilename();
        if (Objects.requireNonNull(originalFileName).lastIndexOf(".") < 0) {
            log.error("파일 확장자를 찾을 수 없습니다.");
            throw new RuntimeException("파일 확장자를 찾을 수 없습니다.");
        }

        // 파일명 안전성 검사
        if (ValidationUtil.isUnsafeFilename(originalFileName)) {
            log.error("안전하지 않은 파일명입니다: {}", originalFileName);
            throw new RuntimeException("안전하지 않은 파일명입니다.");
        }

        if (originalFileName.contains("..")) {
            log.error("파일이름에 '..'가 포함 될 수 없습니다. {}", originalFileName);
            throw new RuntimeException("파일이름에 특수문자가 포함 될 수 없습니다.");
        }

        // 파일 크기 검증 (10MB 제한)
        if (!ValidationUtil.isValidFileSize(file.getSize(), 10 * 1024 * 1024)) {
            log.error("파일 크기가 너무 큽니다: {} bytes", file.getSize());
            throw new RuntimeException("파일 크기가 너무 큽니다. 최대 10MB까지 업로드 가능합니다.");
        }

        // 파일명 길이 검증
        if (!ValidationUtil.isValidLength(originalFileName, 255)) {
            log.error("파일명이 너무 깁니다: {}", originalFileName.length());
            throw new RuntimeException("파일명이 너무 깁니다. 최대 255자까지 가능합니다.");
        }

        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);

        if (!(allowedType.equals("image") ? IMAGE_ALLOWED_EXTENSIONS : ALLOWED_EXTENSIONS).contains(
            fileExtension)) {
            log.error("지정되지 않은 확장자 입니다: {}", fileExtension);
            throw new RuntimeException("지정되지 않은 파일 확장자 입니다.");
        }

        // 파일명에 XSS 공격 시도가 있는지 검사
        if (ValidationUtil.containsXss(originalFileName)) {
            log.error("파일명에 XSS 공격 시도가 감지되었습니다: {}", originalFileName);
            throw new RuntimeException("안전하지 않은 파일명입니다.");
        }
    }

    /**
     * 파일 업로드를 처리하여 메타데이터를 저장하고 지정된 디렉터리에 파일을 기록합니다.
     * 이 메서드는 업로드 경로가 유효하고 안전한지 확인하고, 필요한 디렉터리가 없는 경우 디렉터리를 생성합니다.
     * 또한 업로드된 파일에 대한 임의의 고유 식별자와 파일 이름을 생성합니다.
     *
     * @param file 업로드되는 파일, MultipartFile로 표현됨
     * @param commonFile 업로드된 파일에 대한 메타데이터를 저장할 CommonFile 객체
     * @param allowedType allowedType 업로드되는 파일의 유형(예: "image")은 업로드 경로를 결정하는 데 사용
     * @throws RuntimeException 업로드 경로가 안전하지 않거나 파일을 쓰는 동안 오류가 발생하는 경우 발생
     */
    private void write(MultipartFile file, CommonFile commonFile, String allowedType)
        throws RuntimeException {

        commonFile.setUuid(UUID.randomUUID().toString());
        commonFile.setChangeFileName(RandomGeneratorUtil.generateRandomString(30));
        commonFile.setOriginalFileName(file.getOriginalFilename());
        commonFile.setFileExtension(Objects.requireNonNull(file.getOriginalFilename())
            .substring(file.getOriginalFilename().lastIndexOf(".") + 1));

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = today.format(formatter);
        String sDirPath = formattedDate + "/";
        String sFilePath =
            sDirPath + commonFile.getChangeFileName() + "." + commonFile.getFileExtension();
        // 디렉토리 경로
        Path path = Path.of(
            (allowedType.equals("image") ? IMAGE_FILE_UPLOAD_PATH : FILE_UPLOAD_PATH) + sDirPath);
        // 2. FILE_UPLOAD_PATH를 Path 객체로 만듭니다.
        Path uploadDir = Paths.get(
                (allowedType.equals("image") ? IMAGE_FILE_UPLOAD_PATH : FILE_UPLOAD_PATH))
            .toAbsolutePath().normalize();

        // 3. 사용자 입력으로 받은 경로를 안전한 경로에 결합합니다.
        // Paths.get()은 경로를 안전하게 조합하는 데 도움을 줍니다.
        Path uploadFilePath = uploadDir.resolve(sFilePath).normalize();

        // 4. 최종 정규화된 경로가 업로드 디렉터리 하위에 있는지 확인합니다.
        // `startsWith()` 메서드를 사용하여 외부 경로 접근을 방지합니다.
        if (!uploadFilePath.startsWith(uploadDir)) {
            log.error("uploadDir : {} uploadFilePath : {}", uploadDir, uploadFilePath);
            throw new RuntimeException("지정된 경로가 아닙니다.");
        }

        try {

            // 디렉토리 체크
            if (!Files.isDirectory(path)) {
                // 디렉토리 없으면 생성
                Files.createDirectories(path);
            }

            commonFile.setFilePath(sFilePath);
            commonFileRepository.save(commonFile);

            Files.write(uploadFilePath, file.getBytes());

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            try {
                if (Files.exists(uploadFilePath)) {
                    Files.delete(uploadFilePath);
                }
            } catch (IOException ioe) {
                log.error(ioe.getMessage());
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * 제공된 {@link CommonFile} 엔티티 정보를 기반으로 이미지 파r일의 업로드 프로세스를 처리합니다.
     * 이 메서드는 파일의 유효성을 검사한 후 업로드 프로세스를 내부 메서드에 위임합니다.
     *
     * @param file 업로드할 이미지 파일입니다. null이거나 비어 있지 않은 유효한 {@link MultipartFile} 객체여야 하며,
     *             이미지 파일에 허용되는 파일 확장자를 준수해야 합니다.
     * @param commonFile 파일에 대한 메타데이터(예: 연관된 테이블 이름, 테이블 ID 및 기타 관련 세부 정보)를 포함하는 {@link CommonFile} 엔터티입니다.
     * @throws RuntimeException 파일 검증에 실패하거나 업로드 과정에서 오류가 발생하면 발생
     */
    public void imageThumbnailUpload(MultipartFile file, CommonFile commonFile) throws RuntimeException {

        String allowedType = "image";
        validation(file, allowedType);
        this.write(file, commonFile, allowedType);
    }

    /**
     * 제공된 {@link CommonFile} 엔티티 정보를 기반으로 이미지 파일의 업로드 프로세스를 처리합니다.
     * 이 메서드는 파일의 유효성을 검사한 후 업로드 프로세스를 내부 메서드에 위임합니다.
     *
     * @param file 업로드할 이미지 파일입니다. null이거나 비어 있지 않은 유효한 {@link MultipartFile} 객체여야 하며,
     *             이미지 파일에 허용되는 파일 확장자를 준수해야 합니다.
     * @param commonFile 파일에 대한 메타데이터(예: 연관된 테이블 이름, 테이블 ID 및 기타 관련 세부 정보)를 포함하는 {@link CommonFile} 엔터티입니다.
     * @throws RuntimeException 파일 검증에 실패하거나 업로드 과정에서 오류가 발생하면 발생
     */
    public void imageUpload(MultipartFile file, CommonFile commonFile) throws RuntimeException {

        String allowedType = "image";
        validation(file, allowedType);
        this.write(file, commonFile, allowedType);
        log.info("commonFile : {}", commonFile.toString());
    }

    /**
     * 파일을 업로드하고 제공된 {@link CommonFile} 엔티티 정보를 기반으로 처리합니다.
     * 이 메서드는 파일의 유효성을 검사하고 파일 업로드 프로세스를 다른 내부 메서드에 위임합니다.
     *
     * @param file 업로드할 파일입니다. null이거나 비어 있지 않은 유효한 {@link MultipartFile} 객체여야 하며,
     *             이미지 파일에 허용되는 파일 확장자를 준수해야 합니다.
     * @param commonFile 파일에 대한 메타데이터(예: 연관된 테이블 이름, 테이블 ID 및 기타 관련 세부 정보)를 포함하는 {@link CommonFile} 엔터티입니다.
     * @throws RuntimeException 파일 검증에 실패하거나 업로드 과정에서 오류가 발생하면 발생
     */
    public void upload(MultipartFile file, CommonFile commonFile) throws RuntimeException {

        String allowedType = "file";
        validation(file, allowedType);
        this.write(file, commonFile, allowedType);
        log.info("commonFile : {}", commonFile.toString());
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
