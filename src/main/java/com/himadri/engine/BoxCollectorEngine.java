package com.himadri.engine;

import com.himadri.dto.UserRequest;
import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class BoxCollectorEngine {
    @Autowired
    private ItemToBoxConverter itemToBoxConverter;

    public List<Box> collectBoxes(Collection<Collection<List<Item>>> itemsPerProductGroupPerBox, UserRequest userRequest) {
        List<Box> boxes = new ArrayList<>();
        int indexOfProductGroup = 0;
        for (Collection<List<Item>> productGroupItemsPerBox: itemsPerProductGroupPerBox) {
            for (List<Item> boxItems: productGroupItemsPerBox) {
                final Box box = itemToBoxConverter.createBox(boxItems, indexOfProductGroup, userRequest);
                boxes.add(box);
            }
            indexOfProductGroup++;
        }
        return boxes;
    }
}
