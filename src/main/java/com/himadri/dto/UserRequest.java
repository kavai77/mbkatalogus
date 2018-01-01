package com.himadri.dto;

import java.io.InputStream;

public class UserRequest {
    private final String requestId;
    private final InputStream csvInputStream;
    private final String catalogueTitle;
    private final Quality quality;
    private final boolean wholeSaleFormat;
    private final boolean autoLineBreakAfterMinQty;
    private final int skipBoxSpaceOnBeginning;

    public UserRequest(String requestId, InputStream csvInputStream, String catalogueTitle, Quality quality,
                       boolean wholeSaleFormat, boolean autoLineBreakAfterMinQty,
                       int skipBoxSpaceOnBeginning) {
        this.requestId = requestId;
        this.csvInputStream = csvInputStream;
        this.catalogueTitle = catalogueTitle;
        this.quality = quality;
        this.wholeSaleFormat = wholeSaleFormat;
        this.autoLineBreakAfterMinQty = autoLineBreakAfterMinQty;
        this.skipBoxSpaceOnBeginning = skipBoxSpaceOnBeginning;
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

    public Quality getQuality() {
        return quality;
    }

    public boolean isWholeSaleFormat() {
        return wholeSaleFormat;
    }

    public boolean isAutoLineBreakAfterMinQty() {
        return autoLineBreakAfterMinQty;
    }

    public int getSkipBoxSpaceOnBeginning() {
        return skipBoxSpaceOnBeginning;
    }
}
