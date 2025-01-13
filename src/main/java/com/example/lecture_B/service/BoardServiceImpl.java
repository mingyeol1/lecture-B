package com.example.lecture_B.service;

import com.example.lecture_B.dto.BoardDTO;
import com.example.lecture_B.dto.BoardResponseDTO;
import com.example.lecture_B.dto.LectureResponseDTO;
import com.example.lecture_B.entity.Board;
import com.example.lecture_B.repository.BoardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

   //포스트맨 조회시 순환참조 때문에 계속 늘어남
//    public Board getBoard(Long boardId) {
//        return boardRepository.findById(boardId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판"));
//    }

    //생성된 게시판 조회.
    @Override
    public BoardDTO getBoard(Long boardId) {
        // 게시판 조회
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판"));

        // Lecture 엔티티를 LectureResponseDTO로 변환
        List<LectureResponseDTO> lectureResponseDTOs = board.getLectures().stream()
                .map(lecture -> modelMapper.map(lecture, LectureResponseDTO.class))

                .collect(Collectors.toList());

        // Board 엔티티를 BoardDTO로 변환
        return new BoardDTO(board.getId(), board.getName(), lectureResponseDTOs);
    }

    //모든 게시판 조회 순환 참조 때문에 주석.
//    @Override
//    public List<Board> getAllBoards() {
//        return boardRepository.findAll();
//    }

    @Override
    public List<BoardResponseDTO> getAllBoards() {
        List<Board> boards = boardRepository.findAll();

        return boards.stream()
                .map(board -> {
                    BoardResponseDTO boardDTO = modelMapper.map(board, BoardResponseDTO.class);

                    // 강의 리스트 변환
                    List<LectureResponseDTO> lectures = board.getLectures().stream()
                            .map(lecture -> {
                                LectureResponseDTO lectureDTO = modelMapper.map(lecture, LectureResponseDTO.class);
                                lectureDTO.setBoardName(board.getName());   //자동으로 매핑이 되지않아서 직접 값을 넣음
                                lectureDTO.setUploaderNickname(lecture.getUser().getNickname()); //자동으로 매핑이 되지않아서 직접 값을 넣음
                                return lectureDTO;
                            })
                            .collect(Collectors.toList());

                    boardDTO.setLectures(lectures);
                    return boardDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBoard(Long boardId) {
        boardRepository.deleteById(boardId);
    }
}
