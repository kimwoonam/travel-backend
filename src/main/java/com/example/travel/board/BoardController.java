package com.example.travel.board;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/boards")
@CrossOrigin(origins = "*")
public class BoardController {

    private final BoardService boardService;

    @Autowired
    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    public ResponseEntity<List<Board>> getAllBoards() {
        List<Board> boards = boardService.getAllBoards();
        return ResponseEntity.ok(boards);
    }
    
    @GetMapping("/{uuid}")
    public ResponseEntity<Board> getBoardByUuid(@PathVariable String uuid) {
        try {
            Board board = boardService.getBoardByUuid(uuid);
            return ResponseEntity.ok(board);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestBody Board board) {
        Board createdBoard = boardService.createBoard(board);
        return ResponseEntity.ok(createdBoard);
    }
    
    @PutMapping("/{uuid}")
    public ResponseEntity<Board> updateBoard(@PathVariable String uuid, @RequestBody Board boardDetails) {
        try {
            Board updatedBoard = boardService.updateBoard(uuid, boardDetails);
            return ResponseEntity.ok(updatedBoard);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteBoard(@PathVariable String uuid) {
        try {
            boardService.deleteBoard(uuid);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
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
                board1.setContent("제주도 여행을 다녀왔습니다. 한라산 등반과 해변 산책이 정말 멋졌어요. 특히 성산일출봉에서 본 일출은 잊을 수 없습니다.");
                board1.setAuthor("여행자1");
                
                Board board2 = new Board();
                board2.setTitle("맛집 추천 - 부산");
                board2.setContent("부산 여행 중 발견한 맛집들을 소개합니다. 해운대 해산물, 광안리 회, 서면 닭갈비 등 정말 맛있었어요!");
                board2.setAuthor("맛집탐험가");
                
                Board board3 = new Board();
                board3.setTitle("서울 관광지 추천");
                board3.setContent("서울 여행 필수 코스! 경복궁, 남산타워, 홍대, 명동 등을 추천합니다. 특히 봄철 벚꽃이 피는 시기가 최고예요.");
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
