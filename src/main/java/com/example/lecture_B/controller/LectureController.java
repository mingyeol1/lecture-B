package com.example.lecture_B.controller;

import com.example.lecture_B.dto.BoardDTO;
import com.example.lecture_B.dto.LectureRequestDTO;
import com.example.lecture_B.dto.LectureResponseDTO;
import com.example.lecture_B.entity.CustomUser;
import com.example.lecture_B.entity.Lecture;
import com.example.lecture_B.repository.LectureRepository;
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


    //강의 생성.
    @PostMapping("/{boardId}")
    public ResponseEntity<?> createLecture(
            @PathVariable Long boardId,
            @RequestPart("lecture") String lectureString,       //lecture라는 이름을 String 타입으로 받음.
            @RequestPart(value = "images", required = false) List<MultipartFile> images,  //images라는 이름을 List<MultipartFile> 타입으로 받음.  required = false 파일이 없어도 요청이 유효함.
            @RequestPart(value = "video", required = false) MultipartFile video, //video라는 이름을 MultipartFile로 받음. required = false 파일이 없어도 요청이 유효함.
            @AuthenticationPrincipal CustomUser user) {     //현재 로그인한 사용자 객체 받아오기

        try {
            // 강의 생성
            lectureService.createLecture(boardId, user, lectureString, images, video);

            return ResponseEntity.ok("강의 생성 완료 : ");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("처리 중 오류 발생: " + e.getMessage() + "\n" + e.getClass().getName());
        }
    }


    //강의 수정.
    @PutMapping("/{lectureId}")
    public ResponseEntity<?> modifyLecture(
            @PathVariable Long lectureId,
            @RequestPart("lecture") String lectureString,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @AuthenticationPrincipal CustomUser user
    ) {
        try {
            // 강의 수정
            lectureService.modifyLectures(lectureId, lectureString, images, video, user);
            return ResponseEntity.ok("강의가 성공적으로 수정되었습니다.");
        } catch (IOException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("파일 처리 중 오류가 발생했습니다: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류가 발생했습니다: " + e.getMessage());
        }
    }




    //강의 내용을 불러옴
    @GetMapping("/{lectureId}")
    public ResponseEntity<LectureResponseDTO> getLecturesByBoardId(@PathVariable Long lectureId) {
        return ResponseEntity.ok(lectureService.getLectures(lectureId));
    }


    //강의삭제
    @DeleteMapping("/{lectureId}")
    public ResponseEntity<?> deleteLecture(@PathVariable Long lectureId, @AuthenticationPrincipal CustomUser currentUser) {
        try {
            // 삭제
            lectureService.deleteLecture(lectureId, currentUser.getId());

            return ResponseEntity.ok("유저가 성공적으로 삭제 되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());  // 예외 처리
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

}
