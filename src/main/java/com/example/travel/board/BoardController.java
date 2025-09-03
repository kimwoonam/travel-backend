package com.example.travel.board;

import com.example.travel.config.UuidCryptoUtil;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boards")
@CrossOrigin(origins = "*")
public class BoardController {

    private static final Logger log = LoggerFactory.getLogger(BoardController.class);
    private final BoardService boardService;
    private final UuidCryptoUtil uuidCryptoUtil;

    @Autowired
    public BoardController(BoardService boardService, UuidCryptoUtil uuidCryptoUtil) {
        this.boardService = boardService;
        this.uuidCryptoUtil = uuidCryptoUtil;
    }

    @GetMapping
    public ResponseEntity<List<Board>> getAllBoards() {
        List<Board> boards = boardService.getAllBoards();
        return ResponseEntity.ok(boards);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Board> getBoardByUuid(@PathVariable String uuid) {

        log.info("getBoardByUuid : {}", uuid);

        try {
            Board board;

            // UUID가 암호화된 형태인지 확인
            if (uuidCryptoUtil.isEncryptedUuid(uuid)) {
                // 암호화된 UUID로 직접 조회
                String uuidDecrypted = uuidCryptoUtil.decryptUuid(uuid);
                board = boardService.getBoardByUuid(uuidDecrypted);
                log.debug("암호화된 UUID로 조회: {}", uuid);
            } else {
                // 일반 UUID로 조회 후 암호화하여 반환
                log.error("암호화 되지않은 UUID로 조회: {}", uuid);
                throw new RuntimeException("게시글을 찾을 수 없습니다.");
            }

            return ResponseEntity.ok(board);
        } catch (RuntimeException e) {
            log.error("게시글 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestBody Board board) {
        Board createdBoard = boardService.createBoard(board);
        return ResponseEntity.ok(createdBoard);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<Board> updateBoard(@PathVariable String uuid,
        @RequestBody Board boardDetails) {
        try {
            log.info("updateBoard - 받은 UUID: {}", uuid);
            Board updatedBoard = boardService.updateBoard(uuid, boardDetails);
            return ResponseEntity.ok(updatedBoard);
        } catch (RuntimeException e) {
            log.error("게시글 수정 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteBoard(@PathVariable String uuid) {
        try {
            log.info("deleteBoard - 받은 UUID: {}", uuid);
            boardService.deleteBoard(uuid);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("게시글 삭제 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/init")
    public ResponseEntity<String> initializeSampleData() {
        try {
            // 기존 데이터가 없을 때만 샘플 데이터 추가
            if (boardService.getAllBoards().isEmpty()) {
                Board board1 = new Board();
                board1.setTitle("여행 후기 - 제주도");
                board1.setContent(
                    "제주도 여행을 다녀왔습니다. 한라산 등반과 해변 산책이 정말 멋졌어요. 특히 성산일출봉에서 본 일출은 잊을 수 없습니다.");
                board1.setAuthor("여행자1");

                Board board2 = new Board();
                board2.setTitle("맛집 추천 - 부산");
                board2.setContent("부산 여행 중 발견한 맛집들을 소개합니다. 해운대 해산물, 광안리 회, 서면 닭갈비 등 정말 맛있었어요!");
                board2.setAuthor("맛집탐험가");

                Board board3 = new Board();
                board3.setTitle("서울 관광지 추천");
                board3.setContent(
                    "서울 여행 필수 코스! 경복궁, 남산타워, 홍대, 명동 등을 추천합니다. 특히 봄철 벚꽃이 피는 시기가 최고예요.");
                board3.setAuthor("서울가이드");

                boardService.createBoard(board1);
                boardService.createBoard(board2);
                boardService.createBoard(board3);

                return ResponseEntity.ok("샘플 데이터가 추가되었습니다.");
            } else {
                return ResponseEntity.ok("이미 데이터가 존재합니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("샘플 데이터 추가 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
