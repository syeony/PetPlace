// src/main/java/com/minjeok4go/petplace/image/controller/PublicImageController.java
package com.minjeok4go.petplace.image.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/images")
public class PublicImageController {

    private static final Path BASE = Paths.get("/data/images"); // 실제 저장 경로

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> get(@PathVariable String filename) throws Exception {
        Path p = BASE.resolve(filename).normalize();
        // 디렉토리 탈출 방지 + 존재 확인
        if (!p.startsWith(BASE) || !Files.exists(p) || Files.isDirectory(p)) {
            return ResponseEntity.notFound().build();
        }
        String ctype = Files.probeContentType(p);
        MediaType mt = (ctype != null) ? MediaType.parseMediaType(ctype) : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .contentType(mt)
                .body(new FileSystemResource(p));
    }
}
