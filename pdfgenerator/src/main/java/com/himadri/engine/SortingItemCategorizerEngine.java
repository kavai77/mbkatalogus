package com.himadri.engine;

import com.himadri.model.rendering.Item;
import com.himadri.model.service.UserSession;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

public class SortingItemCategorizerEngine implements ItemCategorizerEngine {
    @Override
    public List<ProductGroup> itemsPerProductGroupPerBox(List<Item> items, UserSession userSession) {
        Map<String, Map<String, List<Item>>> product = new LinkedHashMap<>();
        for (Item item: items) {
            final String productGroupName = stripToEmpty(item.getCikkcsoportnev());
            product.putIfAbsent(productGroupName, new LinkedHashMap<>());
            Map<String, List<Item>> productGroupMap = product.get(productGroupName);
            final String productPictureKey = isNotBlank(item.getKepnev()) ? strip(item.getKepnev()) : UUID.randomUUID().toString();
            productGroupMap.putIfAbsent(productPictureKey, new ArrayList<>());
            productGroupMap.get(productPictureKey).add(item);
        }


        List<ProductGroup> retValue = new ArrayList<>();
        product.forEach((k, v) -> {
            final List<BoxItems> boxes = new ArrayList<>();
            v.values().forEach(a -> boxes.add(new BoxItems(a)));
            retValue.add(new ProductGroup(k, boxes));
        });
        return retValue;
    }
}
