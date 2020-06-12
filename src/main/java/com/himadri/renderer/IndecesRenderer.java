package com.himadri.renderer;

import com.google.common.cache.Cache;
import com.himadri.I18NService;
import com.himadri.dto.UserRequest;
import com.himadri.graphics.pdfbox.PDColorTranslator;
import com.himadri.graphics.pdfbox.PDFontService;
import com.himadri.graphics.pdfbox.PdfBoxPageGraphics;
import com.himadri.model.rendering.Index;
import com.himadri.model.service.UserSession;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IndecesRenderer {
    public static final int MAX_BOX_ROW_NB = 53;
    public static final int PRODUCT_NB_BOX_COLUMN_NB = 6;
    public static final int PRODUCT_NAME_BOX_COLUMN_NB = 3;

    @Autowired
    private IndexPageRenderer indexPageRenderer;

    @Autowired
    private PDFontService pdFontService;

    @Autowired
    private PDColorTranslator pdColorTranslator;

    @Autowired
    private Cache<String, UserSession> userSessionCache;

    @Autowired
    private I18NService i18NService;

    public void renderProductNumberIndex(PDDocument doc, Index index, UserRequest userRequest) {
        UserSession userSession = userSessionCache.getIfPresent(userRequest.getRequestId());
        final List<Index.Record> productNumberIndex = index.getProductNumberIndex();
        int startIndex = 0;
        while (startIndex < productNumberIndex.size()) {
            final int pageBoxes = Math.min(MAX_BOX_ROW_NB * PRODUCT_NB_BOX_COLUMN_NB, productNumberIndex.size() - startIndex);
            final int rows = (int) Math.ceil((double) pageBoxes / PRODUCT_NB_BOX_COLUMN_NB);
            final PDRectangle pageSize = Util.getStandardPageSize(userRequest.getQuality().isDrawCuttingEdges());
            PdfBoxPageGraphics g2 = new PdfBoxPageGraphics(doc, pageSize, pdFontService, pdColorTranslator, userSession);
            indexPageRenderer.renderIndex(g2,
                    productNumberIndex,
                    startIndex,
                    i18NService.getMessage("indexProductNb"),
                    i18NService.getMessage("indexPageNb"),
                    userRequest,
                    i18NService.getMessage("indexTitle"),
                    rows,
                    PRODUCT_NB_BOX_COLUMN_NB);
            g2.closeStream();
            startIndex += pageBoxes;
            userSession.incrementCurrentPageNumber();
        }
    }

    public void renderProductNameIndex(PDDocument doc, Index index, UserRequest userRequest) {
        UserSession userSession = userSessionCache.getIfPresent(userRequest.getRequestId());
        final List<Index.Record> productNameIndex = index.getProductNameIndex();
        int startIndex = 0;
        while (startIndex < productNameIndex.size()) {
            final int pageBoxes = Math.min(MAX_BOX_ROW_NB * PRODUCT_NAME_BOX_COLUMN_NB, productNameIndex.size() - startIndex);
            final int rows = (int) Math.ceil((double) pageBoxes / PRODUCT_NAME_BOX_COLUMN_NB);
            final PDRectangle pageSize = Util.getStandardPageSize(userRequest.getQuality().isDrawCuttingEdges());
            PdfBoxPageGraphics g2 = new PdfBoxPageGraphics(doc, pageSize, pdFontService, pdColorTranslator, userSession);
            indexPageRenderer.renderIndex(g2,
                    productNameIndex,
                    startIndex,
                    null,
                    null,
                    userRequest,
                    i18NService.getMessage("subjectIndexTitle"),
                    rows,
                    PRODUCT_NAME_BOX_COLUMN_NB);
            g2.closeStream();
            startIndex += pageBoxes;
            userSession.incrementCurrentPageNumber();
        }
    }
}
