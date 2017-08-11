package com.himadri.engine;

import com.google.common.cache.Cache;
import com.himadri.dto.ErrorItem;
import com.himadri.dto.UserRequest;
import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.Item;
import com.himadri.model.service.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ArrayUtils.toStringArray;
import static org.apache.commons.lang3.StringUtils.*;

@Component
public class ItemToBoxConverter {

    public static final String BRAND_EXTENSION = ".psd";

    @Autowired
    private Cache<String, UserSession> userSessionCache;

    public Box createBox(List<Item> items, int indexOfProductGroup, UserRequest userRequest) {
        List<Box.Article> articleList = new ArrayList<>(items.size());
        final String boxTitle = getBoxTitle(items, userRequest);
        for (Item item: items) {
            articleList.add(convertItemToArticle(item, boxTitle));
        }
        Item firstItem = items.get(0);
        return new Box(firstItem.getKepnev(), firstItem.getGyarto() + BRAND_EXTENSION, boxTitle,
                firstItem.getCikkfajta(), firstItem.getCikkcsoportnev(), indexOfProductGroup, 1, articleList);

    }

    Box.Article convertItemToArticle(Item item, String boxTitle) {
        StringBuilder descriptionBuilder = new StringBuilder();
        if (isNotBlank(item.getNagykerar())) {
            descriptionBuilder.append(remove(item.getNagykerar(), '\u00a0')).append(",- ");
        }
        if (isNotBlank(item.getM1()) || isNotBlank(item.getM2()) || isNotBlank(item.getM3())) {
            descriptionBuilder.append(String.format("(Min:%s%s/%s/%s) ", stripToEmpty(item.getM1()),
                    stripToEmpty(item.getMe()), stripToEmpty(item.getM2()), stripToEmpty(item.getM3())));
        }
        final String itemText = stripToEmpty(stripStart(removeStart(item.getCikknev(), boxTitle), " ;"));
        descriptionBuilder.append(itemText);
        return new Box.Article(item.getCikkszam(), remove(item.getKiskerar(), '\u00a0'), descriptionBuilder.toString(), isBlank(itemText));
    }

    String getBoxTitle(List<Item> items, UserRequest userRequest) {
        String boxTitle;
        if (items.size() == 1) {
            boxTitle = substringBefore(items.get(0).getCikknev(), ";");
        } else {
            final String[] titles = toStringArray(items.stream().map(Item::getCikknev).collect(Collectors.toList()).toArray());
            boxTitle = getCommonPrefix(titles);
            if (contains(boxTitle, ';')) {
                boxTitle = substringBeforeLast(boxTitle, ";");
            } else if (!endsWith(boxTitle, " ")) {
                boxTitle = substringBeforeLast(boxTitle, " ");
            }
            if (isBlank(boxTitle)) {
                userSessionCache.getIfPresent(userRequest.getRequestId()).addErrorItem(ErrorItem.Severity.ERROR,
                        ErrorItem.ErrorCategory.FORMATTING, "Az összevont cikkeknek nincs egységes kezdetük: " + Arrays.toString(titles));
            }
        }
        return stripToEmpty(boxTitle);
    }
}
