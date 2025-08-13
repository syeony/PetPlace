package com.minjeok4go.petplace.user.service;

//(위치 정보 전달용 Helper 클래스)
import lombok.Getter;
import org.locationtech.jts.geom.Geometry;

@Getter
public class RegionData {
    private final long id;
    private final String name;
    private final Geometry geometry; // geometry는 내부 계산용으로만 사용됩니다.

    public RegionData(long id, String name, Geometry geometry) {
        this.id = id;
        this.name = name;
        this.geometry = geometry;
    }
}