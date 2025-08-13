package com.minjeok4go.petplace.user.service;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UserPetSmell {

    private final static Integer BASE = 50;
    private final static Integer STEP = 70;

    public Integer levelForExp(Integer exp) {
        if (exp <= 0) return 1;

        Integer remain = exp;

        int level = 1;

        while (true) {
            Integer need = requiredExpForLevel(level);
            if (remain < need) return level;
            remain -= need;
            level++;
        }
    }

    public Integer requiredExpForLevel(int level) {
        if (level < 1) return BASE;
        return BASE + (level - 1) * STEP;
    }
}
