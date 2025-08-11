package com.minjeok4go.petplace.user.service;

import com.minjeok4go.petplace.common.constant.Animal;
import com.minjeok4go.petplace.pet.entity.Pet;
import com.minjeok4go.petplace.region.entity.Region;
import com.minjeok4go.petplace.region.repository.RegionRepository;
import com.minjeok4go.petplace.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class UserGroupService {

    private final RegionRepository regionRepository;

    // 호환용(기존 호출 그대로 사용)
    public String determineGroupKey(User user, List<Pet> pets) {
        return determineGroupKey(user, pets, this::getRegionNameById);
    }

    // 성능용(배치에서 region 캐시 리졸버 주입)
    public String determineGroupKey(User user, List<Pet> pets, Function<Long, String> regionResolver) {
        int age = Period.between(user.getBirthday(), LocalDate.now()).getYears();
        int ageGroup = (age / 10) * 10;
        String gender = user.getGender().toString();

        String region = (regionResolver != null)
                ? regionResolver.apply(user.getRegionId())
                : getRegionNameById(user.getRegionId());

        boolean hasPet = pets != null && !pets.isEmpty();
        boolean hasDog = hasPet && pets.stream().anyMatch(p -> p.getAnimal() == Animal.DOG);
        String petStatus = hasDog ? "HASDOG" : hasPet ? "HASPET" : "NOPET";

        return String.format("%d_%s_%s_%s", ageGroup, gender, region, petStatus);
    }

    public String getRegionNameById(Long regionId) {
        return regionRepository.findById(regionId)
                .map(Region::getName)
                .orElse("UNKNOWN");
    }
}
