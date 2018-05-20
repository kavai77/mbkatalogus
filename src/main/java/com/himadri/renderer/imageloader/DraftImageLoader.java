package com.himadri.renderer.imageloader;

import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.Item;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class DraftImageLoader implements ImageLoader {
    @Override
    public String getImageName(Item item) {
        return null;
    }

    @Override
    public PDImageXObject loadImage(Box box, PDDocument document) {
        return null;
    }

    @Override
    public PDImageXObject loadLogoImage(Box box, PDDocument document) {
        return null;
    }

    @Override
    public MemoryUsageSetting getMemoryUsageSettings() {
        return MemoryUsageSetting.setupMainMemoryOnly();
    }
}
