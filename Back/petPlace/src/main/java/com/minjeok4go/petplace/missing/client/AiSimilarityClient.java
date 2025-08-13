package com.minjeok4go.petplace.missing.client;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
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
@RequiredArgsConstructor
public class AiSimilarityClient {

    /**
     * aiWebClient:
     *  - WebClientConfig에서 만든 "AI 엔진 전용" WebClient 빈을 주입받는다.
     *  - @Qualifier 로 이름을 명시해 다른 WebClient 빈들과 충돌을 피한다.
     *  - baseUrl은 application.yml의 ai.similarity.base-url (예: http://similarity:8083)
     */
    private final @Qualifier("aiWebClient") WebClient aiWebClient;

    // ----------- 응답 DTO (FastAPI 스키마에 맞춘 최소 형태) -----------

    /**
     * /index/add 응답 바인딩용 DTO
     *  - ok: 처리 성공 여부
     *  - id: 우리가 보낸 image_id 그대로 반환 (인덱스에 저장된 키)
     *  - latency_ms: 서버 처리 지연(밀리초)
     *  - breed: 개일 때만 품종 추정 결과 {"eng": "...", "ko": "..."} (없으면 null)
     *  - msg: 오류 메시지(실패 시)
     */
    @Getter @Setter
    public static class IndexAddResp {
        private boolean ok;
        private Long id;
        private Integer latency_ms;
        private Map<String,String> breed;
        private String msg;
    }

    /**
     * /search 응답 바인딩용 DTO
     *  - ok: 처리 성공 여부
     *  - results: [{id, score} ...] (id는 인덱스에 저장된 image_id = 우리 DB의 Image.id)
     *  - latency_ms: 서버 처리 지연(밀리초)
     *  - breed_query: 쿼리 이미지(개일 때)에서 추정한 품종 {"eng","ko"} (없으면 null)
     *  - msg: 오류 메시지(실패 시)
     */
    @Getter @Setter
    public static class SearchResp {
        @Getter @Setter public static class Item { Long id; double score; }
        private boolean ok;
        private List<Item> results;
        private Integer latency_ms;
        private Map<String,String> breed_query;
        private String msg;
    }

    // ----------- 유틸: 이미지 바이트 확보 -----------

    /**
     * 원격/정적 URL에서 바이트를 가져온다.
     *  - 스프링 정적 경로(file-server), S3 퍼블릭 URL 등에서 이미지 바이트를 내려받을 때 사용.
     *  - 반환형 Mono<byte[]> 이므로, 호출부에서 .block() 하거나 리액티브 방식으로 구독해야 실제 요청이 발생한다.
     */
    public Mono<byte[]> fetchBytes(String absoluteUrl) {
        return aiWebClient.get()
                // 절대 URL을 직접 지정. baseUrl을 무시하고 이 URL로 요청을 보낸다.
                .uri(URI.create(absoluteUrl))
                .retrieve()
                .bodyToMono(byte[].class);
    }

    // ----------- API: /index/add (인덱싱) -----------

    /**
     * 인덱싱 API 호출.
     *
     * @param imageId  FastAPI 인덱스에 저장할 고유 키 (== 우리 DB의 Image.id)
     * @param species  "dog" 또는 "cat"
     * @param imageBytes  업로드할 이미지 바이트 (JPG/PNG 등)
     * @param wFace    (선택) 얼굴 가중치 (null이면 서버 기본값 사용)
     * @return         IndexAddResp (ok=true면 인덱싱 성공)
     *
     * 전송 형식:
     *  - multipart/form-data
     *    - image_id: Long (text field)
     *    - species:  String (text field)
     *    - file:     Binary (file part)
     *    - w_face:   Double (text field, optional)
     */
    public Mono<IndexAddResp> indexAdd(long imageId, String species, byte[] imageBytes, Double wFace) {
        // 멀티파트 바디 구성: 폼 필드와 파일 파트를 함께 담는다.
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("image_id", String.valueOf(imageId));
        parts.add("species", species);
        // ByteArrayResource로 메모리 상의 바이트를 파일 파트처럼 보낸다.
        parts.add("file", new ByteArrayResource(imageBytes) {
            @Override public String getFilename() { return "img.jpg"; } // 파일명 힌트 (서버는 이름에 의존하지 않음)
        });
        if (wFace != null) parts.add("w_face", String.valueOf(wFace));

        // WebClient로 POST /index/add 호출
        return aiWebClient.post()
                .uri("/index/add")
                .contentType(MediaType.MULTIPART_FORM_DATA)           // 반드시 멀티파트로 지정
                .body(BodyInserters.fromMultipartData(parts))          // 위에서 만든 파트 바인딩
                .retrieve()                                            // 응답 추출
                .bodyToMono(IndexAddResp.class);                       // JSON → DTO 매핑
    }

    // ----------- API: /search (검색) -----------

    /**
     * 검색 API 호출.
     *
     * @param species     "dog" 또는 "cat"
     * @param imageBytes  쿼리 이미지 바이트
     * @param topk        상위 몇 개까지 가져올지 (null이면 서버 기본값)
     * @param wFace       얼굴 가중치 (null이면 서버 기본값)
     * @return            SearchResp (results: [{id, score}, ...])
     *
     * 전송 형식:
     *  - multipart/form-data
     *    - species: String
     *    - file:    Binary
     *    - topk:    Int (optional)
     *    - w_face:  Double (optional)
     *
     * 참고:
     *  - bbox 좌표(xmin/ymin/xmax/ymax)를 쓰고 싶으면 parts.add(...)로 필드만 추가하면 된다.
     *  - 반환되는 id는 인덱싱 때 넣은 image_id이므로, 우리 DB의 Image.id와 1:1로 매핑된다.
     */
    public Mono<SearchResp> search(String species, byte[] imageBytes, Integer topk, Double wFace) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("species", species);
        parts.add("file", new ByteArrayResource(imageBytes) {
            @Override public String getFilename() { return "q.jpg"; }
        });
        if (topk != null)  parts.add("topk", String.valueOf(topk));
        if (wFace != null) parts.add("w_face", String.valueOf(wFace));

        return aiWebClient.post()
                .uri("/search")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(parts))
                .retrieve()
                .bodyToMono(SearchResp.class);
    }
}
