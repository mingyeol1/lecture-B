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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    // 게시판 내 강의 목록 조회
    public List<Lecture> getLectures(Long boardId) {
        return lectureRepository.findByBoardId(boardId);
    }

    public void deleteLecture(Long lectureId, Long userId) {
        // 강의 조회
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다.: " + lectureId));

        // 강의를 올린 사용자의 ID 확인
        Long uploaderId = lecture.getUser().getId();

        // 현재 사용자 ID와 비교
        if (!uploaderId.equals(userId)) {
            throw new RuntimeException("유저가 달라 지울 권한이 없습니다.");
        }

        // 강의 삭제
        lectureRepository.deleteById(lectureId);
    }
}
