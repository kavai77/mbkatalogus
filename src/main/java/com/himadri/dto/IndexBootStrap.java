package com.himadri.dto;

public class IndexBootStrap {
    private String pageTitle;
    private String lastDocumentTitle;
    private Quality lastQuality;
    private String lastWholeSaleFormat;
    private boolean lastAutoLineBreakAfterMinQty;
    private int lastSkipBoxSpaceOnBeginning;

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

    public Quality getLastQuality() {
        return lastQuality;
    }

    public IndexBootStrap setLastQuality(Quality lastQuality) {
        this.lastQuality = lastQuality;
        return this;
    }

    public String isLastWholeSaleFormat() {
        return lastWholeSaleFormat;
    }

    public IndexBootStrap setLastWholeSaleFormat(String lastWholeSaleFormat) {
        this.lastWholeSaleFormat = lastWholeSaleFormat;
        return this;
    }

    public boolean isLastAutoLineBreakAfterMinQty() {
        return lastAutoLineBreakAfterMinQty;
    }

    public IndexBootStrap setLastAutoLineBreakAfterMinQty(boolean lastAutoLineBreakAfterMinQty) {
        this.lastAutoLineBreakAfterMinQty = lastAutoLineBreakAfterMinQty;
        return this;
    }

    public int getLastSkipBoxSpaceOnBeginning() {
        return lastSkipBoxSpaceOnBeginning;
    }

    public IndexBootStrap setLastSkipBoxSpaceOnBeginning(int lastSkipBoxSpaceOnBeginning) {
        this.lastSkipBoxSpaceOnBeginning = lastSkipBoxSpaceOnBeginning;
        return this;
    }
}
