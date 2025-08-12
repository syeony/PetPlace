package com.minjeok4go.petplace.pet.repository;

import com.minjeok4go.petplace.pet.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByUserId(Long userId);

    Optional<Pet> findByIdAndUserId(Long id, Long userId);

    List<Pet> findByUserIdIn(Collection<Long> userIds); // collection은 다양한 자료구조(List,Set)등을 사용할 수 있게 만들어줌

}
