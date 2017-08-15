package com.himadri.model.service;

import com.google.common.base.Objects;
import org.apache.pdfbox.pdmodel.PDDocument;

public class DocumentFont {
    private final PDDocument pdDocument;
    private final String fontName;

    public DocumentFont(PDDocument pdDocument, String fontName) {
        this.pdDocument = pdDocument;
        this.fontName = fontName;
    }

    public PDDocument getPdDocument() {
        return pdDocument;
    }

    public String getFontName() {
        return fontName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentFont that = (DocumentFont) o;
        return Objects.equal(pdDocument, that.pdDocument) &&
                Objects.equal(fontName, that.fontName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pdDocument, fontName);
    }
}
