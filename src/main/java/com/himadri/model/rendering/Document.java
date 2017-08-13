package com.himadri.model.rendering;

import java.util.List;

public class Document {
    private final List<Page> pages;
    private final TableOfContent tableOfContent;

    public Document(List<Page> pages, TableOfContent tableOfContent) {
        this.pages = pages;
        this.tableOfContent = tableOfContent;
    }

    public List<Page> getPages() {
        return pages;
    }

    public TableOfContent getTableOfContent() {
        return tableOfContent;
    }
}
