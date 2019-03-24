package com.himadri.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class IndexBootStrap {
    private String pageTitle;
    private String lastDocumentTitle;
    private Quality lastQuality;
    private String lastWholeSaleFormat;
    private boolean lastAutoLineBreakAfterMinQty;
    private boolean lastLastWideHeaderImage;
    private boolean lastLastWideFooterImage;
    private List<String> productGroupsWithoutChapter;
    private String buildTimestamp;
}
