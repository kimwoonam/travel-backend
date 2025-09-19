package com.example.travel.board;

import com.example.travel.account.Account;
import com.example.travel.board.dto.BoardDto.BoardResponse;
import com.example.travel.common.file.CommonFile;
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
 * BoardService 클래스는 보드 생성, 업데이트, 삭제 및 검색을 포함하여 보드 엔티티를 관리하고 조작하는 기능을 제공합니다.
 * 이 클래스는 데이터베이스 상호작용을 위한 BoardRepository, UUID 암호화 및 복호화를 위한 CryptoUtil
 * 파일 작업 처리를 위한 CommonFileService와 통합됩니다.
 */
@Service
public class BoardService {

    private static final Logger log = LoggerFactory.getLogger(BoardService.class);
    private final BoardRepository boardRepository;
    private final CryptoUtil cryptoUtil;
    private final CommonFileService commonFileService;
    private final RedisProvider redisProvider;

    @Autowired
    public BoardService(BoardRepository boardRepository, CryptoUtil cryptoUtil,
        CommonFileService commonFileService, RedisProvider redisProvider) {
        this.boardRepository = boardRepository;
        this.cryptoUtil = cryptoUtil;
        this.commonFileService = commonFileService;
        this.redisProvider = redisProvider;
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


    public String getEmail(HttpServletRequest request) throws RuntimeException {

        if (Objects.isNull(request.getAttribute("email"))) {
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
        boards.forEach(board -> {
            board.setUuid(cryptoUtil.encrypt(board.getUuid()));
        });
        return boards;
    }

    /**
     * UUID로 Board 엔터티를 검색합니다. Board를 찾을 수 없으면 RuntimeException이 발생합니다. UUID는 반환되기 전에 암호화됩니다.
     *
     * @param encryptedUuid 검색할 보드의 고유 식별자
     * @return Board UUID가 암호화된 Board 엔터티를 반환
     * @throws RuntimeException 주어진 UUID로 보드를 찾을 수 없는 경우 발생
     */
    public Board getBoardByUuid(String encryptedUuid) throws RuntimeException {

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

        return board;
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
     * @param files    생성된 보드와 연관될 파일 목록
     * @return 보드 엔터티와 연관된 파일 목록을 결합한 BoardResponse 객체를 반환합니다.
     * @throws RuntimeException 보드를 저장하거나 파일을 처리하는 동안 오류가 발생하는 경우 발생
     */
    public BoardResponse createBoard(Board reqBoard, String email, List<MultipartFile> files)
        throws RuntimeException {

        Account account = redisProvider.getUserInfo(email)
            .orElseThrow(() -> new RuntimeException("UserInfo is null"));
        reqBoard.setAccountUuid(account.getUuid());

        Board board = this.saveBoard(reqBoard);
        List<CommonFile> commonFiles = null;
        if (!files.isEmpty()) {
            commonFiles = new ArrayList<>();
            for (MultipartFile file : files) {
                CommonFile commonFile = new CommonFile();
                commonFile.setTableName("board");
                commonFile.setTableId(board.getId());
                commonFileService.writeFile(file, commonFile);
                commonFiles.add(commonFile);
            }
        }

        return new BoardResponse(board, commonFiles);
    }

    public BoardResponse updateBoard(String email, String encryptedUuid, Board boardDetails, List<MultipartFile> files) {

        Account account = redisProvider.getUserInfo(email)
            .orElseThrow(() -> new RuntimeException("UserInfo is null"));
        Board board = boardRepository.findByUuidAndAccountUuid(cryptoUtil.decrypt(encryptedUuid),
            account.getUuid()).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        board.setTitle(boardDetails.getTitle());
        board.setContent(boardDetails.getContent());

        boardRepository.save(board);
        List<CommonFile> commonFiles = null;
        if (!files.isEmpty()) {
            commonFiles = new ArrayList<>();
            for (MultipartFile file : files) {
                CommonFile commonFile = new CommonFile();
                commonFile.setTableName("board");
                commonFile.setTableId(board.getId());
                commonFileService.writeFile(file, commonFile);
                commonFiles.add(commonFile);
            }
        }

        return new BoardResponse(board, commonFiles);
    }

    public void deleteBoard(String email, String encryptedUuid) {

        Account account = redisProvider.getUserInfo(email)
            .orElseThrow(() -> new RuntimeException("UserInfo is null"));
        Board board = boardRepository.findByUuidAndAccountUuid(cryptoUtil.decrypt(encryptedUuid),
            account.getUuid()).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        boardRepository.delete(board);
    }

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
