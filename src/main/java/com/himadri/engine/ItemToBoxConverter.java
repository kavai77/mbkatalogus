package com.himadri.engine;

import com.google.common.cache.Cache;
import com.himadri.model.Box;
import com.himadri.model.ErrorCollector;
import com.himadri.model.Item;
import com.himadri.model.UserRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.*;

@Component
public class ItemToBoxConverter {

    public static final String BRAND_EXTENSION = ".psd";

    @Autowired
    private Cache<String, ErrorCollector> userSessionCache;

    public Box createBox(List<Item> items, int indexOfProductGroup, UserRequest userRequest) {
        List<Box.Article> articleList = new ArrayList<>(items.size());
        for (Item item: items) {
            articleList.add(convertItemToArticle(item));
        }
        Item firstItem = items.get(0);
        return new Box(firstItem.getKepnev(), firstItem.getGyarto() + BRAND_EXTENSION, getBoxTitle(items, userRequest),
                firstItem.getCikkfajta(), firstItem.getCikkcsoportnev(), indexOfProductGroup, 1, articleList);

    }

    private Box.Article convertItemToArticle(Item item) {
        StringBuilder descriptionBuilder = new StringBuilder();
        descriptionBuilder.append(item.getKiskerar()).append(",- ");
        if (isNotBlank(item.getM1()) || isNotBlank(item.getM2()) || isNotBlank(item.getM3())) {
            descriptionBuilder.append(String.format("(Min:%s%s/%s/%s) ", stripToEmpty(item.getM1()),
                    stripToEmpty(item.getMe()), stripToEmpty(item.getM2()), stripToEmpty(item.getM3())));
        }
        descriptionBuilder.append(stripToEmpty(substringAfter(item.getCikknev(), ";")));
        return new Box.Article(item.getCikkszam(), item.getNagykerar(), descriptionBuilder.toString());
    }

    private String getBoxTitle(List<Item> items, UserRequest userRequest) {
        String boxTitle = stripToEmpty(substringBefore(items.get(0).getCikknev(), ";"));
        for (int i = 1; i < items.size(); i++) {
            Item item = items.get(i);
            String itemTitle = stripToEmpty(substringBefore(item.getCikknev(), ";"));
            if (!StringUtils.equals(boxTitle, itemTitle)) {
                final ErrorCollector errorCollector = userSessionCache.getIfPresent(userRequest.getRequestId());
                errorCollector.addErrorItem(ErrorCollector.Severity.WARN,
                        "A doboz fejléce (%s) nem egyezik meg az összevont cikk fejlécével (%s). Cikkszám: %s",
                        boxTitle, itemTitle, item.getCikkszam());
            }
        }
        return boxTitle;
    }
}
