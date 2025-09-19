package com.example.travel.board;

import com.example.travel.board.dto.BoardDto.BoardResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 보드 작업과 관련된 HTTP 요청을 처리하는 컨트롤러 클래스입니다.
 * CRUD 작업과 대량 삭제, 샘플 데이터 초기화와 같은 추가 기능을 위한 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private static final Logger log = LoggerFactory.getLogger(BoardController.class);

    private final BoardService boardService;

    @Autowired
    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    /**
     * 데이터베이스에서 모든 게시판 목록을 검색합니다.
     * 게시판은 생성일을 기준으로 내림차순으로 정렬되며 UUID는 반환되기 전에 암호화됩니다.
     *
     * @return 암호화된 UUID가 있는 보드 목록을 포함하는 ResponseEntity입니다. 생성일을 기준으로 내림차순으로 정렬됩니다.
     */
    @GetMapping
    public ResponseEntity<List<Board>> getAllBoards() {
        List<Board> boards = boardService.getAllBoards();
        return ResponseEntity.ok(boards);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Board> getBoardByUuid(@PathVariable String uuid) {

        try {
            return ResponseEntity.ok(boardService.getBoardByUuid(uuid));
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @Transactional
    public ResponseEntity<BoardResponse> createBoard(HttpServletRequest request,
        @ModelAttribute Board board, @RequestParam("file") List<MultipartFile> files) {

        return ResponseEntity.ok(
            boardService.createBoard(board, request.getAttribute("email").toString(), files));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<BoardResponse> updateBoard(HttpServletRequest request,
        @PathVariable String uuid,
        @ModelAttribute Board boardDetails, @RequestParam("file") List<MultipartFile> files) {

        try {
            return ResponseEntity.ok(
                boardService.updateBoard(boardService.getEmail(request), uuid, boardDetails,
                    files));
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteBoard(HttpServletRequest request, @PathVariable String uuid) {
        try {
            boardService.deleteBoard(boardService.getEmail(request), uuid);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/bulk/{uuid}")
    @Transactional
    public ResponseEntity<Void> deleteBoardByUuids(HttpServletRequest request,
        @PathVariable String uuid) {

        try {
            boardService.deleteBoardBulk(boardService.getEmail(request), uuid);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 애플리케이션의 샘플 데이터를 초기화합니다.
     * 시스템에 데이터가 없는 경우 이 메서드는 미리 정의된 샘플 데이터 항목을 생성하여 추가합니다.
     * 데이터가 이미 있는 경우 새로운 항목은 추가되지 않습니다.
     *
     * @return 샘플 데이터가 추가되면 성공 메시지를 포함하는 ResponseEntity, 데이터가 이미 존재함을 나타내는 메시지,
     *         데이터 초기화 중 예외가 발생하면 오류 메시지를 포함하는 ResponseEntity입니다.
     */
    @PostMapping("/init")
    public ResponseEntity<String> initializeSampleData(HttpServletRequest request) {

        try {

            String email = boardService.getEmail(request);

            // 기존 데이터가 없을 때만 샘플 데이터 추가
            if (boardService.getAllBoards().isEmpty()) {
                Board board1 = new Board();
                board1.setTitle("여행 후기 - 제주도");
                board1.setContent(
                    "제주도 여행을 다녀왔습니다. 한라산 등반과 해변 산책이 정말 멋졌어요. 특히 성산일출봉에서 본 일출은 잊을 수 없습니다.");
                board1.setNickName("여행자1");

                Board board2 = new Board();
                board2.setTitle("맛집 추천 - 부산");
                board2.setContent("부산 여행 중 발견한 맛집들을 소개합니다. 해운대 해산물, 광안리 회, 서면 닭갈비 등 정말 맛있었어요!");
                board2.setNickName("맛집탐험가");

                Board board3 = new Board();
                board3.setTitle("서울 관광지 추천");
                board3.setContent(
                    "서울 여행 필수 코스! 경복궁, 남산타워, 홍대, 명동 등을 추천합니다. 특히 봄철 벚꽃이 피는 시기가 최고예요.");
                board3.setNickName("서울가이드");

                boardService.createBoard(board1, email);
                boardService.createBoard(board2, email);
                boardService.createBoard(board3, email);

                return ResponseEntity.ok("샘플 데이터가 추가되었습니다.");
            } else {
                return ResponseEntity.ok("이미 데이터가 존재합니다.");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body("샘플 데이터 추가 중 오류가 발생했습니다.");
        }
    }
}
