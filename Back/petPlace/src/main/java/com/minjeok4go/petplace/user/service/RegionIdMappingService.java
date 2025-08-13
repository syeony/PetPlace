package com.minjeok4go.petplace.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class RegionIdMappingService {

    // GeoJSON ID -> DB ID 매핑 테이블
    private static final Map<Long, Long> GEOJSON_TO_DB_ID_MAP = new HashMap<>();
    
    static {
        // 구미시 지역 매핑 (GeoJSON ID -> DB ID)
        // 실제 GeoJSON 파일에서 나오는 ID들로 업데이트 필요
        GEOJSON_TO_DB_ID_MAP.put(37050710L, 4719068000L); // 진미동
        
        // 다른 구미시 동들도 매핑 (실제 GeoJSON 데이터 확인 후 업데이트)
        // 예시:
        // GEOJSON_TO_DB_ID_MAP.put(GeoJSON_ID, 4719069000L); // 양포동
        // GEOJSON_TO_DB_ID_MAP.put(GeoJSON_ID, 4719067000L); // 인동동
        // GEOJSON_TO_DB_ID_MAP.put(GeoJSON_ID, 4719051000L); // 송정동
        // ... 등등
        
        log.info("지역 ID 매핑 테이블 초기화 완료: {} 개 매핑", GEOJSON_TO_DB_ID_MAP.size());
    }

    /**
     * GeoJSON에서 나온 ID를 DB에 저장된 실제 ID로 변환
     */
    public Long mapGeoJsonIdToDbId(Long geoJsonId) {
        Long dbId = GEOJSON_TO_DB_ID_MAP.get(geoJsonId);
        if (dbId != null) {
            log.debug("ID 매핑: GeoJSON {} -> DB {}", geoJsonId, dbId);
            return dbId;
        }
        
        // 매핑되지 않은 경우 원래 ID 그대로 반환
        log.debug("매핑되지 않은 ID: {}", geoJsonId);
        return geoJsonId;
    }

    /**
     * 특정 지역의 매핑이 존재하는지 확인
     */
    public boolean hasMappingFor(Long geoJsonId) {
        return GEOJSON_TO_DB_ID_MAP.containsKey(geoJsonId);
    }
}