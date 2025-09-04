package com.example.travel.board;

import com.example.travel.config.UuidCryptoUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BoardService {

    private static final Logger log = LoggerFactory.getLogger(BoardService.class);
    private final BoardRepository boardRepository;
    private final UuidCryptoUtil uuidCryptoUtil;

    @Autowired
    public BoardService(BoardRepository boardRepository, UuidCryptoUtil uuidCryptoUtil) {
        this.boardRepository = boardRepository;
        this.uuidCryptoUtil = uuidCryptoUtil;
    }

    public List<Board> getAllBoards() {
        List<Board> boards = boardRepository.findAllByOrderByCreatedAtDesc();
        // 조회 시 UUID를 암호화하여 반환
        boards.forEach(board -> {
            board.setUuid(uuidCryptoUtil.encryptUuid(board.getUuid()));
        });
        return boards;
    }

    public Board getBoardById(Long id) {
        return boardRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    }

    public Board getBoardByUuid(String uuid) {
        log.debug("getBoardByUuid : {}", uuid);
        Board board = boardRepository.findByUuid(uuid)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 조회 시 UUID를 암호화하여 반환
        board.setUuid(uuidCryptoUtil.encryptUuid(board.getUuid()));

        return board;
    }

    public Board createBoard(Board board) {
        // UUID 자동 생성
        String uuid = UUID.randomUUID().toString();
        board.setUuid(uuid);

        return boardRepository.save(board);
    }

    public Board updateBoard(String encryptedUuid, Board boardDetails) {
        // 암호화된 UUID를 복호화하여 실제 UUID 획득
        String actualUuid = uuidCryptoUtil.decryptUuid(encryptedUuid);
        log.debug("updateBoard - 복호화된 UUID: {}", actualUuid);

        Board board = boardRepository.findByUuid(actualUuid)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        board.setTitle(boardDetails.getTitle());
        board.setContent(boardDetails.getContent());
        board.setAuthor(boardDetails.getAuthor());

        return boardRepository.save(board);
    }

    public void deleteBoard(String encryptedUuid) {
        // 암호화된 UUID를 복호화하여 실제 UUID 획득
        String actualUuid = uuidCryptoUtil.decryptUuid(encryptedUuid);
        log.debug("deleteBoard - 복호화된 UUID: {}", actualUuid);

        Board board = boardRepository.findByUuid(actualUuid)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        boardRepository.delete(board);
    }

    public void deleteBoardBulk(List<String> encryptedUuids) {

        List<String> uuids = new ArrayList<>();
        encryptedUuids.forEach(encryptedUuid -> {
            uuids.add(uuidCryptoUtil.decryptUuid(encryptedUuid));
            log.debug("deleteBoardBulk - 복호화된 UUID: {}", uuidCryptoUtil.decryptUuid(encryptedUuid));
        });

        log.debug("uuids.size: {}", uuids.size());

        boardRepository.deleteByUuidIn(uuids);
    }
}
