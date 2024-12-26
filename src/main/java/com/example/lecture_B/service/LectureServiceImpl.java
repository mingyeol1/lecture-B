package com.example.lecture_B.service;

import com.example.lecture_B.dto.LectureRequestDTO;
import com.example.lecture_B.dto.LectureResponseDTO;
import com.example.lecture_B.entity.Board;
import com.example.lecture_B.entity.CustomUser;
import com.example.lecture_B.entity.Lecture;
import com.example.lecture_B.entity.User;
import com.example.lecture_B.repository.BoardRepository;
import com.example.lecture_B.repository.LectureRepository;
import com.example.lecture_B.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;  // UserRepository 추가
    private final ModelMapper modelMapper;

    public Lecture createLecture(LectureRequestDTO dto, Long boardId, CustomUser customUser) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시판이 존재하지 않음.: " + boardId));

        // userId로 User 엔티티를 찾아옴
        User user = userRepository.findByUserId(customUser.getUserId())
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않음."));

        Lecture lecture = modelMapper.map(dto, Lecture.class);
        lecture.setBoard(board);
        lecture.setUser(user);  // User 엔티티 설정
        lecture.setCreatedAt(LocalDateTime.now());

        lectureRepository.save(lecture);
        return lecture;
    }
}
