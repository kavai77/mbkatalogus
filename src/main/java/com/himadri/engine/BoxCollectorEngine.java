package com.himadri.engine;

import com.himadri.dto.UserRequest;
import com.himadri.engine.ItemCategorizerEngine.BoxItems;
import com.himadri.engine.ItemCategorizerEngine.ProductGroup;
import com.himadri.model.rendering.Box;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BoxCollectorEngine {
    @Autowired
    private ItemToBoxConverter itemToBoxConverter;

    public List<Box> collectBoxes(List<ProductGroup> productGroups, UserRequest userRequest) {
        List<Box> boxes = new ArrayList<>();
        for (int i = 0; i < productGroups.size(); i++) {
            ProductGroup productGroup = productGroups.get(i);
            for (BoxItems boxItems : productGroup.getBoxes()) {
                final List<Box> itemBoxes = itemToBoxConverter.createBox(boxItems, i, productGroup.getName(), userRequest);
                boxes.addAll(itemBoxes);
            }
        }
        return boxes;
    }
}
