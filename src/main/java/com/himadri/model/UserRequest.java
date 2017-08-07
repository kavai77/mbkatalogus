package com.himadri.model;

import java.io.InputStream;

public class UserRequest {
    private final String requestId;
    private final InputStream csvInputStream;
    private final String catalogueTitle;
    private final boolean enableImages;

    public UserRequest(String requestId, InputStream csvInputStream, String catalogueTitle, boolean enableImages) {
        this.requestId = requestId;
        this.csvInputStream = csvInputStream;
        this.catalogueTitle = catalogueTitle;
        this.enableImages = enableImages;
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
}
