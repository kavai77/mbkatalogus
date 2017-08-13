package com.himadri.renderer;

import com.google.common.cache.Cache;
import com.himadri.dto.ErrorItem;
import com.himadri.dto.UserRequest;
import com.himadri.model.rendering.Document;
import com.himadri.model.rendering.Page;
import com.himadri.model.service.UserSession;
import de.rototor.pdfbox.graphics2d.IPdfBoxGraphics2DColorMapper;
import de.rototor.pdfbox.graphics2d.IPdfBoxGraphics2DFontApplier;
import de.rototor.pdfbox.graphics2d.IPdfBoxGraphics2DImageEncoder;
import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFontFactory;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static org.apache.commons.lang3.StringUtils.*;

@Component
public class DocumentRenderer {
    @Autowired
    private TableOfContentRenderer tableOfContentRenderer;

    @Autowired
    private PageRenderer pageRenderer;

    @Autowired
    private Cache<String, UserSession> userSessionCache;

    @Value("${renderingLocation}")
    private String renderingLocation;

    @Value("${pagesPerDocumentInQualityMode}")
    private int pagesPerDocumentInQualityMode;

    @PostConstruct
    public void init() {
        final File renderingLocationFile = new File(renderingLocation);
        if (!renderingLocationFile.exists()) {
            final boolean successful = renderingLocationFile.mkdirs();
            if (!successful) {
                throw new RuntimeException("Could not create the path for rendering location: " + renderingLocation);
            }
        }
    }

    public void renderDocument(Document document, UserRequest userRequest) throws IOException {
        int previousDocumentStartPage = 1;
        int pagesPerDocument = userRequest.isDraftMode() ? Integer.MAX_VALUE : pagesPerDocumentInQualityMode;
        UserSession userSession = userSessionCache.getIfPresent(userRequest.getRequestId());
        PDDocument doc = new PDDocument();
        renderObject(doc, g2 -> tableOfContentRenderer.renderTableOfContent(g2, document.getTableOfContent()));
        for (int i = 0; i < document.getPages().size(); i++) {
            if (i > 0 && i % pagesPerDocument == 0) {
                closeDocument(doc, userRequest, userSession, previousDocumentStartPage);
                previousDocumentStartPage = i + 1;
                doc = new PDDocument();
            }
            final Page page = document.getPages().get(i);
            renderObject(doc, g2 -> pageRenderer.drawPage(g2, page, userRequest));
            userSession.incrementCurrentPageNumber();
            if (userSession.isCancelled()) {
                break;
            }
        }

        closeDocument(doc, userRequest, userSession, previousDocumentStartPage);
        userSession.addErrorItem(ErrorItem.Severity.INFO, ErrorItem.ErrorCategory.INFO,
                userSession.isCancelled() ? "A dokumentum készítés megszakítva" : "A dokumentum készítés kész.");
    }

    private void renderObject(PDDocument doc, Consumer<Graphics2D> consumer) throws IOException {
        PDPage pdPage = new PDPage(PDRectangle.A4);
        PdfBoxGraphics2D g2 = new PdfBoxGraphics2D(doc, PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());
        doc.addPage(pdPage);
        setCommonGraphics(g2);
        consumer.accept(g2);
        g2.dispose();
        PDPageContentStream contentStream = new PDPageContentStream(doc, pdPage);
        contentStream.drawForm(g2.getXFormObject());
        contentStream.close();
    }

    private void closeDocument(PDDocument doc, UserRequest userRequest, UserSession userSession, int previousDocumentStartPage) throws IOException {
        String docPrefix = deleteWhitespace(stripAccents(lowerCase(userRequest.getCatalogueTitle())));
        final File pdfFile = File.createTempFile(String.format("%s-%d-%d-", docPrefix, previousDocumentStartPage,
                userSession.getCurrentPageNumber()),".pdf", new File(renderingLocation));
        doc.save(pdfFile);
        doc.close();
        userSession.addGeneratedDocument(pdfFile.getName(), String.format("%d-%d", previousDocumentStartPage,
                userSession.getCurrentPageNumber()));
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
        g2.setFontApplier(new IPdfBoxGraphics2DFontApplier() {
            @Override
            public void applyFont(PDDocument document, PDPageContentStream contentStream, Font font) throws IOException {
                contentStream.setFont(PDFontFactory.createDefaultFont(), 1);
            }
        });
    }
}
