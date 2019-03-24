package com.himadri.dto;

import com.himadri.model.service.GeneratedDocument;
import com.himadri.model.service.UserSession;
import lombok.Builder;
import lombok.Getter;

import java.text.DecimalFormat;
import java.util.List;

@Getter
@Builder
public class UserPollingInfo {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();

    private final List<ErrorItem> errorItems;
    private final List<GeneratedDocument> generatedDocuments;
    private final int totalPageCount;
    private final int currentPageNumber;
    private final boolean done;
    private final String totalImageSize;
    private final String totalLogoImageSize;

    public static UserPollingInfo createFromUserSession(UserSession userSession) {
        return UserPollingInfo.builder()
            .errorItems(userSession.pollErrorItems())
            .generatedDocuments(userSession.getAllGeneratedDocuments())
            .totalPageCount(userSession.getTotalPageCount())
            .currentPageNumber(userSession.getCurrentPageNumber())
            .done(userSession.isDone())
            .totalImageSize(convertToMBs(userSession.getTotalImageSize().get()))
            .totalLogoImageSize(convertToMBs(userSession.getTotalLogoImageSize().get()))
            .build();
    }

    private static String convertToMBs(long bytes) {
        return String.format("%.1f MB", bytes / 1048576.0);
    }
}
