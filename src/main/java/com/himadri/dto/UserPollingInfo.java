package com.himadri.dto;

import com.himadri.model.service.UserSession;

import java.util.List;

public class UserPollingInfo {
    private final List<ErrorItem> errorItems;
    private final List<String> generatedDocuments;
    private final int totalPageCount;
    private final int currentPageNumber;
    private final boolean done;

    private UserPollingInfo(List<ErrorItem> errorItems, List<String> generatedDocuments, int totalPageCount, int currentPageNumber, boolean done) {
        this.errorItems = errorItems;
        this.generatedDocuments = generatedDocuments;
        this.totalPageCount = totalPageCount;
        this.currentPageNumber = currentPageNumber;
        this.done = done;
    }

    public static UserPollingInfo createFromUserSession(UserSession userSession) {
        return new UserPollingInfo(userSession.pollErrorItems(), userSession.getAllGeneratedDocuments(),
                userSession.getTotalPageCount(), userSession.getCurrentPageNumber(), userSession.isDone());
    }

    public List<ErrorItem> getErrorItems() {
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

    public boolean isDone() {
        return done;
    }

    @Override
    public String toString() {
        return "UserPollingInfo{" +
                "errorItems=" + errorItems +
                ", generatedDocuments=" + generatedDocuments +
                ", totalPageCount=" + totalPageCount +
                ", currentPageNumber=" + currentPageNumber +
                ", done=" + done +
                '}';
    }
}
