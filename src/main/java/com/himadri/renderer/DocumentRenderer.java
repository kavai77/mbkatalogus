package com.himadri.renderer;

import com.google.common.cache.Cache;
import com.himadri.dto.ErrorItem;
import com.himadri.dto.UserRequest;
import com.himadri.graphics.pdfbox.PDColorTranslator;
import com.himadri.graphics.pdfbox.PDFontService;
import com.himadri.graphics.pdfbox.PdfBoxGraphics;
import com.himadri.model.rendering.Document;
import com.himadri.model.rendering.Page;
import com.himadri.model.service.UserSession;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static org.apache.commons.lang3.StringUtils.*;

@Component
public class DocumentRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentRenderer.class);
    @Autowired
    private TableOfContentRenderer tableOfContentRenderer;

    @Autowired
    private PageRenderer pageRenderer;

    @Autowired
    private Cache<String, UserSession> userSessionCache;

    @Autowired
    private PDFontService pdFontService;

    @Autowired
    private PDColorTranslator pdColorTranslator;

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
        PDDocument doc = new PDDocument(MemoryUsageSetting.setupMixed(100 * 2^20));
        renderPDFPage(doc, userSession, g2 -> tableOfContentRenderer.renderTableOfContent(g2, document.getTableOfContent()));
        userSession.incrementCurrentPageNumber();
        for (int i = 0; i < document.getPages().size(); i++) {
            if (i > 0 && i % pagesPerDocument == 0) {
                closeDocument(doc, userRequest, userSession, previousDocumentStartPage);
                previousDocumentStartPage = i + 1;
                doc = new PDDocument();
            }
            final Page page = document.getPages().get(i);
            LOGGER.debug("Rendering page:" + page.getPageNumber());
            renderPDFPage(doc, userSession, g -> pageRenderer.drawPage(g, page, userRequest));
            userSession.incrementCurrentPageNumber();
            if (userSession.isCancelled()) {
                break;
            }
        }

        closeDocument(doc, userRequest, userSession, previousDocumentStartPage);
        userSession.addErrorItem(ErrorItem.Severity.INFO, ErrorItem.ErrorCategory.INFO,
                userSession.isCancelled() ? "A dokumentum készítés megszakítva" : "A dokumentum készítés kész.");
    }

    private void renderPDFPage(PDDocument doc, UserSession userRequest, Consumer<PdfBoxGraphics> consumer) throws IOException {
        PDPage pdPage = new PDPage(PDRectangle.A4);
        doc.addPage(pdPage);
        PdfBoxGraphics graphics = new PdfBoxGraphics(doc, pdPage, pdFontService, pdColorTranslator, userRequest);
        consumer.accept(graphics);
        graphics.closeStream();
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
}
