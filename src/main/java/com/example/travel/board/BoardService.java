package com.example.travel.board;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    @Autowired
    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public List<Board> getAllBoards() {
        return boardRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public Board getBoardById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    }
    
    public Board getBoardByUuid(String uuid) {
        return boardRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    }
    
    public Board createBoard(Board board) {
        // UUID 자동 생성
        board.setUuid(UUID.randomUUID().toString());
        return boardRepository.save(board);
    }
    
    public Board updateBoard(String uuid, Board boardDetails) {
        Board board = getBoardByUuid(uuid);
        board.setTitle(boardDetails.getTitle());
        board.setContent(boardDetails.getContent());
        board.setAuthor(boardDetails.getAuthor());
        return boardRepository.save(board);
    }
    
    public void deleteBoard(String uuid) {
        Board board = getBoardByUuid(uuid);
        boardRepository.delete(board);
    }
}
