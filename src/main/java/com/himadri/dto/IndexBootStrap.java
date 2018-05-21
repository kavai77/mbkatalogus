package com.himadri.dto;

import java.util.List;

public class IndexBootStrap {
    private String pageTitle;
    private String lastDocumentTitle;
    private Quality lastQuality;
    private String lastWholeSaleFormat;
    private boolean lastAutoLineBreakAfterMinQty;
    private int lastSkipBoxSpaceOnBeginning;
    private List<String> productGroupsWithoutChapter;

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

    public String getLastWholeSaleFormat() {
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

    public List<String> getProductGroupsWithoutChapter() {
        return productGroupsWithoutChapter;
    }

    public IndexBootStrap setProductGroupsWithoutChapter(List<String> productGroupsWithoutChapter) {
        this.productGroupsWithoutChapter = productGroupsWithoutChapter;
        return this;
    }
}
