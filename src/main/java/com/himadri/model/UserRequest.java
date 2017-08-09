package com.himadri.model;

import java.io.InputStream;

public class UserRequest {
    private final String requestId;
    private final InputStream csvInputStream;
    private final String catalogueTitle;
    private final boolean enableImages;
    private final int pagesPerDocument;

    public UserRequest(String requestId, InputStream csvInputStream, String catalogueTitle, boolean enableImages, int pagesPerDocument) {
        this.requestId = requestId;
        this.csvInputStream = csvInputStream;
        this.catalogueTitle = catalogueTitle;
        this.enableImages = enableImages;
        this.pagesPerDocument = pagesPerDocument;
    }

    public String getRequestId() {
        return requestId;
    }

    public InputStream getCsvInputStream() {
        return csvInputStream;
    }

    public String getCatalogueTitle() {
        return catalogueTitle;
    }

    public boolean isEnableImages() {
        return enableImages;
    }

    public int getPagesPerDocument() {
        return pagesPerDocument;
    }
}
