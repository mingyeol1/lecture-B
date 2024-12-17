package com.example.lecture_B.service;

import com.example.lecture_B.dto.UserDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class S3Service {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;
    private final S3Client s3Client; // AWS S3와 통신하기 위한 클라이언트.

    //S3에서 이미지 삭제

    public void deleteImage(String imageUrl) {
        if (imageUrl != null && imageUrl.contains(bucket + ".s3.amazonaws.com/")) {
            // URL에서 키 추출 (예: profile-images/xxx-xxx-xxx.jpg)
            String key = imageUrl.substring(imageUrl.indexOf(bucket + ".s3.amazonaws.com/")
                    + (bucket + ".s3.amazonaws.com/").length());

            // S3에서 파일 삭제
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket) // 삭제할 객체가 있는 버킷 이름
                    .key(key)   // 삭제할 객체의 키(경로)
                    .build());
        }
    }

    //S3에 이미지 업로드
    public String uploadImage(MultipartFile file) throws IOException {
        // 파일 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        // S3에 저장할 파일 경로 (예: profile-images/{UUID}.jpg)
        String s3Path = "profile-images/" + UUID.randomUUID() + extension;

        // 파일 업로드
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket) // 버킷 이름.
                        .key(s3Path)    // 저장될 경로와 파일명
                        .contentType(file.getContentType())
                        .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes())// 실제 파일 데이터
        );

        // 업로드된 파일의 S3 URL 반환
        return "https://" + bucket + ".s3.amazonaws.com/" + s3Path;
    }
}