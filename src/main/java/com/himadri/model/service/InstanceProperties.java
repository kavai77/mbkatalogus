package com.himadri.model.service;

import com.himadri.dto.Quality;

import java.util.List;

public class InstanceProperties {
    private String lastCatalogueName;
    private Quality lastQuality;
    private Boolean lastWholeSaleFormat;
    private boolean lastAutoLineBreakAfterMinQty;
    private int lastSkipBoxSpaceOnBeginning;
    private List<String> productsWithoutChapter;

    public String getLastCatalogueName() {
        return lastCatalogueName;
    }

    public void setLastCatalogueName(String lastCatalogueName) {
        this.lastCatalogueName = lastCatalogueName;
    }

    public List<String> getProductsWithoutChapter() {
        return productsWithoutChapter;
    }

    public void setProductsWithoutChapter(List<String> productsWithoutChapter) {
        this.productsWithoutChapter = productsWithoutChapter;
    }

    public Quality getLastQuality() {
        return lastQuality;
    }

    public void setLastQuality(Quality lastQuality) {
        this.lastQuality = lastQuality;
    }

    public Boolean isLastWholeSaleFormat() {
        return lastWholeSaleFormat;
    }

    public void setLastWholeSaleFormat(boolean lastWholeSaleFormat) {
        this.lastWholeSaleFormat = lastWholeSaleFormat;
    }

    public boolean isLastAutoLineBreakAfterMinQty() {
        return lastAutoLineBreakAfterMinQty;
    }

    public void setLastAutoLineBreakAfterMinQty(boolean lastAutoLineBreakAfterMinQty) {
        this.lastAutoLineBreakAfterMinQty = lastAutoLineBreakAfterMinQty;
    }

    public int getLastSkipBoxSpaceOnBeginning() {
        return lastSkipBoxSpaceOnBeginning;
    }

    public void setLastSkipBoxSpaceOnBeginning(int lastSkipBoxSpaceOnBeginning) {
        this.lastSkipBoxSpaceOnBeginning = lastSkipBoxSpaceOnBeginning;
    }
}
