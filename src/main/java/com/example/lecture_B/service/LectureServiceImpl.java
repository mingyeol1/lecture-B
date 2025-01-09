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
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;  // UserRepository 추가
    private final ModelMapper modelMapper;
    private final S3Service s3Service;

    public Lecture createLecture(Long boardId, CustomUser customUser, String lectureString,
                                 List<MultipartFile> images, MultipartFile video) throws Exception {
        // 이미지 업로드 처리
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            imageUrls = s3Service.uploadImages(images);
        }

        // 비디오 업로드 처리
        String videoUrl = null;
        if (video != null && !video.isEmpty()) {
            videoUrl = s3Service.uploadVideo(video);
        }

        // lectureString을 DTO로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        LectureRequestDTO dto = objectMapper.readValue(lectureString, LectureRequestDTO.class);

        // 게시판 및 사용자 정보 조회
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시판이 존재하지 않음.: " + boardId));

        User user = userRepository.findByUserId(customUser.getUserId())
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않음."));

        // 강의 엔티티 생성 및 설정
        Lecture lecture = modelMapper.map(dto, Lecture.class);
        lecture.setBoard(board);
        lecture.setUser(user);
        lecture.setImagesUrl(imageUrls);
        lecture.setVideoUrl(videoUrl);
        lecture.setCreatedAt(LocalDateTime.now());

        // 강의 저장
        lectureRepository.save(lecture);

        return lecture;
    }


    //강의 수정.
    @Override
    public void modifyLectures(Long lectureId, String lectureString, List<MultipartFile> images,
                                MultipartFile video, CustomUser user) throws IOException {
        // 기존 강의 조회
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다: " + lectureId));

        // 권한 체크
        if (!lecture.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("해당 강의를 수정할 권한이 없습니다.");
        }

        // 새 이미지 처리
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            // 기존 이미지 삭제
            if (lecture.getImagesUrl() != null) {
                for (String oldImageUrl : lecture.getImagesUrl()) {
                    s3Service.deleteImage(oldImageUrl);
                }
            }
            imageUrls = s3Service.uploadImages(images);
        }

        // 새 비디오 처리
        String videoUrl = null;
        if (video != null && !video.isEmpty()) {
            // 기존 비디오 삭제
            if (lecture.getVideoUrl() != null) {
                s3Service.deleteImage(lecture.getVideoUrl());
            }
            videoUrl = s3Service.uploadVideo(video);
        }

        // lectureString을 DTO로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        LectureRequestDTO dto = objectMapper.readValue(lectureString, LectureRequestDTO.class);

        // 강의 수정
        lecture.update(
                dto.getTitle(),
                dto.getDescription(),
                !imageUrls.isEmpty() ? imageUrls : lecture.getImagesUrl(),
                videoUrl != null ? videoUrl : lecture.getVideoUrl(),
                LocalDateTime.now()
        );

        // 강의 저장
        lectureRepository.save(lecture);

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

    @Override
    public void deleteLecture(Long lectureId, Long userId) {
        // 강의 조회
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다: " + lectureId));

        // 강의를 올린 사용자의 ID 확인
        Long uploaderId = lecture.getUser().getId();

        // 현재 사용자 ID와 비교
        if (!uploaderId.equals(userId)) {
            throw new RuntimeException("유저가 달라 지울 권한이 없습니다.");
        }

        // 이미지 삭제 처리
        if (lecture.getImagesUrl() != null) {
            for (String oldImageUrl : lecture.getImagesUrl()) {
                s3Service.deleteImage(oldImageUrl);
            }
        }

        // 영상 삭제 처리
        if (lecture.getVideoUrl() != null) {
            s3Service.deleteImage(lecture.getVideoUrl());
        }

        // 강의 삭제
        lectureRepository.deleteById(lectureId);

    }
}
