package com.example.lecture_B.controller;

import com.example.lecture_B.dto.BoardDTO;
import com.example.lecture_B.entity.Board;
import com.example.lecture_B.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<?> createBoard(@RequestBody BoardDTO dto) {
        //추후 ADMIN만 게시판 생성을 할 수 있게 수정.
        try {
             boardService.createBoard(dto);
             return ResponseEntity.ok("성공적으로 게시판이 생성되었습니다." + dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    //id 값으로 게시판 조회
    @GetMapping("/{boardId}")
    public ResponseEntity<Board> getBoardById(@PathVariable Long boardId) {
        return ResponseEntity.ok(boardService.getBoard(boardId));
    }
    //모든 게시판 조회.
    @GetMapping
    public ResponseEntity<List<Board>> getAllBoards() {
        return ResponseEntity.ok(boardService.getAllBoards());
    }
    //id값으로 게시판 삭제.
    @DeleteMapping("/{boardId}")
    public ResponseEntity<String> deleteBoard(@PathVariable Long boardId) {
        boardService.deleteBoard(boardId);
        return ResponseEntity.ok("게시판 삭제 성공");
    }
}
