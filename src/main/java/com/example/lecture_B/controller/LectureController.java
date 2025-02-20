package com.example.lecture_B.controller;

import com.example.lecture_B.dto.LectureResponseDTO;
import com.example.lecture_B.dto.LectureSearchDTO;
import com.example.lecture_B.entity.CustomUser;
import com.example.lecture_B.service.LectureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards/{boardId}/lectures") // boardId를 포함한 기본 URL 설정
@Log4j2
public class LectureController {

    private final LectureService lectureService;

    // 🔹 강의 생성
    @PostMapping("/create")
    public ResponseEntity<?> createLecture(
            @PathVariable Long boardId,
            @RequestPart("lecture") String lectureString,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @AuthenticationPrincipal CustomUser user) {

        try {
            lectureService.createLecture(boardId, user, lectureString, images, video);
            return ResponseEntity.ok("강의 생성 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("처리 중 오류 발생: " + e.getMessage());
        }
    }

    // 🔹 강의 수정
    @PutMapping("/modify/{lectureId}")
    public ResponseEntity<?> modifyLecture(
            @PathVariable Long boardId,  // boardId 추가
            @PathVariable Long lectureId,
            @RequestPart("lecture") String lectureString,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @AuthenticationPrincipal CustomUser user) {
        try {
            lectureService.modifyLectures(lectureId, lectureString, images, video, user);
            return ResponseEntity.ok("강의가 성공적으로 수정되었습니다.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("파일 처리 중 오류 발생: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류 발생: " + e.getMessage());
        }
    }

    // 🔹 특정 board의 강의 목록 불러오기 (페이지네이션 추가)
    @GetMapping("/all")
    public ResponseEntity<Page<LectureResponseDTO>> getLecturesByBoardId(
            @PathVariable Long boardId,
            @RequestParam(defaultValue = "0") int page,  // 기본 페이지 0
            @RequestParam(defaultValue = "10") int size) {  // 기본 사이즈 10

        Page<LectureResponseDTO> lectures = lectureService.getLecturesByBoardId(boardId, page, size);
        return ResponseEntity.ok(lectures);
    }

    // 🔹 특정 강의 상세 조회
    @GetMapping("/{lectureId}")
    public ResponseEntity<LectureResponseDTO> getLectureDetail(
            @PathVariable Long boardId,  // boardId 추가
            @PathVariable Long lectureId) {
        return ResponseEntity.ok(lectureService.getLectures(lectureId));
    }

    // 🔹 강의 삭제
    @DeleteMapping("/remove/{lectureId}")
    public ResponseEntity<?> deleteLecture(
            @PathVariable Long boardId,  // boardId 추가
            @PathVariable Long lectureId,
            @AuthenticationPrincipal CustomUser currentUser) {
        try {
            lectureService.deleteLecture(lectureId, currentUser.getId());
            return ResponseEntity.ok("강의가 성공적으로 삭제되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<LectureSearchDTO>> searchLectures(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<LectureSearchDTO> results = lectureService.searchLectures(keyword, page, size);
        return ResponseEntity.ok(results);
    }
}
