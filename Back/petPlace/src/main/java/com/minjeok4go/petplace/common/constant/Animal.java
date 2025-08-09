package com.minjeok4go.petplace.common.constant;

import lombok.Getter;

@Getter
public enum Animal {
    DOG("강아지"),
    CAT("고양이"),
    RABBIT("토끼"),
    HAMSTER("햄스터"),
    GUINEA_PIG("기니피그"),
    HEDGEHOG("고슴도치"),
    FERRET("앵무새"),
    BIRD("새"),
    TURTLE("거북이"),
    FISH("물고기"),
    REPTILE("파충류"),
    AMPHIBIAN("양서류"),
    OTHER("기타");

    private final String displayName;
    Animal(String displayName){
        this.displayName = displayName;
    }
}
