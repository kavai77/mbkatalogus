package com.himadri.engine;

import com.google.common.cache.Cache;
import com.himadri.dto.ErrorItem;
import com.himadri.dto.UserRequest;
import com.himadri.graphics.pdfbox.PDFontService;
import com.himadri.graphics.pdfbox.PdfBoxPageGraphics;
import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.Index;
import com.himadri.model.rendering.Page;
import com.himadri.model.service.UserSession;
import com.himadri.renderer.IndecesRenderer;
import com.himadri.renderer.IndexPageRenderer;
import com.himadri.renderer.Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.himadri.renderer.Fonts.INDEX_CONTENT_FONT;
import static org.apache.commons.lang3.StringUtils.stripToEmpty;

@Component
public class IndexEngine {
    @Autowired
    private PDFontService pdFontService;

    @Autowired
    private IndexPageRenderer indexPageRenderer;

    @Autowired
    private Cache<String, UserSession> userSessionCache;

    @Autowired
    private Util util;

    public Index createIndex(List<Page> pages, UserRequest userRequest) {
        Index index = new Index();
        SortedSet<Index.Record> productNameSet = new TreeSet<>();
        PdfBoxPageGraphics g2 = PdfBoxPageGraphics.createForStringWidthCalculation(pdFontService);
        g2.setFont(INDEX_CONTENT_FONT);
        for (Page page: pages) {
            for (Box box: page.getBoxes()) {
                for (Box.Article article: box.getArticles()) {
                    String title = getIndexTitle(g2, article, userRequest);
                    if (StringUtils.isNotBlank(title)) {
                        productNameSet.add(new Index.Record(title, page.getPageNumber()));
                    }
                    index.getProductNumberIndex().add(new Index.Record(article.getNumber(), page.getPageNumber()));
                }
            }
        }
        index.getProductNameIndex().addAll(productNameSet);
        Collections.sort(index.getProductNumberIndex());
        g2.closeStream();
        return index;
    }

    private String getIndexTitle(PdfBoxPageGraphics g2, Box.Article article, UserRequest userRequest) {
        final String boxIndexName = stripToEmpty(article.getIndexName());
        final int indexStringWidth = g2.getStringWidth(boxIndexName);
        if (indexStringWidth > indexPageRenderer.calculateKeySplitWidth(IndecesRenderer.PRODUCT_NAME_BOX_COLUMN_NB)) {
            final UserSession userSession = userSessionCache.getIfPresent(userRequest.getRequestId());
            userSession.addErrorItem(ErrorItem.Severity.ERROR, ErrorItem.ErrorCategory.FORMATTING,
                    "A tárgymutató szövege hosszabb, mint amennyi kifér a boxba: " + boxIndexName);
        }
        return boxIndexName;
    }
}
