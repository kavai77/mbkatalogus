package com.himadri.model.rendering;

import java.util.List;

public class Page {
    public enum Orientation {LEFT, RIGHT}

    private final String headLine;
    private final String category;
    private final int pageNumber;
    private final Orientation orientation;
    private final List<Box> boxes;

    public Page(String headLine, String category, int pageNumber, Orientation orientation, List<Box> boxes) {
        this.headLine = headLine;
        this.category = category;
        this.pageNumber = pageNumber;
        this.orientation = orientation;
        this.boxes = boxes;
    }

    public String getHeadLine() {
        return headLine;
    }

    public String getCategory() {
        return category;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public List<Box> getBoxes() {
        return boxes;
    }
}
