package com.example.lecture_B.controller;

import com.example.lecture_B.dto.LectureRequestDTO;
import com.example.lecture_B.dto.LectureResponseDTO;
import com.example.lecture_B.entity.CustomUser;
import com.example.lecture_B.entity.Lecture;
import com.example.lecture_B.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lectures")
public class LectureController {

    private final LectureService lectureService;

    @PostMapping("/{boardId}")
    public ResponseEntity<?> createLecture(@PathVariable Long boardId, @RequestBody LectureRequestDTO dto, @AuthenticationPrincipal CustomUser user) {
        try {
            // 강의 생성
            Lecture lecture = lectureService.createLecture(dto, boardId, user);

            return ResponseEntity.ok("강의 생성 완료 : " + lecture);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("강의 생성 실패: " + e.getMessage());
        }
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<Lecture>> getLecturesByBoardId(@PathVariable Long boardId) {
        return ResponseEntity.ok(lectureService.getLectures(boardId));
    }

    @DeleteMapping("/{lectureId}")
    public ResponseEntity<String> deleteLecture(@PathVariable Long lectureId) {
        lectureService.deleteLecture(lectureId);
        return ResponseEntity.ok("강의 삭제 성공.");
    }
}
