package com.minjeok4go.petplace.user.service;

import com.minjeok4go.petplace.pet.entity.Animal;
import com.minjeok4go.petplace.pet.entity.Pet;
import com.minjeok4go.petplace.region.entity.Region;
import com.minjeok4go.petplace.region.repository.RegionRepository;
import com.minjeok4go.petplace.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;


@Service
@RequiredArgsConstructor
public class UserGroupService {
    private final RegionRepository regionRepository;

    public String determineGroupKey(User user, List<Pet> pets) {
        // 연령대 계산 (10,20,30대...)
        int age = Period.between(user.getBirthday(), LocalDate.now()).getYears(); // (생일 - 오늘)을 해서 만 나이를 구함
        int ageGroup = (age / 10) * 10; // 10의 자리 단위로 연령대를 만들어줌

        // 성별
        String gender = user.getGender().toString();

        // 지역 (Region ID를 지역명으로 변환하는 별도 메서드 필요)
        String region = getRegionNameById(user.getRegionId());

        // 펫 보유 여부 판단
        boolean hasPet = pets != null && !pets.isEmpty();
        boolean hasDog = hasPet && pets.stream()
                .anyMatch(pet -> pet.getAnimal() == Animal.DOG);

        String petStatus = hasDog ? "HASDOG" : hasPet ? "HASPET" : "NOPET";

        // 최종 그룹 키 조합
        return String.format("%d_%s_%s_%s", ageGroup, gender, region, petStatus);
    }
    // 유저의 지역 ID(regionId)를 기준으로 지역명을 찾아오는 메서드
    private String getRegionNameById(Long regionId) {
        return regionRepository.findById(regionId)
                .map(Region::getName)
                .orElse("UNKNOWN");
    }
}
