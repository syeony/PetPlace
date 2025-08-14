package com.minjeok4go.petplace.image.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Tag(name = "Image Upload", description = "이미지 업로드 API")
@RestController
@RequestMapping("/api/upload")
public class ImageUploadController {

    private static final String UPLOAD_DIR = System.getProperty("os.name").toLowerCase().contains("win")
            ? "C:/Users/SSAFY/Desktop/test_folder/"
            : "/data/images/";

    @Operation(
            summary = "여러 이미지 업로드",
            description = "여러 이미지를 업로드하면 각각의 이미지 URL 배열을 반환합니다."
    )
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(
            @RequestParam("files") List<MultipartFile> files //테스트 해봐야됨 swagger는 자체 오류로인하여테스트 불가
    ) {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body("No files selected.");
        }

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String fileName = makeUniqueFileName(file.getOriginalFilename());
            File dest = new File(UPLOAD_DIR + fileName);
            try {
                file.transferTo(dest);
                imageUrls.add("/images/" + fileName);
            } catch (IOException e) {
                return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
            }
        }
        return ResponseEntity.ok(Map.of("urls", imageUrls));
    }

    /** 파일명 중복 방지 (타임스탬프+랜덤) **/
    private String makeUniqueFileName(String originalName) {
        String ext = "";
        if (originalName != null && originalName.lastIndexOf('.') != -1) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
        }
        return System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6) + ext;
    }
}

