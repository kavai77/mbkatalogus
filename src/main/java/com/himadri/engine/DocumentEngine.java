package com.himadri.engine;

import com.google.common.cache.Cache;
import com.himadri.dto.UserRequest;
import com.himadri.engine.ItemCategorizerEngine.CsvProductGroup;
import com.himadri.exception.ValidationException;
import com.himadri.model.rendering.*;
import com.himadri.model.service.UserSession;
import com.himadri.renderer.IndecesRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentEngine {
    @Autowired
    private ItemCategorizerEngine itemCategorizerEngine;

    @Autowired
    private PageCollectorEngine pageCollectorEngine;

    @Autowired
    private TableOfContentEngine tableOfContentEngine;

    @Autowired
    private IndexEngine indexEngine;

    @Autowired
    private Cache<String, UserSession> userSessionCache;

    public Document createDocumentFromItems(List<CsvItem> items, UserRequest userRequest, UserSession userSession) throws ValidationException {
        final List<CsvProductGroup> productGroups = itemCategorizerEngine.groupCsvItems(items, userSession);
        final List<Page> pages = pageCollectorEngine.createPages(productGroups, userRequest.getCatalogueTitle(), userRequest);
        final TableOfContent tableOfContent = tableOfContentEngine.createTableOfContent(pages);
        final Index index = indexEngine.createIndex(pages, userRequest);
        userSessionCache.getIfPresent(userRequest.getRequestId()).setTotalPageCount(
                 1 + pages.size() +
                (int) Math.ceil((double) index.getProductNumberIndex().size() / (IndecesRenderer.MAX_BOX_ROW_NB * IndecesRenderer.PRODUCT_NB_BOX_COLUMN_NB)) +
                (int) Math.ceil((double) index.getProductNameIndex().size() / (IndecesRenderer.MAX_BOX_ROW_NB * IndecesRenderer.PRODUCT_NAME_BOX_COLUMN_NB)));
        return new Document(pages, tableOfContent, index);
    }
}
