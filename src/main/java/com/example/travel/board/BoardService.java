package com.example.travel.board;

import com.example.travel.account.Account;
import com.example.travel.board.dto.BoardDto.BoardResponse;
import com.example.travel.common.file.CommonFile;
import com.example.travel.common.file.CommonFileRepository;
import com.example.travel.common.file.CommonFileService;
import com.example.travel.common.provider.RedisProvider;
import com.example.travel.common.util.CryptoUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 게시판 관리와 관련된 기능을 제공하는 서비스 클래스를 나타냅니다.
 * 여기에는 게시판 엔터티 생성, 검색, 수정 및 삭제, 관련 파일 처리, 안전한 작업을 위한 암호화 활용 등이 포함됩니다.
 */
@Service
public class BoardService {

    private static final Logger log = LoggerFactory.getLogger(BoardService.class);
    private final BoardRepository boardRepository;
    private final CryptoUtil cryptoUtil;
    private final CommonFileService commonFileService;
    private final RedisProvider redisProvider;
    private final CommonFileRepository commonFileRepository;

    @Autowired
    public BoardService(BoardRepository boardRepository, CryptoUtil cryptoUtil,
        CommonFileService commonFileService, RedisProvider redisProvider,
        CommonFileRepository commonFileRepository) {
        this.boardRepository = boardRepository;
        this.cryptoUtil = cryptoUtil;
        this.commonFileService = commonFileService;
        this.redisProvider = redisProvider;
        this.commonFileRepository = commonFileRepository;
    }

    /**
     * 고유한 UUID를 생성한 후 제공된 Board 객체를 저장소에 저장합니다.
     *
     * @param board 저장할 Board 객체
     * @return 생성된 UUID로 저장된 Board 객체
     * @throws RuntimeException 저장 작업 중에 문제가 발생
     */
    private Board saveBoard(Board board) throws RuntimeException {
        // UUID 자동 생성
        String uuid = UUID.randomUUID().toString();
        board.setUuid(uuid);
        return boardRepository.save(board);
    }

    /**
     * 지정된 보드와 연관된 CommonFile 객체로 업로드된 파일 목록을 저장합니다.
     *
     * @param files 저장할 파일 목록
     * @param boardId 파일을 연결할 게시판의 ID
     * @return 파일이 성공적으로 저장되면 CommonFile 객체 목록이 반환되고, 입력 목록이 비어 있으면 null이 반환됩니다.
     * @throws RuntimeException 파일 저장 과정에서 오류가 발생하는 경우
     */
    private List<CommonFile> saveCommonFiles(List<MultipartFile> files, Long boardId)
        throws RuntimeException {

        List<CommonFile> commonFiles = null;
        if (!files.isEmpty()) {
            commonFiles = new ArrayList<>();
            for (MultipartFile file : files) {
                CommonFile commonFile = new CommonFile();
                commonFile.setTableName("board");
                commonFile.setTableId(boardId);
                commonFileService.writeFile(file, commonFile);
                commonFiles.add(commonFile);
            }
        }
        return commonFiles;
    }

    /**
     * 주어진 HTTP 서블릿 요청에서 이메일 속성을 가져옵니다.
     * 이메일 속성이 null이면 RuntimeException이 발생합니다.
     *
     * @param request 이메일 속성을 포함하는 HttpServletRequest 객체
     * @return 문자열로 된 이메일 속성
     * @throws RuntimeException 이메일 속성이 null인 경우
     */
    public String getEmail(HttpServletRequest request) throws RuntimeException {

        if (Objects.isNull(request.getAttribute("email"))) {
            log.error("email not null");
            throw new RuntimeException("email is null");
        }

        return request.getAttribute("email").toString();
    }

    /**
     * 모든 게시판 엔터티를 생성일을 기준으로 내림차순으로 정렬하여 검색합니다. 게시판 목록을 반환하기 전에 UUID가 암호화됩니다.
     *
     * @return 암호화된 UUID가 있는 Board 엔터티 목록을 생성 날짜별로 내림차순으로 정렬 한 객체
     */
    public List<Board> getAllBoards() {
        List<Board> boards = boardRepository.findAllByOrderByCreatedAtDesc();
        // 조회 시 UUID를 암호화하여 반환
        boards.forEach(board -> board.setUuid(cryptoUtil.encrypt(board.getUuid())));
        return boards;
    }

    /**
     * UUID로 Board 엔터티를 검색합니다. Board를 찾을 수 없으면 RuntimeException이 발생합니다. UUID는 반환되기 전에 암호화됩니다.
     *
     * @param encryptedUuid 검색할 보드의 고유 식별자
     * @return Board UUID가 암호화된 Board 엔터티를 반환
     * @throws RuntimeException 주어진 UUID로 보드를 찾을 수 없는 경우 발생
     */
    public BoardResponse getBoardByUuid(String encryptedUuid) throws RuntimeException {

        // UUID가 암호화된 형태인지 확인
        if (!cryptoUtil.isEncrypted(encryptedUuid)) {
            // 일반 UUID로 조회 후 암호화하여 반환
            log.error("암호화 되지않은 UUID로 조회");
            throw new RuntimeException("게시글을 찾을 수 없습니다.");
        }

        Board board = boardRepository.findByUuid(cryptoUtil.decrypt(encryptedUuid))
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        // 조회 시 UUID를 암호화하여 반환
        board.setUuid(cryptoUtil.encrypt(board.getUuid()));

        List<CommonFile> commonFiles = commonFileRepository.findByTableNameAndTableId("board", board.getId());
        commonFiles.forEach(commonFile -> commonFile.setUuid(cryptoUtil.encrypt(commonFile.getUuid())));

        return new BoardResponse(board, commonFiles);
    }

    /**
     * 새 Board 엔티티를 저장소에 저장합니다. 보드의 UUID는 저장 작업의 일부로 자동으로 생성됩니다.
     *
     * @param board 저장해야 할 필수 세부 정보를 포함하는 Board 엔터티
     */
    public void createBoard(Board board, String email) throws RuntimeException {

        Account account = redisProvider.getUserInfo(email)
            .orElseThrow(() -> new RuntimeException("Not logged in"));
        board.setAccountUuid(account.getUuid());

        this.saveBoard(board);
    }

    /**
     * 새로운 보드 엔티티를 만들고 업로드된 파일을 해당 엔티티에 연결합니다.
     *
     * @param reqBoard 생성할 보드의 세부 정보를 포함하는 보드 객체
     * @param files 생성된 보드와 연관될 파일 목록
     * @return 보드 엔터티와 연관된 파일 목록을 결합한 BoardResponse 객체를 반환합니다.
     * @throws RuntimeException 보드를 저장하거나 파일을 처리하는 동안 오류가 발생하는 경우 발생
     */
    public BoardResponse createBoard(Board reqBoard, String email, List<MultipartFile> files)
        throws RuntimeException {

        Account account = redisProvider.getUserInfo(email)
            .orElseThrow(() -> new RuntimeException("UserInfo is null"));
        reqBoard.setAccountUuid(account.getUuid());

        Board board = this.saveBoard(reqBoard);
        List<CommonFile> commonFiles = this.saveCommonFiles(files, board.getId());

        return new BoardResponse(board, commonFiles);
    }

    /**
     * UUID로 식별된 지정된 보드에 대한 보드 세부 정보와 관련 파일을 업데이트합니다.
     *
     * @param email 게시판을 소유한 사용자의 이메일
     * @param encryptedUuid 업데이트할 보드의 암호화된 UUID
     * @param boardDetail 보드의 업데이트된 제목과 내용을 담고 있는 {@code Board} 객체
     * @param files 보드에 업로드하고 연관시킬 파일을 나타내는 {@code MultipartFile} 객체
     * @return 업데이트된 보드 세부 정보와 관련 파일을 포함하는 {@code BoardResponse} 객체
     * @throws RuntimeException 사용자 정보를 검색할 수 없거나 게시판을 찾을 수 없는 경우
     */
    public BoardResponse updateBoard(String email, String encryptedUuid, Board boardDetail,
        String deleteFileId, List<MultipartFile> files) {

        Account account = redisProvider.getUserInfo(email)
            .orElseThrow(() -> new RuntimeException("UserInfo is null"));
        Board board = boardRepository.findByUuidAndAccountUuid(cryptoUtil.decrypt(encryptedUuid),
            account.getUuid()).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 삭제 할 파일 정보
        String[] deleteFileIds = deleteFileId.split(",");
        for (String fileId : deleteFileIds) {
            commonFileService.softDeleteCommonFile(fileId, "board", board.getId());
        }

        board.setTitle(boardDetail.getTitle());
        board.setContent(boardDetail.getContent());

        boardRepository.save(board);
        List<CommonFile> commonFiles = this.saveCommonFiles(files, board.getId());

        return new BoardResponse(board, commonFiles);
    }

    /**
     * 지정된 사용자 이메일과 암호화된 UUID와 관련된 게시판을 삭제합니다.
     *
     * @param email 삭제할 게시판 사용자의 이메일 주소
     * @param encryptedUuid 삭제할 보드의 암호화된 UUID
     * @throws RuntimeException 사용자 정보가 없거나 지정된 게시판이 존재하지 않는 경우
     */
    public void deleteBoard(String email, String encryptedUuid) {

        Account account = redisProvider.getUserInfo(email)
            .orElseThrow(() -> new RuntimeException("UserInfo is null"));
        Board board = boardRepository.findByUuidAndAccountUuid(cryptoUtil.decrypt(encryptedUuid),
            account.getUuid()).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        commonFileService.softDeleteCommonFileBulk("board", board.getId());
        boardRepository.delete(board);
    }

    /**
     * 이메일과 UUID 목록을 기반으로 특정 사용자의 여러 게시판을 대량으로 삭제합니다.
     * <br/>
     * 이 메서드는 제공된 이메일을 사용하여 사용자 계정 정보를 검색하고, UUID를 복호화하고,
     * 소프트 삭제 방식을 사용하여 관련 게시판과 관련 파일을 삭제합니다.
     * UUID가 제공되지 않거나 비어 있으면 RuntimeException이 발생합니다.
     *
     * @param email 삭제해야 할 보드의 계정 소유자의 이메일 주소
     * @param uuid 삭제할 보드를 나타내는 암호화된 UUID의 쉼표로 구분된 문자열
     * @throws RuntimeException UUID가 비어 있거나 사용자 정보를 검색할 수 없는 경우
     */
    @Transactional
    public void deleteBoardBulk(String email, String uuid) throws RuntimeException {

        if (uuid.isEmpty()) {
            throw new RuntimeException("uuid is empty");
        }

        List<String> uuids = Arrays.asList(uuid.split(","));

        Account account = redisProvider.getUserInfo(email)
            .orElseThrow(() -> new RuntimeException("UserInfo is null"));
        uuids.forEach(encryptedUuid -> {
            Board board = boardRepository.findByUuidAndAccountUuid(
                cryptoUtil.decrypt(encryptedUuid), account.getUuid()).orElse(null);
            if (!Objects.isNull(board)) {
                commonFileService.softDeleteCommonFileBulk("board", board.getId());
                boardRepository.delete(board);
            }
        });
    }
}
