package com.himadri.dto;

public enum Quality {
    DRAFT(false, false),
    WEB(false, false),
    PRESS(true, true);

    private final boolean drawCuttingEdges;
    private final boolean splitIntoMultiplePDFs;

    Quality(boolean drawCuttingEdges, boolean splitIntoMultiplePDFs) {
        this.drawCuttingEdges = drawCuttingEdges;
        this.splitIntoMultiplePDFs = splitIntoMultiplePDFs;
    }

    public boolean isDrawCuttingEdges() {
        return drawCuttingEdges;
    }

    public boolean isSplitIntoMultiplePDFs() {
        return splitIntoMultiplePDFs;
    }
}
