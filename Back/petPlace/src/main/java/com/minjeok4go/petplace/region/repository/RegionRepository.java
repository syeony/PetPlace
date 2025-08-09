package com.minjeok4go.petplace.region.repository;

import com.minjeok4go.petplace.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {
}
