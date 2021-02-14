package com.himadri.model.rendering;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
public class TableOfContent {
    private final Map<String, TableOfContentItem> tableOfContent = new LinkedHashMap<>();
    @Data
    public static class TableOfContentItem {
        private final int pageNumber;
        private final Color color;
    }
}
