package com.himadri.engine;

import com.google.common.cache.Cache;
import com.himadri.dto.ErrorItem;
import com.himadri.dto.UserRequest;
import com.himadri.graphics.pdfbox.PDFontService;
import com.himadri.graphics.pdfbox.PdfBoxGraphics;
import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.Item;
import com.himadri.model.service.UserSession;
import com.himadri.renderer.BoxRenderer;
import com.himadri.renderer.Util;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ArrayUtils.toStringArray;
import static org.apache.commons.lang3.StringUtils.*;

@Component
public class ItemToBoxConverter {

    private static final String BRAND_EXTENSION = ".psd";

    @Autowired
    Cache<String, UserSession> userSessionCache;

    @Autowired
    BoxRenderer boxRenderer;

    @Autowired
    PDFontService pdFontService;

    private PdfBoxGraphics pdfBoxGraphics;

    @PostConstruct
    public void init() {
        final PDDocument document = new PDDocument();
        final PDPage page = new PDPage();
        document.addPage(page);
        pdfBoxGraphics = new PdfBoxGraphics(document, page, pdFontService, null, null);
    }

    public List<Box> createBox(List<Item> items, int indexOfProductGroup, UserRequest userRequest) {
        List<Box.Article> articleList = new ArrayList<>(items.size());
        final String boxTitle = getBoxTitle(items, userRequest);
        for (Item item : items) {
            articleList.add(convertItemToArticle(item, boxTitle, userRequest));
        }
        Item firstItem = items.get(0);
        int articleStart = 0;
        final List<Box> boxList = new ArrayList<>();
        while (articleStart < articleList.size()) {
            final BoxRenderer.RequiredOccupiedSpace requiredOccupiedSpace = boxRenderer.calculateRequiredOccupiedSpace(
                    pdfBoxGraphics, articleList, articleStart);
            if (articleStart == requiredOccupiedSpace.getIndexOfNextArticle()) {
                final UserSession userSession = userSessionCache.getIfPresent(userRequest.getRequestId());
                userSession.addErrorItem(ErrorItem.Severity.ERROR, ErrorItem.ErrorCategory.FORMATTING,
                        String.format("A %s leírása túlnyúlik egy oldalon, ezért a teljes boxot kihagytuk.",
                                articleList.get(articleStart).getNumber()));
                return Collections.emptyList();
            }
            boxList.add(new Box(firstItem.getKepnev(), firstItem.getGyarto() + BRAND_EXTENSION, boxTitle,
                    firstItem.getCikkfajta(), firstItem.getCikkcsoportnev(), indexOfProductGroup,
                    Math.max(1, requiredOccupiedSpace.getBoxSize()),
                    articleList.subList(articleStart, requiredOccupiedSpace.getIndexOfNextArticle())));

            articleStart = requiredOccupiedSpace.getIndexOfNextArticle();
        }
        return boxList;

    }

    Box.Article convertItemToArticle(Item item, String boxTitle, UserRequest userRequest) {
        StringBuilder descriptionBuilder = new StringBuilder();
        if (userRequest.isWholeSaleFormat()) {
            if (isNotBlank(item.getNagykerar())) {
                descriptionBuilder.append(remove(item.getNagykerar(), '\u00a0')).append(",- ");
            }
            if (isNotBlank(item.getM1()) || isNotBlank(item.getM2()) || isNotBlank(item.getM3())) {
                descriptionBuilder.append(String.format("(Min:%s%s/%s/%s) ", stripToEmpty(item.getM1()),
                        stripToEmpty(item.getMe()), stripToEmpty(item.getM2()), stripToEmpty(item.getM3())));
            }
            if (descriptionBuilder.length() > 0 && userRequest.isAutoLineBreakAfterMinQty()) {
                descriptionBuilder.append(Util.FORCE_LINE_BREAK_CHARACTERS);
            }
        }
        final String itemText = stripToEmpty(stripStart(removeStart(item.getCikknev(), boxTitle), " ;"));
        descriptionBuilder.append(itemText);
        return new Box.Article(item.getCikkszam(), remove(item.getKiskerar(), '\u00a0'), descriptionBuilder.toString(), isBlank(itemText));
    }

    String getBoxTitle(List<Item> items, UserRequest userRequest) {
        final UserSession userSession = userSessionCache.getIfPresent(userRequest.getRequestId());
        String boxTitle;
        if (items.size() == 1) {
            boxTitle = substringBefore(items.get(0).getCikknev(), ";");
        } else {
            final String[] titles = toStringArray(items.stream().map(Item::getCikknev).collect(Collectors.toList()).toArray());
            boxTitle = getCommonPrefix(titles);
            if (contains(boxTitle, ';')) {
                boxTitle = substringBeforeLast(boxTitle, ";");
            } else {
                if (!endsWith(boxTitle, " ")) {
                    boxTitle = substringBeforeLast(boxTitle, " ");
                }
                if (isBlank(boxTitle)) {
                    userSession.addErrorItem(ErrorItem.Severity.ERROR,
                            ErrorItem.ErrorCategory.FORMATTING, String.format("Az összevont cikkeknek nincs egységes kezdetük. " +
                                    "Első cikkszám: %s. Cikknevek: %s ", items.get(0).getCikkszam(), Arrays.toString(titles)));
                } else {
                    userSession.addErrorItem(ErrorItem.Severity.WARN, ErrorItem.ErrorCategory.FORMATTING, String.format(
                            "Az összevont cikkek közös kezdetében nincs pontosvessző, így az elválasztás automatikusan történik. " +
                                    "Első cikkszám: %s. Cikknevek: %s", items.get(0).getCikkszam(), Arrays.toString(titles)));
                }
            }
        }
        return stripToEmpty(boxTitle);
    }
}
