package com.example.lecture_B.service;

import com.example.lecture_B.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;
    private final S3Client s3Client;

    /**
     * S3에 이미지 업로드
     */
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
                        .bucket(bucket)
                        .key(s3Path)
                        .contentType(file.getContentType())
                        .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes())
        );

        // 업로드된 파일의 S3 URL 반환
        return "https://" + bucket + ".s3.amazonaws.com/" + s3Path;
    }
}
