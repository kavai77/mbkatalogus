package com.himadri.model;

public class UserRequest {
    private final String catalogueName;

    public UserRequest(String catalogueName) {
        this.catalogueName = catalogueName;
    }

    public String getCatalogueName() {
        return catalogueName;
    }
}
