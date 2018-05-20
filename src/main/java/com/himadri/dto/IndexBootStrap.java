package com.himadri.dto;

public class IndexBootStrap {
    private String pageTitle;
    private String lastDocumentTitle;

    public String getPageTitle() {
        return pageTitle;
    }

    public IndexBootStrap setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
        return this;
    }

    public String getLastDocumentTitle() {
        return lastDocumentTitle;
    }

    public IndexBootStrap setLastDocumentTitle(String lastDocumentTitle) {
        this.lastDocumentTitle = lastDocumentTitle;
        return this;
    }
}
