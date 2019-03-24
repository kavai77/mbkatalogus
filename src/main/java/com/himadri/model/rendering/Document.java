package com.himadri.model.rendering;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Document {
    private final List<Page> pages;
    private final TableOfContent tableOfContent;
    private final Index index;
}
