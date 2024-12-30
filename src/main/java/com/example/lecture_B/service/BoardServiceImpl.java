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
        Board board = boardRepository.findById(boardId) //board id로 게시판 조회.
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판"));

        // Lecture를 LectureResponseDTO로 변환
        List<LectureResponseDTO> lectureResponseDTO = board.getLectures().stream()// board 엔티티에 lectures필드를 가져옴.
                .map(lecture -> {   //스트림 내 각 lecture 엔티티를 DTO로 변환.
                    LectureResponseDTO dto = new LectureResponseDTO();     //Lecture 엔티티의 데이터를 DTO에 복사.
                    dto.setId(lecture.getId());
                    dto.setTitle(lecture.getTitle());
                    dto.setDescription(lecture.getDescription());
                    dto.setVideoUrl(lecture.getVideoUrl());
                    dto.setRating(lecture.getRating());
                    dto.setBoardName(board.getName()); // 현재 board의 이름
                    dto.setUploaderNickname(lecture.getUser().getNickname()); // 강의를 업로드한 유저의 닉네임
                    return dto;
                })
                .collect(Collectors.toList());

        // Board를 BoardDTO로 변환
        return new BoardDTO(board.getId(), board.getName(), lectureResponseDTO);
    }

    //모든 게시판 조회 순환 참조 때문에 주석.
//    @Override
//    public List<Board> getAllBoards() {
//        return boardRepository.findAll();
//    }

    @Override
    public List<BoardResponseDTO> getAllBoards() {
        List<Board> boards = boardRepository.findAll(); // 모든 게시판 조회
        return boards.stream()
                .map(board -> {
                    // 강의 리스트를 LectureResponseDTO로 변환
                    List<LectureResponseDTO> lectures = board.getLectures().stream()
                            .map(lecture -> {
                                LectureResponseDTO dto = new LectureResponseDTO();
                                dto.setId(lecture.getId());
                                dto.setTitle(lecture.getTitle());
                                dto.setDescription(lecture.getDescription());
                                dto.setVideoUrl(lecture.getVideoUrl());
                                dto.setRating(lecture.getRating());
                                dto.setBoardName(board.getName()); // 현재 board의 이름 설정
                                dto.setUploaderNickname(lecture.getUser().getNickname()); // 강의를 업로드한 유저의 닉네임 설정
                                return dto;
                            })
                            .collect(Collectors.toList());

                    // Board를 BoardResponseDTO로 변환
                    BoardResponseDTO dto = new BoardResponseDTO();
                    dto.setId(board.getId());
                    dto.setName(board.getName());
                    dto.setLectures(lectures);
                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Override
    public void deleteBoard(Long boardId) {
        boardRepository.deleteById(boardId);
    }
}
