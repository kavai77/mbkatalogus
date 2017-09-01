package com.himadri.engine;

import com.google.common.cache.Cache;
import com.himadri.dto.UserRequest;
import com.himadri.exception.ValidationException;
import com.himadri.model.rendering.*;
import com.himadri.model.service.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class ModelTransformerEngine {
    @Autowired
    private ItemCategorizerEngine itemCategorizerEngine;

    @Autowired
    private BoxCollectorEngine boxCollectorEngine;

    @Autowired
    private PageCollectorEngine pageCollectorEngine;

    @Autowired
    private TableOfContentEngine tableOfContentEngine;

    @Autowired
    private Cache<String, UserSession> userSessionCache;

    public Document createDocumentFromItems(List<Item> items, UserRequest userRequest) throws ValidationException {
        final Collection<Collection<List<Item>>> itemsPerProductGroupPerBox = itemCategorizerEngine
                .itemsPerProductGroupPerBox(items);
        final List<Box> boxes = boxCollectorEngine.collectBoxes(itemsPerProductGroupPerBox, userRequest);
        final List<Page> pages = pageCollectorEngine.createPages(boxes, userRequest.getCatalogueTitle());
        final TableOfContent tableOfContent = tableOfContentEngine.createTableOfContent(pages);
        userSessionCache.getIfPresent(userRequest.getRequestId()).setTotalPageCount(pages.size() + 1);
        return new Document(pages, tableOfContent);
    }
}
