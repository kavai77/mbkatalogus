package com.himadri.model.service;

import java.util.List;

public class InstanceProperties {
    private String lastCatalogueName;
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
}
