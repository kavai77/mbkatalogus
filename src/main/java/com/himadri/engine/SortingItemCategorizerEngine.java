package com.himadri.engine;

import com.himadri.model.rendering.CsvItem;
import com.himadri.model.service.UserSession;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

public class SortingItemCategorizerEngine implements ItemCategorizerEngine {
    @Override
    public List<CsvProductGroup> groupCsvItems(List<CsvItem> items, UserSession userSession) {
        Map<String, Map<String, List<CsvItem>>> product = new LinkedHashMap<>();
        for (CsvItem item: items) {
            final String productGroupName = stripToEmpty(item.getCikkcsoportnev());
            product.putIfAbsent(productGroupName, new LinkedHashMap<>());
            Map<String, List<CsvItem>> productGroupMap = product.get(productGroupName);
            final String productPictureKey = isNotBlank(item.getKepnev()) ? strip(item.getKepnev()) : UUID.randomUUID().toString();
            productGroupMap.putIfAbsent(productPictureKey, new ArrayList<>());
            productGroupMap.get(productPictureKey).add(item);
        }


        List<CsvProductGroup> retValue = new ArrayList<>();
        product.forEach((k, v) -> {
            final List<CsvItemGroup> boxes = new ArrayList<>();
            v.values().forEach(a -> boxes.add(new CsvItemGroup(a)));
            retValue.add(new CsvProductGroup(k, boxes));
        });
        return retValue;
    }
}
