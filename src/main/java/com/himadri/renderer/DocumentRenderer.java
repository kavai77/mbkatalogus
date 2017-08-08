package com.himadri.renderer;

import com.google.common.cache.Cache;
import com.himadri.Settings;
import com.himadri.model.Page;
import com.himadri.model.UserRequest;
import com.himadri.model.UserSession;
import de.rototor.pdfbox.graphics2d.IPdfBoxGraphics2DColorMapper;
import de.rototor.pdfbox.graphics2d.IPdfBoxGraphics2DImageEncoder;
import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.lang.StringUtils.deleteWhitespace;
import static org.apache.commons.lang.StringUtils.lowerCase;

@Component
public class DocumentRenderer {
    @Autowired
    private PageRenderer pageRenderer;

    @Autowired
    private Cache<String, UserSession> userSessionCache;

    public void renderDocument(List<Page> pages, UserRequest userRequest) throws IOException {
        int pagePerDocument = Integer.MAX_VALUE;
        int previousDocumentStartPage = 1;
        UserSession userSession = userSessionCache.getIfPresent(userRequest.getRequestId());
        PDDocument doc = new PDDocument();
        for (int i = 0; i < pages.size(); i++) {
            if (i > 0 && i % pagePerDocument == 0) {
                closeDocument(doc, userRequest, userSession, previousDocumentStartPage);
                previousDocumentStartPage = i + 1;
                doc = new PDDocument();
            }
            Page page = pages.get(i);
            PDPage pdPage = new PDPage(PDRectangle.A4);
            PdfBoxGraphics2D g2 = new PdfBoxGraphics2D(doc, PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());
            doc.addPage(pdPage);
            setCommonGraphics(g2);
            pageRenderer.drawPage(g2, page, userRequest);
            g2.dispose();
            PDPageContentStream contentStream = new PDPageContentStream(doc, pdPage);
            contentStream.drawForm(g2.getXFormObject());
            contentStream.close();
            userSession.incrementCurrentPageNumber();
        }
        closeDocument(doc, userRequest, userSession, previousDocumentStartPage);
    }

    private void closeDocument(PDDocument doc, UserRequest userRequest, UserSession userSession, int previousDocumentStartPage) throws IOException {
        String docPrefix = deleteWhitespace(lowerCase(userRequest.getCatalogueTitle(), Locale.ENGLISH));
        final String fileName = String.format("%s-%d-%d.pdf", docPrefix, previousDocumentStartPage,
                userSession.getCurrentPageNumber() + 1);
        userSession.addGeneratedDocument(fileName);
        doc.save(new File(Settings.RENDERING_LOCATION, fileName));
        doc.close();
    }

    private static void setCommonGraphics(PdfBoxGraphics2D g2) {
        g2.setColorMapper(new IPdfBoxGraphics2DColorMapper() {
            @Override
            public PDColor mapColor(PDPageContentStream pdPageContentStream, Color color) {
                if (color == null)
                    return new PDColor(new float[] { 0, 0, 0, 1f }, PDDeviceCMYK.INSTANCE);

                float[] c = color.getRGBColorComponents(null);
                float k = 1 - Math.max(c[0], Math.max(c[1], c[2]));
                if (k == 1) {
                    return new PDColor(new float[]{0, 0, 0, 1}, PDDeviceCMYK.INSTANCE);
                } else {
                    return new PDColor(new float[]{
                            (1 - c[0] - k) / (1 - k),
                            (1 - c[1] - k) / (1 - k),
                            (1 - c[2] - k) / (1 - k),
                            k}, PDDeviceCMYK.INSTANCE);
                }
            }
        });
        g2.setImageEncoder(new IPdfBoxGraphics2DImageEncoder() {
            @Override
            public PDImageXObject encodeImage(PDDocument document, PDPageContentStream contentStream, Image image) {
                try {
                    return LosslessFactory.createFromImage(document, (BufferedImage) image);
                } catch (IOException e) {
                    throw new RuntimeException("Could not encode Image", e);
                }
            }
        });
    }
}
