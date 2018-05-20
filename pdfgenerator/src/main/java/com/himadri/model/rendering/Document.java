package com.himadri.model.rendering;

import java.util.List;

public class Document {
    private final List<Page> pages;
    private final TableOfContent tableOfContent;
    private final Index index;

    public Document(List<Page> pages, TableOfContent tableOfContent, Index index) {
        this.pages = pages;
        this.tableOfContent = tableOfContent;
        this.index = index;
    }

    public List<Page> getPages() {
        return pages;
    }

    public TableOfContent getTableOfContent() {
        return tableOfContent;
    }

    public Index getIndex() {
        return index;
    }
}
