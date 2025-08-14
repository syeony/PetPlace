package com.minjeok4go.petplace.missing.client;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * FastAPI 기반 유사도/검색 엔진(LostPet Similarity Service)을 호출하는 전용 클라이언트.
 *
 * - WebClient(리액티브 HTTP 클라이언트)로 내부 네트워크(http://similarity:8083)에 있는 FastAPI로 요청을 보낸다.
 * - 멀티파트 업로드(이미지 바이트 전송)와 일반 폼 필드(image_id, species, topk 등)를 함께 보낼 수 있다.
 * - 응답은 FastAPI 스펙에 맞는 최소한의 DTO(IndexAddResp, SearchResp)로 역직렬화한다.
 *
 * 사용처 예시:
 *   - 실종 신고 저장 직후: /index/add 로 이미지 인덱싱
 *   - 목격 제보 저장 직후: /search 로 후보 검색 → 결과 id로 MissingReport 매칭 생성
 */
@Service
public class AiSimilarityClient {
    private final WebClient ai;

    public AiSimilarityClient(@Qualifier("aiWebClient") WebClient ai) {
        this.ai = ai;
    }

    // ↓ 이하 메서드는 그대로
    public Mono<Map> indexAddPath(long imageId, String species, String path,
                                  Integer xmin,Integer ymin,Integer xmax,Integer ymax,
                                  Double wFace) {
        var parts = new LinkedMultiValueMap<String, Object>();
        parts.add("image_id", String.valueOf(imageId));
        parts.add("species", species);
        parts.add("path", path);
        if (xmin!=null) parts.add("xmin", String.valueOf(xmin));
        if (ymin!=null) parts.add("ymin", String.valueOf(ymin));
        if (xmax!=null) parts.add("xmax", String.valueOf(xmax));
        if (ymax!=null) parts.add("ymax", String.valueOf(ymax));
        if (wFace!=null) parts.add("w_face", String.valueOf(wFace));

        return ai.post().uri("/index/add_path")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(parts))
                .retrieve().bodyToMono(Map.class);
    }

    public Mono<SearchResp> searchPath(String species, String path, Integer topk, Double wFace,
                                       Integer xmin,Integer ymin,Integer xmax,Integer ymax) {
        var parts = new LinkedMultiValueMap<String, Object>();
        parts.add("species", species);
        parts.add("path", path);
        if (topk!=null) parts.add("topk", String.valueOf(topk));
        if (wFace!=null) parts.add("w_face", String.valueOf(wFace));
        if (xmin!=null) parts.add("xmin", String.valueOf(xmin));
        if (ymin!=null) parts.add("ymin", String.valueOf(ymin));
        if (xmax!=null) parts.add("xmax", String.valueOf(xmax));
        if (ymax!=null) parts.add("ymax", String.valueOf(ymax));

        return ai.post().uri("/search_path")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(parts))
                .retrieve().bodyToMono(SearchResp.class);
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchResp {
        @Getter @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Item {
            private Long id;
            private double score;
        }
        private boolean ok;
        private List<Item> results;
    }
}
