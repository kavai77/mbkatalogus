package com.himadri.dto;

import com.himadri.model.service.GeneratedDocument;
import com.himadri.model.service.UserSession;

import java.text.DecimalFormat;
import java.util.List;

public class UserPollingInfo {
    private final List<ErrorItem> errorItems;
    private final List<GeneratedDocument> generatedDocuments;
    private final int totalPageCount;
    private final int currentPageNumber;
    private final boolean done;
    private final String totalImageSize;
    private final String totalLogoImageSize;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();

    private UserPollingInfo(List<ErrorItem> errorItems, List<GeneratedDocument> generatedDocuments, int totalPageCount,
                            int currentPageNumber, boolean done, String totalImageSize, String totalLogoImageSize) {
        this.errorItems = errorItems;
        this.generatedDocuments = generatedDocuments;
        this.totalPageCount = totalPageCount;
        this.currentPageNumber = currentPageNumber;
        this.done = done;
        this.totalImageSize = totalImageSize;
        this.totalLogoImageSize = totalLogoImageSize;
    }

    public static UserPollingInfo createFromUserSession(UserSession userSession) {
        return new UserPollingInfo(userSession.pollErrorItems(), userSession.getAllGeneratedDocuments(),
                userSession.getTotalPageCount(), userSession.getCurrentPageNumber(), userSession.isDone(),
                convertToMBs(userSession.getTotalImageSize().get()),
                convertToMBs(userSession.getTotalLogoImageSize().get()));
    }

    public List<ErrorItem> getErrorItems() {
        return errorItems;
    }

    public List<GeneratedDocument> getGeneratedDocuments() {
        return generatedDocuments;
    }

    public int getTotalPageCount() {
        return totalPageCount;
    }

    public int getCurrentPageNumber() {
        return currentPageNumber;
    }

    public String getTotalImageSize() {
        return totalImageSize;
    }

    public String getTotalLogoImageSize() {
        return totalLogoImageSize;
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

    private static String convertToMBs(long bytes) {
        return String.format("%.1f MB", bytes / 1048576.0);
    }
}
