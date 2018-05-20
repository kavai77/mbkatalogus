package com.himadri.renderer.imageloader;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.util.Objects;

class LogoImageKey {
    private final PDDocument pdDocument;
    private final File logoImage;

    public LogoImageKey(PDDocument pdDocument, File logoImage) {
        this.pdDocument = pdDocument;
        this.logoImage = logoImage;
    }

    public PDDocument getPdDocument() {
        return pdDocument;
    }

    public File getLogoImage() {
        return logoImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogoImageKey that = (LogoImageKey) o;
        return Objects.equals(pdDocument, that.pdDocument) &&
                Objects.equals(logoImage, that.logoImage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pdDocument, logoImage);
    }
}
