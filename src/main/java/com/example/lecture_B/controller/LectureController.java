package com.example.lecture_B.controller;

import com.example.lecture_B.dto.LectureRequestDTO;
import com.example.lecture_B.dto.LectureResponseDTO;
import com.example.lecture_B.entity.CustomUser;
import com.example.lecture_B.entity.Lecture;
import com.example.lecture_B.service.LectureService;
import com.example.lecture_B.service.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lectures")
@Log4j2
public class LectureController {

    private final LectureService lectureService;
    private final S3Service s3Service;
    private final ModelMapper modelMapper;

    @PostMapping("/{boardId}")
    public ResponseEntity<?> createLecture(
            @PathVariable Long boardId,
            @RequestPart("lecture") String lectureString,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @AuthenticationPrincipal CustomUser user) {

        try {
            List<String> imageUrls = new ArrayList<>();
            if (images != null && !images.isEmpty()) {
                imageUrls = s3Service.uploadImages(images);
            }

            String videoUrl = null;
            if (video != null && !video.isEmpty()) {
                // 디버깅을 위한 로그 추가3
                log.info("비디오 파일 이름: " + video.getOriginalFilename());
                log.info("비디오 파일 크기: " + video.getSize());
                log.info("비디오 컨텐츠 타입: " + video.getContentType());

                videoUrl = s3Service.uploadVideo(video);
                // 업로드된 URL 확인
                log.info("업로드된 비디오 URL: " + videoUrl);
            } else {
                log.info("비디오 파일이 전송되지 않았습니다.");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            LectureRequestDTO dto = objectMapper.readValue(lectureString, LectureRequestDTO.class);

            Lecture lecture = lectureService.createLecture(dto, boardId, user, imageUrls, videoUrl);
            return ResponseEntity.ok("강의 생성 완료 : " + lecture);
        } catch (Exception e) {
            e.printStackTrace(); // 상세한 에러 로그 확인
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("처리 중 오류 발생: " + e.getMessage() + "\n" + e.getClass().getName());
        }
    }



    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<Lecture>> getLecturesByBoardId(@PathVariable Long boardId) {
        return ResponseEntity.ok(lectureService.getLectures(boardId));
    }

    @DeleteMapping("/{lectureId}")
    public ResponseEntity<?> deleteLecture(@PathVariable Long lectureId, @AuthenticationPrincipal CustomUser currentUser) {
        try {
            // 현재 유저 ID 추출
            Long currentUserId = currentUser.getId();

            // 강의 삭제 요청
            lectureService.deleteLecture(lectureId, currentUserId);

            return ResponseEntity.ok("강의가 성공적으로 삭제되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
