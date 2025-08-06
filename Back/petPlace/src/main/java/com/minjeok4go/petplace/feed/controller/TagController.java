package com.minjeok4go.petplace.feed.controller;

import com.minjeok4go.petplace.feed.dto.FeedDetailResponse;
import com.minjeok4go.petplace.feed.dto.FeedListResponse;
import com.minjeok4go.petplace.feed.dto.TagResponse;
import com.minjeok4go.petplace.feed.service.TagService;
import com.minjeok4go.petplace.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Tag API", description = "태그 API")
@RestController
@RequestMapping("/api/feeds/tags")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TagController {

    private final TagService tagService;
    @Operation(
            summary = "태그 전체 조회",
            description = "태그의 전체 목록을 반환합니다."
    )
    @GetMapping
    public List<TagResponse> getTagAll() {
        return tagService.getTagAll();
    }

    @Operation(
            summary = "태그 단건 조회",
            description = "Path 변수로 넘어온 태그 ID를 기준으로 태그를 반환합니다."
    )
    @GetMapping("/{id}")
    public TagResponse getTag(@PathVariable Long id) {
        return tagService.getTagById(id);
    }
}
