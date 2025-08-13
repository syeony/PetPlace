package com.minjeok4go.petplace.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LocationService {

    private final List<RegionData> regionDataList = new ArrayList<>();
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @PostConstruct
    private void init() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("static/HangJeongDong_ver20250401.geojson")) {
            if (inputStream == null) {
                log.error("❌ 경계 데이터 파일을 찾을 수 없습니다: static/HangJeongDong_ver20250401.geojson");
                return;
            }
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode geoJson = mapper.readTree(inputStream);
            JsonNode features = geoJson.get("features");
            
            if (features == null || !features.isArray()) {
                log.error("❌ GeoJSON 파일 형식이 올바르지 않습니다.");
                return;
            }

            int loadedCount = 0;
            for (JsonNode feature : features) {
                try {
                    JsonNode properties = feature.get("properties");
                    if (properties == null) continue;
                    
                    JsonNode admCdNode = properties.get("adm_cd");
                    JsonNode admNmNode = properties.get("adm_nm");
                    
                    if (admCdNode == null || admNmNode == null) continue;
                    
                    String regionCodeStr = admCdNode.asText();
                    String regionName = admNmNode.asText();
                    
                    if (regionCodeStr.length() < 8) continue;
                    
                    // 행정동 코드는 8자리만 사용 (뒤의 자리는 불필요한 추가 코드)
                    String dongCode = regionCodeStr.length() >= 8 ? regionCodeStr.substring(0, 8) : regionCodeStr;
                    long regionId = Long.parseLong(dongCode);
                    
                    JsonNode geometry = feature.get("geometry");
                    if (geometry == null) continue;
                    
                    Geometry boundary = parseGeometry(geometry);
                    if (boundary != null) {
                        regionDataList.add(new RegionData(regionId, regionName, boundary));
                        loadedCount++;
                    }
                } catch (Exception e) {
                    log.warn("⚠️ 지역 데이터 파싱 실패: {}", e.getMessage());
                }
            }
            
            log.info("✅ {} 개의 지역 경계 데이터 로딩 완료!", loadedCount);
            
        } catch (Exception e) {
            log.error("❌ GeoJSON 파일 로딩 실패", e);
        }
    }
    
    /**
     * GeoJSON의 geometry를 JTS Geometry로 변환
     */
    private Geometry parseGeometry(JsonNode geometryNode) {
        try {
            String type = geometryNode.get("type").asText();
            JsonNode coordinates = geometryNode.get("coordinates");
            
            switch (type) {
                case "Polygon":
                    return createPolygonFromCoordinates(coordinates.get(0));
                case "MultiPolygon":
                    // MultiPolygon의 경우 첫 번째 폴리곤만 사용
                    return createPolygonFromCoordinates(coordinates.get(0).get(0));
                default:
                    log.warn("⚠️ 지원하지 않는 geometry 타입: {}", type);
                    return null;
            }
        } catch (Exception e) {
            log.warn("⚠️ Geometry 파싱 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 좌표 배열을 JTS Polygon으로 변환
     */
    private Polygon createPolygonFromCoordinates(JsonNode coordinateArray) {
        try {
            List<Coordinate> coordinates = new ArrayList<>();
            
            for (JsonNode coordNode : coordinateArray) {
                if (coordNode.size() >= 2) {
                    double lon = coordNode.get(0).asDouble();
                    double lat = coordNode.get(1).asDouble();
                    coordinates.add(new Coordinate(lon, lat));
                }
            }
            
            if (coordinates.size() < 4) {
                return null; // 폴리곤은 최소 4개의 점이 필요 (시작점과 끝점 동일)
            }
            
            // 폴리곤이 닫혀있지 않으면 첫 번째 점을 마지막에 추가
            if (!coordinates.get(0).equals(coordinates.get(coordinates.size() - 1))) {
                coordinates.add(new Coordinate(coordinates.get(0)));
            }
            
            Coordinate[] coordArray = coordinates.toArray(new Coordinate[0]);
            LinearRing shell = geometryFactory.createLinearRing(coordArray);
            
            return geometryFactory.createPolygon(shell);
            
        } catch (Exception e) {
            log.warn("⚠️ 폴리곤 생성 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * WGS84 좌표로 지역 찾기
     */
    public RegionData findRegionFromWgs84(double longitude, double latitude) {
        try {
            Point userPoint = geometryFactory.createPoint(new Coordinate(longitude, latitude));
            
            for (RegionData regionData : regionDataList) {
                if (regionData.getGeometry() != null && regionData.getGeometry().contains(userPoint)) {
                    return regionData;
                }
            }
            
            log.debug("🔍 좌표 ({}, {})에 해당하는 지역을 찾을 수 없음", longitude, latitude);
            return null;
            
        } catch (Exception e) {
            log.error("❌ 지역 검색 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    // ✅ [추가] 위도, 경도를 JTS의 Point 객체로 변환해주는 메소드
    public Point getPointFromWgs84(double longitude, double latitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    /**
     * 개발용: GeoJSON에서 구미시 지역들의 실제 ID 출력
     */
    public void printGumiRegionIds() {
        log.info("=== 구미시 지역 ID 목록 ===");
        for (RegionData regionData : regionDataList) {
            if (regionData.getName().contains("구미") || regionData.getName().contains("진미") || 
                regionData.getName().contains("양포") || regionData.getName().contains("인동")) {
                log.info("ID: {}, 이름: {}", regionData.getId(), regionData.getName());
            }
        }
        log.info("========================");
    }
}


