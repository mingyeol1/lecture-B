package com.example.lecture_B.service;

import com.example.lecture_B.dto.BoardDTO;
import com.example.lecture_B.entity.Board;
import com.example.lecture_B.repository.BoardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final ModelMapper modelMapper;
    //강의 게시판 생성.
    @Override
    public Board createBoard(BoardDTO dto) {
        Board board = modelMapper.map(dto, Board.class);

        boardRepository.save(board);

        return board;
    }

    //생성된 게시판 조회.
    @Override
    public Board getBoard(Long boardId) {
        return boardRepository.findById(boardId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판"));
    }

    //모든 게시판 조회
    @Override
    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    @Override
    public void deleteBoard(Long boardId) {
        boardRepository.deleteById(boardId);
    }
}
