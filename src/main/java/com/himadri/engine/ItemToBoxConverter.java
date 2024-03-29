package com.himadri.engine;

import com.google.common.cache.Cache;
import com.himadri.dto.ErrorItem;
import com.himadri.dto.UserRequest;
import com.himadri.engine.ItemCategorizerEngine.CsvItemGroup;
import com.himadri.graphics.pdfbox.PDFontService;
import com.himadri.graphics.pdfbox.PdfBoxPageGraphics;
import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.CsvItem;
import com.himadri.model.service.UserSession;
import com.himadri.renderer.BoxRenderer;
import com.himadri.renderer.PageRenderer;
import com.himadri.renderer.Util;
import com.himadri.renderer.imageloader.ImageLoader;
import com.himadri.renderer.imageloader.ImageLoaderServiceRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.himadri.renderer.PageRenderer.*;
import static org.apache.commons.lang3.StringUtils.*;

@Component
public class ItemToBoxConverter {

    private static final String PSD_EXTENSION = ".psd";

    @Autowired
    Cache<String, UserSession> userSessionCache;

    @Autowired
    BoxRenderer boxRenderer;

    @Autowired
    PDFontService pdFontService;

    @Autowired
    ImageLoaderServiceRegistry imageLoaderServiceRegistry;

    @Autowired
    Util util;

    private PdfBoxPageGraphics pdfBoxGraphics;

    @PostConstruct
    public void init() {
        pdfBoxGraphics = PdfBoxPageGraphics.createForStringWidthCalculation(pdFontService);
    }

    public Box createImageBox(InputStream imageInputStream, boolean wideImage, UserRequest userRequest, String errorImageName) {
        if (imageInputStream == null) {
            return null;
        }
        try {
            final BufferedImage image = ImageIO.read(imageInputStream);
            if (image == null) {
                throw new IOException();
            }
            int width = wideImage ? PageRenderer.BOX_COLUMNS_PER_PAGE : 1;
            float scale = BOX_WIDTH * width / image.getWidth();
            int height = (int) Math.ceil(image.getHeight() * scale / BOX_HEIGHT);
            return Box.createImageBox(width, height, image);
        } catch (IOException e) {
            final UserSession userSession = userSessionCache.getIfPresent(userRequest.getRequestId());
            userSession.addErrorItem(ErrorItem.Severity.ERROR, ErrorItem.ErrorCategory.IMAGE,
                String.format("Nem sikerült beolvasni a %s képet, ezért kihagytuk", errorImageName));
            return null;
        }
    }

    public List<Box> createArticleBox(CsvItemGroup items, int indexOfProductGroup, ItemCategorizerEngine.CsvProductGroup productGroup,
                                      UserRequest userRequest, List<Integer> availableBoxHeights) {
        final String boxTitle = getBoxTitle(items.getItems(), userRequest);
        List<Box.Article> articleList = items.getItems().stream().map(item -> convertItemToArticle(item, userRequest)).collect(Collectors.toList());
        CsvItem firstItem = items.getItems().get(0);
        int articleStart = 0;
        final List<Box> boxList = new ArrayList<>();
        final ImageLoader imageLoader = imageLoaderServiceRegistry.getImageLoader(userRequest.getQuality());
        final boolean wideBox = items.getItems().stream()
                .anyMatch(i -> Util.trueValueSet.contains(defaultString(i.getNagykep()).toLowerCase()));
        final boolean newProduct = items.getItems().stream()
                .anyMatch(i -> Util.trueValueSet.contains(defaultString(i.getUj()).toLowerCase()));
        final boolean onNewPage = items.getItems().stream()
                .anyMatch(i -> Util.trueValueSet.contains(defaultString(i.getUjoldalon()).toLowerCase()));
        for (int counter = 0; articleStart < articleList.size(); counter++) {
            int availableBoxSize = counter < availableBoxHeights.size() ? availableBoxHeights.get(counter) : BOX_ROWS_PER_PAGE;
            final BoxRenderer.RequiredHeight requiredOccupiedSpace = boxRenderer.calculateBoxHeight(
                    pdfBoxGraphics, articleList, articleStart, availableBoxSize, wideBox);
            if (articleStart == requiredOccupiedSpace.getIndexOfNextArticle()
                    && availableBoxSize == BOX_ROWS_PER_PAGE) {
                userSessionCache.getIfPresent(userRequest.getRequestId()).addErrorItem(ErrorItem.Severity.ERROR,
                        ErrorItem.ErrorCategory.FORMATTING,
                        String.format("A %s leírása túlnyúlik egy teljes oldalon, ezért a teljes boxot kihagytuk.",
                                articleList.get(articleStart).getNumber()));
                return Collections.emptyList();
            }
            String brandImage = isNotBlank(firstItem.getGyarto()) ? stripToEmpty(firstItem.getGyarto()) + PSD_EXTENSION : null;
            final String imageName = imageLoader.getImageName(firstItem);
            final List<Box.Article> articles = articleList.subList(articleStart, requiredOccupiedSpace.getIndexOfNextArticle());
            Color color;
            if (productGroup.getColor().isPresent()) {
                try {
                    String colorStr = productGroup.getColor().get();
                    colorStr = StringUtils.removeStart(colorStr, "#");
                    colorStr = StringUtils.prependIfMissing(colorStr, "0x");
                    color = Color.decode(colorStr);
                } catch (NumberFormatException e) {
                    color = util.getProductGroupMainColor(indexOfProductGroup);
                    userSessionCache.getIfPresent(userRequest.getRequestId()).addErrorItem(ErrorItem.Severity.WARN,
                        ErrorItem.ErrorCategory.FORMATTING,
                        String.format("Értelmezhetetlen szín %s, figyelmen kívül hagyjuk", productGroup.getColor().get()));
                }
            } else {
                color = util.getProductGroupMainColor(indexOfProductGroup);
            }
            if (!articles.isEmpty()) {
                boxList.add(Box.createArticleBox(imageName, brandImage, boxTitle,
                        stripToEmpty(firstItem.getCikkfajta()), productGroup.getName(), color,
                        wideBox ? PageRenderer.BOX_COLUMNS_PER_PAGE : 1,
                        requiredOccupiedSpace.getBoxHeight(),
                        newProduct, onNewPage, articles));
            }

            articleStart = requiredOccupiedSpace.getIndexOfNextArticle();
        }
        return boxList;

    }

    private Box.Article convertItemToArticle(CsvItem item, UserRequest userRequest) {
        StringBuilder descriptionBuilder = new StringBuilder();
        if (userRequest.isWholeSaleFormat()) {
            if (isNotBlank(item.getNagykerar())) {
                descriptionBuilder
                        .append("<wholesalepricecolor><b>")
                        .append(remove(item.getNagykerar(), '\u00a0'))
                        .append(",-</b></wholesalepricecolor> ");
            }
            if (isNotBlank(item.getErtmenny())) {
                descriptionBuilder
                        .append('#')
                        .append(remove(item.getErtmenny(), '\u00a0'))
                        .append(' ');
            }
            if (isNotBlank(item.getM1()) || isNotBlank(item.getM2()) || isNotBlank(item.getM3())) {
                final String m1m2m3 = String.format("%s%s/%s/%s",
                        stripToEmpty(item.getM1()),
                        stripToEmpty(item.getMe()),
                        stripToEmpty(item.getM2()),
                        stripToEmpty(item.getM3())
                );
                descriptionBuilder.append("(Min:")
                    .append(removePattern(m1m2m3, "/*$"))
                    .append(") ");
            }

            if (descriptionBuilder.length() > 0 && userRequest.isAutoLineBreakAfterMinQty()) {
                descriptionBuilder.append(Util.FORCE_LINE_BREAK_CHARACTERS);
            }
        }
        final String itemText = util.removeLeadingHtmlBreaks(stripToEmpty(item.getLeiras()));
        descriptionBuilder.append(itemText);
        return new Box.Article(item.getCikkszam(), remove(item.getKiskerar(), '\u00a0'),
                descriptionBuilder.toString(), item.getTargymutato(), isEmpty(itemText), StringUtils.defaultString(item.getCsomagolas()),
                StringUtils.defaultString(item.getCikkazon()));
    }

    private String getBoxTitle(List<CsvItem> items, UserRequest userRequest) {
        final String boxTitle = stripToEmpty(normalizeSpace(items.get(0).getDtpmegnevezes()));
        List<String> wrongTitles = items.stream().map(CsvItem::getDtpmegnevezes).map(StringUtils::stripToEmpty)
                .map(StringUtils::normalizeSpace)
                .filter(a -> !StringUtils.equalsIgnoreCase(boxTitle, a)).collect(Collectors.toList());
        if (!wrongTitles.isEmpty()) {
            final UserSession userSession = userSessionCache.getIfPresent(userRequest.getRequestId());
            userSession.addErrorItem(ErrorItem.Severity.WARN,
                    ErrorItem.ErrorCategory.FORMATTING, String.format("Az összevont cikkeknek különbözik a dtp megnevezésük. " +
                            "Első cikkszám: %s. Első DTP megnevezés \"%s\". Eltérő megnevezések: %s ",
                            items.get(0).getCikkszam(), boxTitle, wrongTitles.toString()));
        }

        return boxTitle;
    }
}
