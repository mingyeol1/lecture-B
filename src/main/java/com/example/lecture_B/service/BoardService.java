package com.example.lecture_B.service;


import com.example.lecture_B.dto.BoardDTO;
import com.example.lecture_B.dto.BoardResponseDTO;
import com.example.lecture_B.entity.Board;

import java.util.List;

public interface BoardService {

    //게시판 생성
    public Board createBoard(BoardDTO dto);

    //게시판 조회
    public BoardDTO getBoard(Long boardId);

    //모든 게시판 조회
    public List<BoardResponseDTO> getAllBoards();

    //필요하지 않는 게시판은 삭제
    public void deleteBoard(Long boardId);
}
