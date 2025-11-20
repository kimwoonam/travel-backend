package com.moodo.travel.board;

import com.moodo.travel.account.AccountService;
import com.moodo.travel.board.dto.BoardDto.BoardResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/api/board")
public class BoardController {

    private static final Logger log = LoggerFactory.getLogger(BoardController.class);

    private final AccountService accountService;
    private final BoardService boardService;

    @Autowired
    public BoardController(AccountService accountService, BoardService boardService) {
        this.accountService = accountService;
        this.boardService = boardService;
    }

    /**
     * 데이터베이스에서 모든 게시판 목록을 검색합니다.
     * 게시판은 생성일을 기준으로 내림차순으로 정렬되며 UUID는 반환되기 전에 암호화됩니다.
     * 페이징을 지원합니다.
     *
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @param sort 정렬 기준 (기본값: createdAt,desc)
     * @return 암호화된 UUID가 있는 보드 목록을 포함하는 ResponseEntity입니다. 생성일을 기준으로 내림차순으로 정렬됩니다.
     */
    @GetMapping
    public ResponseEntity<?> getAllBoards(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size,
        @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort) {
        
        // 정렬 파라미터 파싱
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortObj = Sort.by(direction, sortParams[0]);
        
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<Board> boardPage = boardService.getAllBoards(pageable);
        
        return ResponseEntity.ok(boardPage);
    }

    /**
     * UUID를 기반으로 보드 엔터티를 검색합니다.
     *
     * @param uuid 검색할 게시판의 고유 식별자
     * @return 성공 시 검색된 보드을 포함하는 ResponseEntity를 반환하고, 오류가 발생하면 잘못된 요청 응답을 반환합니다.
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<BoardResponse> getBoardByUuid(@PathVariable String uuid) {

        try {
            return ResponseEntity.ok(boardService.getBoardByUuid(uuid));
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 새 게시판을 생성하여 데이터베이스에 저장합니다.
     * 이 메서드는 요청에서 사용자의 이메일을 검색하고, 관련 파일을 포함한 보드 정보를 처리하여 BoardService를 통해 저장합니다.
     *
     * @param request 인증된 사용자에 대한 정보가 포함된 HttpServletRequest
     * @param board 생성할 게시판의 세부 정보를 포함하는 보드 엔터티
     * @param files 게시판과 연관될 파일 목록
     * @return 성공하면 생성된 게시판의 세부 정보를 포함하는 ResponseEntity 또는 예외가 발생하면 오류 응답
     */
    @PostMapping
    @Transactional
    public ResponseEntity<BoardResponse> createBoard(HttpServletRequest request,
        @ModelAttribute Board board, @RequestParam(name = "files", required = false) List<MultipartFile> files) {

        try {
            String email = accountService.getEmail(request);
            return ResponseEntity.ok(boardService.create(board, email, files));
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 제공된 세부 정보로 기존 보드 항목을 업데이트합니다. 이 메서드는 업데이트된 보드 세부 정보를 처리하고,
     * 지정된 파일을 삭제하고, 업데이트 과정에서 새 파일을 보드에 연결합니다.
     *
     * @param request 인증 또는 컨텍스트 관련 정보가 포함된 HttpServletRequest
     * @param uuid 업데이트 할 게시판의 고유 식별자
     * @param boardDetail 모델 속성으로 제공된 업데이트된 게시판 세부 정보
     * @param deleteFileId 해당되는 경우 삭제할 파일의 식별자
     * @param files 게시판과 연관될 파일 목록
     * @return 성공 시 업데이트된 보드 세부 정보를 포함하는 ResponseEntity 또는 실패 시 잘못된 요청 응답
     */
    @PutMapping("/{uuid}")
    public ResponseEntity<BoardResponse> updateBoard(HttpServletRequest request,
        @PathVariable String uuid, @ModelAttribute Board boardDetail,
        @RequestParam(value = "deleteFileId", required = false) String deleteFileId,
        @RequestParam(value = "files", required = false) List<MultipartFile> files) {

        try {
            String email = accountService.getEmail(request);
            return ResponseEntity.ok(
                boardService.update(email, uuid, boardDetail, deleteFileId, files));
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * UUID로 식별된 게시판을 삭제합니다. 요청하는 사용자가 요청에 포함된 이메일의 유효성을 검사하여 권한이 있는지 확인합니다.
     *
     * @param request 사용자 인증 세부 정보가 포함된 HttpServletRequest
     * @param uuid 삭제할 게시판의 고유 식별자
     * @return 작업이 성공하면 HTTP 상태 200을 갖는 ResponseEntity, 실패하면 HTTP 상태 400을 갖는 ResponseEntity
     */
    @DeleteMapping("/{uuid}")
    @Transactional
    public ResponseEntity<Void> deleteBoard(HttpServletRequest request, @PathVariable String uuid) {
        try {
            String email = accountService.getEmail(request);
            boardService.delete(email, uuid);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * UUID로 식별된 여러 게시판을 삭제합니다.
     * 이 작업은 대량으로 수행되며 트랜잭션 방식으로 진행되므로 오류 발생 시 모든 게시판이 삭제되거나 아무것도 삭제되지 않습니다.
     *
     * @param request 인증 또는 세션 세부 정보가 포함된 HTTP 요청
     * @param uuids 삭제할 보드을 나타내는 쉼표로 구분된 UUID 문자열
     * @return 성공 시 HTTP 204 No Content가 포함된 ResponseEntity,
     *         실패 시 HTTP 400 Bad Request가 포함된 ResponseEntity
     */
    @DeleteMapping("/bulk/{uuids}")
    @Transactional
    public ResponseEntity<Void> deleteBoardByUuids(HttpServletRequest request,
        @PathVariable String uuids) {

        try {
            String email = accountService.getEmail(request);
            boardService.deleteBulk(email, uuids);
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

            String email = accountService.getEmail(request);

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

                boardService.create(board1, email);
                boardService.create(board2, email);
                boardService.create(board3, email);

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
