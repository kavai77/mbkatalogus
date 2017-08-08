package com.himadri.dto;

import com.himadri.model.UserSession;

import java.util.List;

public class UserPollingInfo {
    private final List<UserSession.ErrorItem> errorItems;
    private final List<String> generatedDocuments;
    private final int totalPageCount;
    private final int currentPageNumber;

    private UserPollingInfo(List<UserSession.ErrorItem> errorItems, List<String> generatedDocuments, int totalPageCount, int currentPageNumber) {
        this.errorItems = errorItems;
        this.generatedDocuments = generatedDocuments;
        this.totalPageCount = totalPageCount;
        this.currentPageNumber = currentPageNumber;
    }

    public static UserPollingInfo createFromUserSession(UserSession userSession) {
        return new UserPollingInfo(userSession.pollErrorItems(), userSession.getAllGeneratedDocuments(),
                userSession.getTotalPageCount(), userSession.getCurrentPageNumber());
    }

    public List<UserSession.ErrorItem> getErrorItems() {
        return errorItems;
    }

    public List<String> getGeneratedDocuments() {
        return generatedDocuments;
    }

    public int getTotalPageCount() {
        return totalPageCount;
    }

    public int getCurrentPageNumber() {
        return currentPageNumber;
    }
}
