package com.himadri.engine;

import com.himadri.model.Item;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.apache.commons.lang.StringUtils.*;

@Component
public class ItemCategorizerEngine {
    public Collection<Collection<List<Item>>> itemsPerProductGroupPerBox(List<Item> items) {
        Map<String, Map<String, List<Item>>> product = new LinkedHashMap<>();
        for (Item item: items) {
            final String productGroupName = stripToEmpty(item.getCikkcsoportnev());
            product.putIfAbsent(productGroupName, new LinkedHashMap<>());
            Map<String, List<Item>> productGroupMap = product.get(productGroupName);
            final String productPictureKey = isNotBlank(item.getKepnev()) ? strip(item.getKepnev()) : UUID.randomUUID().toString();
            productGroupMap.putIfAbsent(productPictureKey, new ArrayList<>());
            productGroupMap.get(productPictureKey).add(item);
        }


        Map<String, Collection<List<Item>>> retValue = new LinkedHashMap<>();
        product.forEach((k, v) -> retValue.put(k, v.values()));
        return retValue.values();
    }
}
