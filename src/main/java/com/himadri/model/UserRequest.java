package com.himadri.model;

public class UserRequest {
    private String localCsvFile;
    private String catalogueName;

    public String getCatalogueName() {
        return catalogueName;
    }

    public void setCatalogueName(String catalogueName) {
        this.catalogueName = catalogueName;
    }

    public String getLocalCsvFile() {
        return localCsvFile;
    }

    public void setLocalCsvFile(String localCsvFile) {
        this.localCsvFile = localCsvFile;
    }
}
