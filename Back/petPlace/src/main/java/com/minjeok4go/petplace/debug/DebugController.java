package com.minjeok4go.petplace.debug;

import com.minjeok4go.petplace.user.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final LocationService locationService;

    @GetMapping("/gumi-regions")
    public String printGumiRegions() {
        locationService.printGumiRegionIds();
        return "구미시 지역 ID 목록이 로그에 출력되었습니다.";
    }
}
