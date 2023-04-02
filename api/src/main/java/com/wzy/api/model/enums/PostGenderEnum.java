package com.wzy.api.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帖子性别枚举
 *
 * 
 */
public enum PostGenderEnum {

    MALE("男", 0),
    FEMALE("女", 1);

    private final String text;

    private final int value;

    PostGenderEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
