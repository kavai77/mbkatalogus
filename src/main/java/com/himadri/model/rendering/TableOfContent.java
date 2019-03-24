package com.himadri.model.rendering;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
public class TableOfContent {
    private final Map<String, Integer> tableOfContent = new LinkedHashMap<>();
}
