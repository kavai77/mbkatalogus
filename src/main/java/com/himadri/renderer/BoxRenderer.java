package com.himadri.renderer;

import com.google.common.cache.Cache;
import com.google.common.primitives.Floats;
import com.himadri.dto.UserRequest;
import com.himadri.graphics.pdfbox.PDFontService;
import com.himadri.graphics.pdfbox.PdfBoxPageGraphics;
import com.himadri.model.rendering.Box;
import com.himadri.model.service.UserSession;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.himadri.dto.ErrorItem.ErrorCategory.FORMATTING;
import static com.himadri.dto.ErrorItem.ErrorCategory.IMAGE;
import static com.himadri.dto.ErrorItem.Severity.ERROR;
import static com.himadri.dto.ErrorItem.Severity.WARN;
import static com.himadri.renderer.PageRenderer.BOX_HEIGHT;
import static java.lang.Math.min;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.stripToEmpty;

@Component
public class BoxRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoxRenderer.class);

    private static final float IMAGE_WIDTH_MAX = 95f;
    private static final float IMAGE_HEIGHT_MAX = 89f;
    private static final float LOGO_IMAGE_WIDTH_MAX = 33f;
    private static final float LOGO_IMAGE_HEIGHT_MAX = 33f;
    private static final float BOX_START = 5;
    private static final float TEXT_BOX_X = 105f;
    private static final float TEXT_BOX_WIDTH = 150f;
    private static final float TEXT_BOX_HEAD_HEIGHT = 27f;
    private static final float TEXT_BOX_LINE_HEIGHT = 12f;
    private static final float TEXT_MARGIN = 3f;
    private static final int MAIN_TEXT_BOX_LINE_COUNT = 6;
    private static final float MAX_SPACE_PER_PAGE = PageRenderer.BOX_ROWS_PER_PAGE * BOX_HEIGHT;

    @Autowired
    private LogoImageCache logoImageCache;

    @Autowired
    private Cache<String, UserSession> userSessionCache;
    
    @Autowired
    private Util util;
    
    @Autowired
    private PDFontService fontService;

    @Value("${imageLocation}")
    private String imageLocation;

    @Value("${logoImageLocation}")
    private String logoImageLocation;

    @PostConstruct
    public void init() {
        File imageLocationFile = new File(imageLocation);
        if (!imageLocationFile.exists() || !imageLocationFile.isDirectory()) {
            throw new RuntimeException(String.format("The configured path for image location %s does not exist", imageLocation));
        }

        File logoImageLocationFile = new File(logoImageLocation);
        if (!logoImageLocationFile.exists() || !logoImageLocationFile.isDirectory()) {
            throw new RuntimeException(String.format("The configured path for logo image location %s does not exist", logoImageLocation));
        }
    }

    public void drawBox(PdfBoxPageGraphics g2, Box box, UserRequest userRequest) {
        final UserSession userSession = userSessionCache.getIfPresent(userRequest.getRequestId());

        // draw image
        final File imageFile = new File(imageLocation, stripToEmpty(box.getImage()));
        if (!imageFile.exists() || !imageFile.isFile()) {
            userSession.addErrorItem(WARN, IMAGE, "Nem található a kép: " + box.getImage());
        } else if (!userRequest.isDraftMode()) {
            try (InputStream fis = new FileInputStream(imageFile)) {
                BufferedImage image = ImageIO.read(fis);
                float scale = min(1f, min(IMAGE_WIDTH_MAX / image.getWidth(), IMAGE_HEIGHT_MAX / image.getHeight()));
                float posX = (TEXT_BOX_X - image.getWidth() * scale) / 2;
                float posY = (BOX_HEIGHT - image.getHeight() * scale) / 2;
                g2.drawImage(image, posX, posY, image.getWidth() * scale, image.getHeight() * scale);
            } catch (IOException e) {
                userSession.addErrorItem(ERROR, IMAGE, String.format("Nem lehetett kirajzolni a képet: %s. Hibaüzenet: %s",
                        box.getImage(), e.getMessage()));
                LOGGER.error("Could not paint image {}", box, e);
            }
        }

        // draw the logo
        final File logoImageFile = new File(logoImageLocation, stripToEmpty(box.getBrandImage()));
        if (!logoImageFile.exists() || !logoImageFile.isFile()) {
            userSession.addErrorItem(WARN, IMAGE, "Nem található a logo file kép: " + box.getBrandImage());
        } else if (!userRequest.isDraftMode()) {
            try {
                BufferedImage logoImage = logoImageCache.getLogoImage(logoImageFile);
                float scale = min(1f, min(LOGO_IMAGE_WIDTH_MAX / logoImage.getWidth(), LOGO_IMAGE_HEIGHT_MAX / logoImage.getHeight()));
                g2.drawImage(logoImage, 3f, 3f, logoImage.getWidth() * scale, logoImage.getHeight() * scale);
            } catch (IOException e) {
                userSession.addErrorItem(ERROR, IMAGE, String.format("Nem lehetett kirajzolni a logo képet: %s. Hibaüzenet: %s",
                        box.getImage(), e.getMessage()));
                LOGGER.error("Could not paint logo image", e);
            }
        }

        final BoxPositions boxPositions = calculateBoxPositions(g2, box.getArticles());
        final BoxPosition mainBoxPosition = boxPositions.getMainBoxPosition();

        // headline box
        final Color mainColor = util.getBoxMainColor(box);
        g2.setNonStrokingColor(mainColor);
//        g2.setPaint(new LinearGradientPaint(TEXT_BOX_X, TEXT_BOX_HEAD_HEIGHT, TEXT_BOX_X + TEXT_BOX_WIDTH, 0,
//                new float[]{0.0f, 0.5f, 1f}, new Color[]{mainColor, Color.white, mainColor}));
        g2.fillRect(TEXT_BOX_X, 0, TEXT_BOX_WIDTH, TEXT_BOX_HEAD_HEIGHT);

        // text boxes
        final Color grayBackground = new Color(224, 224, 244);
        boolean colorAlternate = false;
        for (float y = TEXT_BOX_HEAD_HEIGHT; y < box.getOccupiedSpace() * BOX_HEIGHT - TEXT_BOX_LINE_HEIGHT; y+=TEXT_BOX_LINE_HEIGHT) {
            g2.setNonStrokingColor(colorAlternate ? Color.white : grayBackground);
            colorAlternate = !colorAlternate;
            if (y < BOX_HEIGHT) {
                g2.fillRect(TEXT_BOX_X, y, TEXT_BOX_WIDTH, TEXT_BOX_LINE_HEIGHT);
            } else {
                g2.fillRect(BOX_START, y,TEXT_BOX_X + TEXT_BOX_WIDTH - BOX_START, TEXT_BOX_LINE_HEIGHT);
            }
        }

        // category
        g2.setNonStrokingColor(new Color(38, 66, 140));
        g2.setFont(Fonts.BOX_PRODUCT_CATEGORY_FONT);
        final float categoryStart = mainBoxPosition.getTextEnd() - g2.getStringWidth(box.getCategory());
        g2.drawString(box.getCategory(), categoryStart,22);

        // heading text
        g2.setNonStrokingColor(Color.black);
        g2.setFont(Fonts.BOX_TITLE_FONT);
        String[] headingTextLines = util.splitGraphicsText(g2, Fonts.BOX_TITLE_FONT, box.getTitle(),
                mainBoxPosition.getTextEnd() - mainBoxPosition.getTextStart(),
                categoryStart - mainBoxPosition.getTextStart() - TEXT_MARGIN);
        if (headingTextLines.length == 0) {
            userSession.addErrorItem(ERROR, FORMATTING, String.format("Nincs címsor: %s", box.getArticles().get(0).getNumber()));
        } else if (headingTextLines.length == 1) {
            g2.drawString(headingTextLines[0], mainBoxPosition.getTextStart(), 13);
        } else {
            g2.drawString(headingTextLines[0], mainBoxPosition.getTextStart(), 10);
            g2.drawString(headingTextLines[1], mainBoxPosition.getTextStart(), 22);
            if (headingTextLines.length > 2) {
                final Box.Article firstArticle = box.getArticles().get(0);
                if (box.getArticles().size() == 1 && firstArticle.isEmptyItemText()) {
                    String newItemText = join(headingTextLines, ' ', 2, headingTextLines.length);
                    box.getArticles().set(0, new Box.Article(firstArticle.getNumber(), firstArticle.getPrice(),
                            firstArticle.getDescription() + newItemText, false));
                    userSession.addErrorItem(WARN, FORMATTING, String.format("Nincs pontosvesszős elválasztás a cikknévben, " +
                                    "így autmatikus tördelést alkalmaztunk a címsor és a leírás között. " +
                                    "Cikkszám: %s. Címsor: %s. Leírás: %s", box.getArticles().get(0).getNumber(),
                            join(headingTextLines[0], " ", headingTextLines[1]), newItemText));
                } else {
                    userSession.addErrorItem(ERROR, FORMATTING, String.format("Túl hosszú a címsor, le kellett vágni a második sorban. " +
                            "Cikkszám: %s. Teljes címsor: %s", box.getArticles().get(0).getNumber(), box.getTitle()));
                }
            }
        }

        // description
        int currentLine = 0;
        for (int articleIndex = 0; articleIndex < box.getArticles().size(); articleIndex++) {
            Box.Article article = box.getArticles().get(articleIndex);
            BoxPosition boxPosition = getBoxPositionForLine(boxPositions, currentLine);

            // product number
            g2.setNonStrokingColor(Color.black);
            g2.setFont(Fonts.BOX_PRODUCT_NUMBER_FONT);
            g2.drawString(article.getNumber(), boxPosition.getTextStart(), getLineYBaseLine(currentLine));

            // price
            g2.setNonStrokingColor(Color.black);
            g2.setFont(Fonts.BOX_PRICE_FONT);
            g2.drawString(article.getPrice(), boxPosition.getTextEnd() - g2.getStringWidth(article.getPrice()),
                    getLineYBaseLine(currentLine));

            // description
            g2.setNonStrokingColor(Color.black);
            g2.setFont(Fonts.BOX_PRODUCT_DESCRIPTION_FONT);
            final String[] descriptionSplit = util.splitGraphicsText(g2, Fonts.BOX_PRODUCT_DESCRIPTION_FONT, article.getDescription(),
                    getSplitWidths(boxPositions, articleIndex, currentLine));
            for (String line: descriptionSplit) {
                g2.drawString(line, getBoxPositionForLine(boxPositions, currentLine).getDescriptionStart(),
                        getLineYBaseLine(currentLine));
                currentLine++;
            }
        }


        // bottom line
        g2.setStrokingColor(Color.lightGray);
        g2.setLineWidth(.5f);
        g2.drawLine(BOX_START, box.getOccupiedSpace() * BOX_HEIGHT, TEXT_BOX_X + TEXT_BOX_WIDTH,
                box.getOccupiedSpace() * BOX_HEIGHT);
    }

    public RequiredOccupiedSpace calculateRequiredOccupiedSpace(PdfBoxPageGraphics g2, List<Box.Article> articles, int articleStartIndex) {
        final BoxPositions boxPositions = calculateBoxPositions(g2, articles);
        int lineCount = 0;
        float requiredSpace = TEXT_BOX_HEAD_HEIGHT;
        for (int i = articleStartIndex; i < articles.size(); i++) {
            final String[] descriptionSplit = util.splitGraphicsText(g2, Fonts.BOX_PRODUCT_DESCRIPTION_FONT, articles.get(i).getDescription(),
                    getSplitWidths(boxPositions, i, lineCount));
            requiredSpace += descriptionSplit.length * TEXT_BOX_LINE_HEIGHT;
            lineCount += descriptionSplit.length;
            if (requiredSpace > MAX_SPACE_PER_PAGE) {
                return new RequiredOccupiedSpace((int)Math.ceil((requiredSpace - descriptionSplit.length * TEXT_BOX_LINE_HEIGHT) / BOX_HEIGHT), i);
            }
        }
        return new RequiredOccupiedSpace((int)Math.ceil(requiredSpace / BOX_HEIGHT), articles.size());
    }

    private BoxPosition getBoxPositionForLine(BoxPositions boxPositions, int currentLine) {
        return currentLine < MAIN_TEXT_BOX_LINE_COUNT ? boxPositions.getMainBoxPosition() :
                boxPositions.getExtendedBoxPosition();
    }

    private float[] getSplitWidths(BoxPositions boxPositions, int indexOfArticle, int currentLine) {
        if (currentLine >= MAIN_TEXT_BOX_LINE_COUNT) {
            return new float[]{boxPositions.getExtendedBoxPosition().getDescriptionEnd(indexOfArticle) -
                    boxPositions.getExtendedBoxPosition().getDescriptionStart()};
        } else {
            float[] splitWidths = new float[MAIN_TEXT_BOX_LINE_COUNT - currentLine + 1];
            Arrays.fill(splitWidths, 0, MAIN_TEXT_BOX_LINE_COUNT - currentLine,
                    boxPositions.getMainBoxPosition().getDescriptionEnd(indexOfArticle) -
                            boxPositions.getMainBoxPosition().getDescriptionStart());
            splitWidths[MAIN_TEXT_BOX_LINE_COUNT - currentLine] =
                    boxPositions.getExtendedBoxPosition().getDescriptionEnd(indexOfArticle) -
                    boxPositions.getExtendedBoxPosition().getDescriptionStart();
            return splitWidths;
        }
    }

    private float getLineYBaseLine(int line) {
        return TEXT_BOX_HEAD_HEIGHT + (line + 1) * TEXT_BOX_LINE_HEIGHT - TEXT_MARGIN;
    }

    private BoxPositions calculateBoxPositions(PdfBoxPageGraphics g2, List<Box.Article> articles) {
        final float mainBoxTextStart = TEXT_BOX_X + TEXT_MARGIN;
        final float boxTextEnd = TEXT_BOX_X + TEXT_BOX_WIDTH - TEXT_MARGIN;
        final float extendedBoxTextStart = BOX_START + TEXT_MARGIN;
        final PDFont productNumberFont = fontService.getPDFont(g2.getDocument(), Fonts.BOX_PRODUCT_NUMBER_FONT);
        final float maxProductNumberWidth = (float) articles.stream().map(Box.Article::getNumber).mapToDouble(
                a -> g2.getStringWidth(productNumberFont, Fonts.BOX_PRODUCT_NUMBER_FONT.getSize2D(), a)).max().getAsDouble();
        final PDFont priceFont = fontService.getPDFont(g2.getDocument(), Fonts.BOX_PRICE_FONT);
        final float[] descriptionEnds = Floats.toArray(
                articles.stream().
                map(Box.Article::getPrice).
                map(priceStr -> g2.getStringWidth(priceFont, Fonts.BOX_PRICE_FONT.getSize2D(), priceStr)).
                map(priceWidth -> boxTextEnd - priceWidth - TEXT_MARGIN / 2).
                collect(Collectors.toList()));
        final BoxPosition mainBoxPosition = new BoxPosition()
                .withTextStart(mainBoxTextStart)
                .withTextEnd(boxTextEnd)
                .withDescriptionStart(mainBoxTextStart + maxProductNumberWidth + TEXT_MARGIN)
                .withDescriptionEnd(descriptionEnds);
        final BoxPosition extendedBoxPosition = new BoxPosition()
                .withTextStart(extendedBoxTextStart)
                .withTextEnd(boxTextEnd)
                .withDescriptionStart(extendedBoxTextStart + maxProductNumberWidth + TEXT_MARGIN)
                .withDescriptionEnd(descriptionEnds);
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

    private static class BoxPosition {
        private float descriptionStart;
        private float[] descriptionEnd;
        private float textStart;
        private float textEnd;

        public BoxPosition withDescriptionStart(float descriptionStart) {
            this.descriptionStart = descriptionStart;
            return this;
        }

        public BoxPosition withDescriptionEnd(float[] descriptionEnd) {
            this.descriptionEnd = descriptionEnd;
            return this;
        }

        public BoxPosition withTextStart(float textStart) {
            this.textStart = textStart;
            return this;
        }

        public BoxPosition withTextEnd(float textEnd) {
            this.textEnd = textEnd;
            return this;
        }

        public float getDescriptionStart() {
            return descriptionStart;
        }

        public float getDescriptionEnd(int index) {
            return descriptionEnd[index];
        }

        public float getTextStart() {
            return textStart;
        }

        public float getTextEnd() {
            return textEnd;
        }
    }

    public static class RequiredOccupiedSpace {
        private final int boxSize;
        private final int indexOfNextArticle;

        public RequiredOccupiedSpace(int boxSize, int indexOfNextArticle) {
            this.boxSize = boxSize;
            this.indexOfNextArticle = indexOfNextArticle;
        }

        public int getBoxSize() {
            return boxSize;
        }

        public int getIndexOfNextArticle() {
            return indexOfNextArticle;
        }
    }

}
