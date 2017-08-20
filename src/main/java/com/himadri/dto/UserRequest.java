package com.himadri.dto;

import java.io.InputStream;

public class UserRequest {
    private final String requestId;
    private final InputStream csvInputStream;
    private final String catalogueTitle;
    private final boolean draftMode;
    private final boolean wholeSaleFormat;

    public UserRequest(String requestId, InputStream csvInputStream, String catalogueTitle, boolean draftMode,
                       boolean wholeSaleFormat) {
        this.requestId = requestId;
        this.csvInputStream = csvInputStream;
        this.catalogueTitle = catalogueTitle;
        this.draftMode = draftMode;
        this.wholeSaleFormat = wholeSaleFormat;
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

    public boolean isDraftMode() {
        return draftMode;
    }

    public boolean isWholeSaleFormat() {
        return wholeSaleFormat;
    }
}
