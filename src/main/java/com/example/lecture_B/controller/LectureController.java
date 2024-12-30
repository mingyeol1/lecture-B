package com.example.lecture_B.controller;

import com.example.lecture_B.dto.BoardDTO;
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
            @RequestPart("lecture") String lectureString,       //lecture라는 이름을 String 타입으로 받음.
            @RequestPart(value = "images", required = false) List<MultipartFile> images,  //images라는 이름을 List<MultipartFile> 타입으로 받음.  required = false 파일이 없어도 요청이 유효함.
            @RequestPart(value = "video", required = false) MultipartFile video, //video라는 이름을 MultipartFile로 받음. required = false 파일이 없어도 요청이 유효함.
            @AuthenticationPrincipal CustomUser user) {     //현재 로그인한 사용자 객체 받아오기

        try {
            // 이미지 업로드 처리.
            List<String> imageUrls = new ArrayList<>();
            if (images != null && !images.isEmpty()) {  //이미지가 비어있지 않으면 저장.
                imageUrls = s3Service.uploadImages(images);
            }

            String videoUrl = null;
            if (video != null && !video.isEmpty()) {    //비디오가 비어있지 않으면 저장.
                // 디버깅을 위한 로그 추가3
                log.info("비디오 파일 이름: " + video.getOriginalFilename());
                log.info("비디오 파일 크기: " + video.getSize());
                log.info("비디오 컨텐츠 타입: " + video.getContentType());

                videoUrl = s3Service.uploadVideo(video);
                // 업로드된 URL 확인
                log.info("업로드된 비디오 URL : " + videoUrl);
            } else {
                log.info("비디오 파일이 전송되지 않았습니다.");
            }

            //lectureString을 LectureRequestDTO로 변환
            //ObjectMapper는 JSON 데이터를 Java객체로 변환해줌.
            ObjectMapper objectMapper = new ObjectMapper();
            LectureRequestDTO dto = objectMapper.readValue(lectureString, LectureRequestDTO.class);

            //강의 생성.
            Lecture lecture = lectureService.createLecture(dto, boardId, user, imageUrls, videoUrl);
            return ResponseEntity.ok("강의 생성 완료 : " + lecture);
        } catch (Exception e) {
            e.printStackTrace(); // 상세한 에러 로그 확인
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("처리 중 오류 발생: " + e.getMessage() + "\n" + e.getClass().getName());
        }
    }



    @GetMapping("/{lectureId}")
    public ResponseEntity<LectureResponseDTO> getLecturesByBoardId(@PathVariable Long lectureId) {
        return ResponseEntity.ok(lectureService.getLectures(lectureId));
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
