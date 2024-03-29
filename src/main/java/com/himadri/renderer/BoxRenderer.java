package com.himadri.renderer;

import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Floats;
import com.himadri.dto.UserRequest;
import com.himadri.exception.ImageNotFoundException;
import com.himadri.graphics.pdfbox.PDFontService;
import com.himadri.graphics.pdfbox.PdfBoxPageGraphics;
import com.himadri.model.rendering.Box;
import com.himadri.model.service.Line;
import com.himadri.model.service.Paragraph;
import com.himadri.model.service.PdfObject;
import com.himadri.model.service.UserSession;
import com.himadri.renderer.imageloader.ImageLoader;
import com.himadri.renderer.imageloader.ImageLoaderServiceRegistry;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.himadri.dto.ErrorItem.ErrorCategory.FORMATTING;
import static com.himadri.dto.ErrorItem.ErrorCategory.IMAGE;
import static com.himadri.dto.ErrorItem.Severity.ERROR;
import static com.himadri.dto.ErrorItem.Severity.WARN;
import static com.himadri.renderer.PageRenderer.BOX_HEIGHT;
import static com.himadri.renderer.PageRenderer.BOX_WIDTH;
import static java.lang.Math.min;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class BoxRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoxRenderer.class);
    static final int MAX_HEADLINE_LINES = 2;
    static final Map<Integer, int[]> HEAD_LINE_POS_MAP = ImmutableMap.of(
        1, new int[] {13},
        2, new int[] {10, 22}
    );
    static final float BOX_SYMBOL_WIDTH = 8f;
    public static final int BARCODE_WIDTH = 45;

    @Autowired
    private Cache<String, UserSession> userSessionCache;
    
    @Autowired
    private Util util;
    
    @Autowired
    private PDFontService fontService;

    @Autowired
    private ImageLoaderServiceRegistry imageLoaderServiceRegistry;

    private final BoxMetrics regularBoxMetrics = new BoxMetrics(
            95f,
            BOX_HEIGHT - 10,
            33f,
            33f,
            5,
            105f,
            157.5f,
            27f,
            12f,
            3f,
            6,
            1
    );

    private final BoxMetrics wideBoxMetrics = new BoxMetrics(
            BOX_WIDTH - 10,
            2 * BOX_HEIGHT - 10,
            2 * 33f,
            2 * 33f,
            5,
            BOX_WIDTH,
            255,
            30f,
            12f,
            3f,
            14,
            2
    );

    public void drawBox(PdfBoxPageGraphics g2, Box box, UserRequest userRequest) {
        switch (box.getBoxType()) {
            case ARTICLE:
                drawArticleBox(g2, box, userRequest);
                break;
            case IMAGE:
                drawImageBox(g2, box);
                break;
        }
    }

    private void drawImageBox(PdfBoxPageGraphics g2, Box box) {
        BufferedImage image = box.getBufferedImage();
        float scale = (BOX_WIDTH * box.getWidth()) / image.getWidth();
        float ty;
        if (box.getRow() == 0) { // headline image
            ty = (box.getHeight() * BOX_HEIGHT - image.getHeight() * scale) / 2f;
        } else { // footline image
            ty = box.getHeight() * BOX_HEIGHT - image.getHeight() * scale;
        }
        g2.drawImage(image, 0, ty, image.getWidth() * scale, image.getHeight() * scale);

        if (box.getRow() == 0 && box.getWidth() == 1) {
            drawBottomLine(g2, box, regularBoxMetrics);
        }
    }

    private void drawArticleBox(PdfBoxPageGraphics g2, Box box, UserRequest userRequest) {
        final UserSession userSession = userSessionCache.getIfPresent(userRequest.getRequestId());

        // draw image
        PDImageXObject image = null;
        final ImageLoader imageLoader = imageLoaderServiceRegistry.getImageLoader(userRequest.getQuality());
        final BoxMetrics m = box.getWidth() == 1 ? regularBoxMetrics : wideBoxMetrics;
        try {
            image = imageLoader.loadImage(box, g2.getDocument(), userSession);
            if (image != null) {
                float scale = min(1f, min(m.getImageWidthMax() / image.getWidth(), m.getImageHeightMax() / image.getHeight()));
                float posX = (m.getTextBoxX() - image.getWidth() * scale) / 2;
                float posY = (m.getInitialHeight() * BOX_HEIGHT - image.getHeight() * scale) / 2;
                g2.drawImage(image, posX, posY, image.getWidth() * scale, image.getHeight() * scale);
            }
        } catch (ImageNotFoundException e) {
            userSession.addErrorItem(WARN, IMAGE, "Nem található a kép: " + box.getImage());
        } catch (IOException e) {
            userSession.addErrorItem(ERROR, IMAGE, String.format("Nem lehetett kirajzolni a képet: %s. Hibaüzenet: %s",
                    box.getImage(), e.getMessage()));
            LOGGER.error("Could not paint image {}", box, e);
        }

        // draw the logo
        if (isNotBlank(box.getBrandImage()) && image != null) {
            try {
                PDImageXObject logoImage = imageLoader.loadLogoImage(box, g2.getDocument(), userSession);
                if (logoImage != null) {
                    float scale = min(1f, min(m.getLogoImageWidthMax() / logoImage.getWidth(), m.getLogoImageHeightMax() / logoImage.getHeight()));
                    g2.drawImage(logoImage, 3f, m.getImageHeightMax() - logoImage.getHeight() * scale + 5, logoImage.getWidth() * scale, logoImage.getHeight() * scale);
                }
            } catch (ImageNotFoundException e) {
                userSession.addErrorItem(WARN, IMAGE, "Nem található a logo kép: " + box.getBrandImage());
            } catch (IOException e) {
                userSession.addErrorItem(ERROR, IMAGE, String.format("Nem lehetett kirajzolni a logo képet: %s. Hibaüzenet: %s",
                        box.getBrandImage(), e.getMessage()));
                LOGGER.error("Could not paint logo image {}", box, e);
            }
        }

        // draw the new logo
        if (box.isNewProduct()) {
            try {
                PDImageXObject newImage = imageLoader.loadResourceImage("/uj.png", g2.getDocument());
                if (newImage != null) {
                    final float scale = min(1f, min(m.getLogoImageWidthMax() / newImage.getWidth(), m.getLogoImageHeightMax() / newImage.getHeight()));
                    g2.drawImage(newImage, m.getImageWidthMax() - newImage.getWidth() * scale + 5,
                        m.getImageHeightMax() - newImage.getHeight() * scale + 5,
                        newImage.getWidth() * scale,
                        newImage.getHeight() * scale);
                }
            } catch (IOException e) {
                userSession.addErrorItem(WARN, IMAGE, "Hibás Új termék logó");
            }
        }

        final BoxPositions boxPositions = calculateBoxPositions(g2, box.getArticles(), m);
        final BoxPosition mainBoxPosition = boxPositions.getMainBoxPosition();

        // headline box
        final Color mainColor = box.getProductColor();
        g2.setNonStrokingColor(mainColor);
//        g2.setPaint(new LinearGradientPaint(TEXT_BOX_X, textBoxHeadHeight, TEXT_BOX_X + TEXT_BOX_WIDTH, 0,
//                new float[]{0.0f, 0.5f, 1f}, new Color[]{mainColor, Color.white, mainColor}));
        g2.fillRect(m.getTextBoxX(), 0, m.getTextBoxWidth(), m.getTextBoxHeadHeight());

        // text boxes
        final Color grayBackground = new Color(224, 224, 244);
        boolean colorAlternate = false;
        for (float y = m.getTextBoxHeadHeight(); y < box.getHeight() * BOX_HEIGHT - m.getTextBoxLineHeight();
             y+=m.getTextBoxLineHeight()) {
            g2.setNonStrokingColor(colorAlternate ? Color.white : grayBackground);
            colorAlternate = !colorAlternate;
            if (y < m.getInitialHeight() * BOX_HEIGHT) {
                g2.fillRect(m.getTextBoxX(), y, m.getTextBoxWidth(), m.getTextBoxLineHeight());
            } else {
                g2.fillRect(m.getBoxStart(), y,m.getTextBoxX() + m.getTextBoxWidth() - m.getBoxStart(), m.getTextBoxLineHeight());
            }
        }

        // category
        g2.setNonStrokingColor(new Color(38, 66, 140));
        g2.setFont(Fonts.BOX_PRODUCT_CATEGORY_FONT);
        final float categoryStart = mainBoxPosition.getTextEnd() - g2.getStringWidth(box.getCategory());
        g2.drawString(box.getCategory(), categoryStart,22);

        // heading text
        g2.setNonStrokingColor(Color.black);
        g2.setStrokingColor(Color.black);
        g2.setFont(Fonts.BOX_TITLE_FONT);
        g2.setLineWidth(.5f);
        Paragraph headingTextLines = util.splitMultiLineText(g2, Fonts.BOX_TITLE_FONT, box.getTitle(), false,
                mainBoxPosition.getTextEnd() - mainBoxPosition.getTextStart(),
                categoryStart - mainBoxPosition.getTextStart() - m.getTextMargin());
        if (!headingTextLines.getLines().isEmpty()) {
            final int lineCount = min(MAX_HEADLINE_LINES, headingTextLines.getLines().size());
            for (int l = 0; l < lineCount; l++) {
                for (PdfObject pdfObject: headingTextLines.getLines().get(l).getObjects()) {
                    pdfObject.render(mainBoxPosition.getTextStart(), HEAD_LINE_POS_MAP.get(lineCount)[l]);
                }
            }
            if (headingTextLines.getLines().size() > MAX_HEADLINE_LINES) {
                userSession.addErrorItem(ERROR, FORMATTING, String.format("Túl hosszú a box fejléce, le kellett vágni a második sorban. " +
                    "Cikkszám: %s. Teljes címsor: %s", box.getArticles().get(0).getNumber(), box.getTitle()));
            }
        } else {
            userSession.addErrorItem(ERROR, FORMATTING, String.format("Nincs box fejléce (dtp megnevezés): %s", box.getArticles().get(0).getNumber()));
        }

        // description
        int currentLine = 0;
        for (int articleIndex = 0; articleIndex < box.getArticles().size(); articleIndex++) {
            Box.Article article = box.getArticles().get(articleIndex);
            BoxPosition boxPosition = getBoxPositionForLine(boxPositions, currentLine, m);

            // product number
            g2.setNonStrokingColor(Fonts.BOX_PRODUCT_NUMBER_COLOR);
            g2.setFont(Fonts.BOX_PRODUCT_NUMBER_FONT);
            g2.drawString(article.getNumber(), boxPosition.getTextStart(), getLineYBaseLine(currentLine, m));

            // price
            g2.setNonStrokingColor(Color.black);
            g2.setFont(Fonts.BOX_PRICE_FONT);
            g2.drawString(article.getPrice(), boxPosition.getTextEnd() - g2.getStringWidth(article.getPrice()),
                    getLineYBaseLine(currentLine, m));

            // gyűjtő és karton
            if (isNotBlank(article.getPackaging())) {
                g2.setNonStrokingColor(Color.black);
                g2.setFont(Fonts.BOX_PRODUCT_DESCRIPTION_FONT);
                float firstLineDescriptionEnds = boxPosition.getDescriptionEnd() -
                    boxPositions.getMainBoxPosition().getPriceWidth()[articleIndex] -
                    boxPositions.getMainBoxPosition().getPackagingWidth()[articleIndex];
                g2.drawString(article.getPackaging(),
                    firstLineDescriptionEnds + m.getTextMargin() + BOX_SYMBOL_WIDTH,
                    getLineYBaseLine(currentLine, m));
                try {
                    PDImageXObject boxImage = imageLoader.loadResourceImage("/box.png", g2.getDocument());
                    if (boxImage != null) {
                        final float scale = BOX_SYMBOL_WIDTH / boxImage.getWidth();
                        g2.drawImage(boxImage,
                            firstLineDescriptionEnds + m.getTextMargin() / 2,
                            getLineYBaseLine(currentLine, m) - boxImage.getHeight() * scale,
                            boxImage.getWidth() * scale,
                            boxImage.getHeight() * scale);
                    }
                } catch (IOException e) {
                    userSession.addErrorItem(WARN, IMAGE, "Hibás doboz logó a gyűjtő és karton mennyiségekhez");
                }
            }

            // description
            g2.setNonStrokingColor(Color.black);
            g2.setStrokingColor(Color.black);
            g2.setFont(Fonts.BOX_PRODUCT_DESCRIPTION_FONT);
            g2.setLineWidth(.5f);
            float[] splitWidths = getSplitWidths(boxPositions, articleIndex, currentLine, m);
            final Paragraph paragraph = util.splitMultiLineText(g2, Fonts.BOX_PRODUCT_DESCRIPTION_FONT,
                article.getDescription(), isNotBlank(article.getCikkAzon()), splitWidths);
//            int lineNb = 0;
            for (Line line: paragraph.getLines()) {
                for (PdfObject pdfObject: line.getObjects()) {
                    pdfObject.render(boxPosition.getDescriptionStart(), getLineYBaseLine(currentLine, m));
                }
//                g2.drawLine(boxPosition.getDescriptionStart() + splitWidths[lineNb], getLineYBaseLine(currentLine, m) - 10,
//                        boxPosition.getDescriptionStart() + splitWidths[lineNb], getLineYBaseLine(currentLine, m));
//                if (lineNb < splitWidths.length - 1) lineNb++;
                boxPosition = getBoxPositionForLine(boxPositions, ++currentLine, m);
            }

            // vonalkód
            if (isNotBlank(article.getCikkAzon())) {
                try {
                    EAN13Bean bean = new EAN13Bean();
                    bean.setHeight(m.getTextBoxLineHeight());
                    bean.doQuietZone(false);
                    bean.setMsgPosition(HumanReadablePlacement.HRP_NONE);
                    BitmapCanvasProvider provider = new BitmapCanvasProvider(300, BufferedImage.TYPE_BYTE_GRAY,true, 0);
                    bean.generateBarcode(provider, article.getCikkAzon());
                    provider.finish();
                    g2.drawImage(provider.getBufferedImage(), boxPosition.getTextEnd() - BARCODE_WIDTH + m.getTextMargin(), m.getTextBoxHeadHeight() + (currentLine - 1) * m.getTextBoxLineHeight(), BARCODE_WIDTH, m.getTextBoxLineHeight());
                } catch (IOException e) {
                    userSession.addErrorItem(WARN, FORMATTING, "Nem sikerült a vonalkód rajzolása: " + article.getCikkAzon());
                }
            }
        }

        drawBottomLine(g2, box, m);
    }

    private void drawBottomLine(PdfBoxPageGraphics g2, Box box, BoxMetrics m) {
        g2.setStrokingColor(Color.lightGray);
        g2.setLineWidth(.5f);
        g2.drawLine(m.getBoxStart(), box.getHeight() * BOX_HEIGHT, m.getTextBoxX() + m.getTextBoxWidth(),
                box.getHeight() * BOX_HEIGHT);
    }

    public RequiredHeight calculateBoxHeight(PdfBoxPageGraphics g2, List<Box.Article> articles,
                                             int articleStartIndex, int availableBoxes, boolean isWide) {
        final BoxMetrics m = isWide ? wideBoxMetrics : regularBoxMetrics;
        final BoxPositions boxPositions = calculateBoxPositions(g2, articles, m);
        int lineCount = 0;
        float requiredSpace = m.getTextBoxHeadHeight();
        for (int i = articleStartIndex; i < articles.size(); i++) {
            Box.Article article = articles.get(i);
            Paragraph paragraph = util.splitMultiLineText(g2, Fonts.BOX_PRODUCT_DESCRIPTION_FONT,
                    article.getDescription(), isNotBlank(article.getCikkAzon()), getSplitWidths(boxPositions, i, lineCount, m));
            requiredSpace += paragraph.getLines().size() * m.getTextBoxLineHeight();
            lineCount += paragraph.getLines().size();
            if (requiredSpace > availableBoxes * BOX_HEIGHT) {
                return new RequiredHeight(availableBoxes, i);
            }
        }
        final int calculatedBoxHeight = (int) Math.ceil(requiredSpace / BOX_HEIGHT);
        return new RequiredHeight(Math.max(m.getInitialHeight(), calculatedBoxHeight), articles.size());
    }



    private BoxPosition getBoxPositionForLine(BoxPositions boxPositions, int currentLine, BoxMetrics m) {
        return currentLine < m.getMainBoxLineCount() ? boxPositions.getMainBoxPosition() :
                boxPositions.getExtendedBoxPosition();
    }

    private float[] getSplitWidths(BoxPositions boxPositions, int indexOfArticle, int currentLine, BoxMetrics m) {
        if (currentLine >= m.getMainBoxLineCount()) {
            return new float[]{
                boxPositions.getExtendedBoxPosition().getDescriptionEnd() -
                boxPositions.getExtendedBoxPosition().getDescriptionStart() -
                boxPositions.getExtendedBoxPosition().getPriceWidth()[indexOfArticle] -
                boxPositions.getExtendedBoxPosition().getPackagingWidth()[indexOfArticle],
                boxPositions.getExtendedBoxPosition().getDescriptionEnd() -
                boxPositions.getExtendedBoxPosition().getDescriptionStart()};
        } else {
            float[] splitWidths = new float[m.getMainBoxLineCount() - currentLine + 1];
            splitWidths[0] = boxPositions.getMainBoxPosition().getDescriptionEnd() -
                             boxPositions.getMainBoxPosition().getDescriptionStart() -
                             boxPositions.getMainBoxPosition().getPriceWidth()[indexOfArticle] -
                             boxPositions.getMainBoxPosition().getPackagingWidth()[indexOfArticle];
            if (m.getMainBoxLineCount() - currentLine > 1) {
                Arrays.fill(splitWidths, 1, m.getMainBoxLineCount() - currentLine,
                    boxPositions.getMainBoxPosition().getDescriptionEnd() -
                        boxPositions.getMainBoxPosition().getDescriptionStart());
            }
            splitWidths[m.getMainBoxLineCount() - currentLine] =
                    boxPositions.getExtendedBoxPosition().getDescriptionEnd() -
                    boxPositions.getExtendedBoxPosition().getDescriptionStart();
            return splitWidths;
        }
    }

    private float getLineYBaseLine(int line, BoxMetrics m) {
        return m.getTextBoxHeadHeight() + (line + 1) * m.getTextBoxLineHeight() - m.getTextMargin();
    }

    private BoxPositions calculateBoxPositions(PdfBoxPageGraphics g2, List<Box.Article> articles, BoxMetrics m) {
        final float mainBoxTextStart = m.getTextBoxX() + m.getTextMargin();
        final float boxTextEnd = m.getTextBoxX() + m.getTextBoxWidth() - m.getTextMargin();
        final float extendedBoxTextStart = m.getBoxStart() + m.getTextMargin();
        final PDFont productNumberFont = fontService.getPDFont(g2.getDocument(), Fonts.BOX_PRODUCT_NUMBER_FONT);
        final float maxProductNumberWidth = (float) articles.stream().map(Box.Article::getNumber).mapToDouble(
                a -> g2.getStringWidth(productNumberFont, Fonts.BOX_PRODUCT_NUMBER_FONT.getSize2D(), a)).max().getAsDouble();
        final PDFont priceFont = fontService.getPDFont(g2.getDocument(), Fonts.BOX_PRICE_FONT);
        final float[] priceWidths = Floats.toArray(
                articles.stream().
                map(article -> {
                    float priceWidth = g2.getStringWidth(priceFont, Fonts.BOX_PRICE_FONT.getSize2D(), article.getPrice());
                    return priceWidth + m.getTextMargin() / 2;
                }).
                collect(Collectors.toList()));
        final float[] packagingWidths = Floats.toArray(
            articles.stream().
                map(article -> isNotBlank(article.getPackaging())
                    ? g2.getStringWidth(priceFont, Fonts.BOX_PRODUCT_DESCRIPTION_FONT.getSize2D(), article.getPackaging())
                        + 2 * m.getTextMargin() + BOX_SYMBOL_WIDTH
                    : 0)
                .collect(Collectors.toList()));
        final BoxPosition mainBoxPosition = BoxPosition.builder()
                .textStart(mainBoxTextStart)
                .textEnd(boxTextEnd)
                .descriptionStart(mainBoxTextStart + maxProductNumberWidth + m.getTextMargin())
                .descriptionEnd(boxTextEnd)
                .packagingWidth(packagingWidths)
                .priceWidth(priceWidths)
                .build();
        final BoxPosition extendedBoxPosition = BoxPosition.builder()
                .textStart(extendedBoxTextStart)
                .textEnd(boxTextEnd)
                .descriptionStart(extendedBoxTextStart + maxProductNumberWidth + m.getTextMargin())
                .descriptionEnd(boxTextEnd)
                .packagingWidth(packagingWidths)
                .priceWidth(priceWidths)
                .build();
        return new BoxPositions(mainBoxPosition, extendedBoxPosition);
    }

    private static class BoxPositions {
        private final BoxPosition mainBoxPosition;
        private final BoxPosition extendedBoxPosition;

        public BoxPositions(BoxPosition mainBoxPosition, BoxPosition extendedBoxPosition) {
            this.mainBoxPosition = mainBoxPosition;
            this.extendedBoxPosition = extendedBoxPosition;
        }

        public BoxPosition getMainBoxPosition() {
            return mainBoxPosition;
        }

        public BoxPosition getExtendedBoxPosition() {
            return extendedBoxPosition;
        }
    }

    @Builder
    @Getter
    private static class BoxPosition {
        private final float descriptionStart;
        private final float descriptionEnd;
        private final float[] priceWidth;
        private final float[] packagingWidth;
        private final float textStart;
        private final float textEnd;
    }

    @Data
    public static class RequiredHeight {
        private final int boxHeight;
        private final int indexOfNextArticle;
    }

}
