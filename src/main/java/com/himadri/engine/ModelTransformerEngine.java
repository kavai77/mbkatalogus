package com.himadri.engine;

import com.himadri.model.Box;
import com.himadri.model.Item;
import com.himadri.model.Page;
import com.himadri.model.UserRequest;
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

    public List<Page> createPagesFromItems(List<Item> items, UserRequest userRequest) {
        final Collection<Collection<List<Item>>> itemsPerProductGroupPerBox = itemCategorizerEngine
                .itemsPerProductGroupPerBox(items);
        final List<Box> boxes = boxCollectorEngine.collectBoxes(itemsPerProductGroupPerBox, userRequest);
        return pageCollectorEngine.createPages(boxes, userRequest.getCatalogueTitle());
    }
}
