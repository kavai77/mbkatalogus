package com.himadri.renderer.imageloader;

import com.himadri.exception.ImageNotFoundException;
import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.CsvItem;
import com.himadri.model.service.UserSession;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;

public interface ImageLoader {
    String getImageName(CsvItem item);
    PDImageXObject loadImage(Box box, PDDocument document, UserSession userSession) throws IOException, ImageNotFoundException;
    PDImageXObject loadLogoImage(Box box, PDDocument document, UserSession userSession) throws IOException, ImageNotFoundException;
    PDImageXObject loadResourceImage(String resource, PDDocument document) throws IOException;
    MemoryUsageSetting getMemoryUsageSettings();
}
