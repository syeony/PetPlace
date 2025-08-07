package com.minjeok4go.petplace.feed.service;

import com.minjeok4go.petplace.feed.dto.TagResponse;
import com.minjeok4go.petplace.feed.entity.Tag;
import com.minjeok4go.petplace.feed.repository.TagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;


    public List<TagResponse> getTagAll() {

        List<Tag> tags = tagRepository.findAllByOrderByIdAsc();

        return tags.stream()
                .map(t -> new TagResponse(t.getId(), t.getName())).toList();
    }

    public TagResponse getTagById(Long id) {

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));

        return new TagResponse(tag.getId(), tag.getName());
    }
}
