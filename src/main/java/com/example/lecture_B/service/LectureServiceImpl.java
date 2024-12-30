package com.example.lecture_B.service;

import com.example.lecture_B.dto.BoardDTO;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;  // UserRepository 추가
    private final ModelMapper modelMapper;

    public Lecture createLecture(LectureRequestDTO dto, Long boardId, CustomUser customUser, List<String> imageUrls, String videoUrl) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시판이 존재하지 않음.: " + boardId));

        User user = userRepository.findByUserId(customUser.getUserId())
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않음."));

        // 강의 엔티티 생성 및 설정
        Lecture lecture = modelMapper.map(dto, Lecture.class); //기본적인 강의 내용 설정.
        lecture.setBoard(board);    //게시판 정보
        lecture.setUser(user);      //사용자 정보.
        lecture.setImagesUrl(imageUrls); // 외부에서 값을 받아와 이미지 URL 리스트 저장
        lecture.setVideoUrl(videoUrl);  // 비디오 URL 저장
        lecture.setCreatedAt(LocalDateTime.now());

        lectureRepository.save(lecture);
        return lecture;
    }

    // 게시판 내 강의 목록 조회
    @Override
    public LectureResponseDTO getLectures(Long lectureId) {
        // 강의 조회
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의"));

        // LectureResponseDTO로 변환
        return convertToLectureResponseDTO(lecture);
    }

    private LectureResponseDTO convertToLectureResponseDTO(Lecture lecture) {
        // Lecture 엔티티를 LectureResponseDTO로 변환
        LectureResponseDTO dto = new LectureResponseDTO();
        dto.setId(lecture.getId());
        dto.setTitle(lecture.getTitle());
        dto.setDescription(lecture.getDescription());
        dto.setVideoUrl(lecture.getVideoUrl());
        dto.setRating(lecture.getRating());
        dto.setBoardName(lecture.getBoard().getName()); // 강의가 속한 게시판 이름
        dto.setUploaderNickname(lecture.getUser().getNickname()); // 강의를 업로드한 유저의 닉네임
        return dto;
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
