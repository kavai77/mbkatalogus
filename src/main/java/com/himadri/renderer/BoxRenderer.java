package com.himadri.renderer;

import com.google.common.cache.Cache;
import com.himadri.dto.UserRequest;
import com.himadri.exception.ValidationException;
import com.himadri.graphics.pdfbox.PdfBoxGraphics;
import com.himadri.model.rendering.Box;
import com.himadri.model.service.UserSession;
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
import java.util.List;

import static com.himadri.dto.ErrorItem.ErrorCategory.FORMATTING;
import static com.himadri.dto.ErrorItem.ErrorCategory.IMAGE;
import static com.himadri.dto.ErrorItem.Severity.ERROR;
import static com.himadri.dto.ErrorItem.Severity.WARN;
import static com.himadri.renderer.PageRenderer.BOX_HEIGHT;
import static org.apache.commons.lang3.StringUtils.*;

@Component
public class BoxRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoxRenderer.class);

    private static final float IMAGE_WIDTH_MAX = 95f;
    private static final float IMAGE_HEIGHT_MAX = 89f;
    private static final float LOGO_IMAGE_WIDTH_MAX = 33f;
    private static final float TEXT_BOX_X = 105f;
    private static final float TEXT_BOX_WIDTH = 150f;
    private static final float TEXT_BOX_HEAD_HEIGHT = 27f;
    private static final float TEXT_BOX_LINE_HEIGHT = 12f;
    private static final int TEXT_BOX_LINE_COUNT = 6;
    private static final String FONT = "Arial Narrow";

    @Autowired
    private LogoImageCache logoImageCache;

    @Autowired
    private Cache<String, UserSession> userSessionCache;
    
    @Autowired
    private Util util;

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

    public void drawBox(PdfBoxGraphics g2, Box box, UserRequest userRequest) {
        final UserSession userSession = userSessionCache.getIfPresent(userRequest.getRequestId());

        // draw image
        final File imageFile = new File(imageLocation, stripToEmpty(box.getImage()));
        if (!imageFile.exists() || !imageFile.isFile()) {
            userSession.addErrorItem(WARN, IMAGE, "Nem található a kép: " + box.getImage());
        } else if (!userRequest.isDraftMode()) {
            try (InputStream fis = new FileInputStream(imageFile)) {
                BufferedImage image = ImageIO.read(fis);
                float scale = Math.min(IMAGE_WIDTH_MAX / image.getWidth(), IMAGE_HEIGHT_MAX / image.getHeight());
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
                float scale = Math.min(1f, LOGO_IMAGE_WIDTH_MAX / logoImage.getWidth());
                g2.drawImage(logoImage, 3f, 3f, logoImage.getWidth() * scale, logoImage.getHeight() * scale);
            } catch (IOException e) {
                userSession.addErrorItem(ERROR, IMAGE, String.format("Nem lehetett kirajzolni a logo képet: %s. Hibaüzenet: %s",
                        box.getImage(), e.getMessage()));
                LOGGER.error("Could not paint logo image", e);
            }
        }

        // headline box
        final Color mainColor = util.getBoxMainColor(box);
        g2.setNonStrokingColor(mainColor);
//        g2.setPaint(new LinearGradientPaint(TEXT_BOX_X, TEXT_BOX_HEAD_HEIGHT, TEXT_BOX_X + TEXT_BOX_WIDTH, 0,
//                new float[]{0.0f, 0.5f, 1f}, new Color[]{mainColor, Color.white, mainColor}));
        g2.fillRect(TEXT_BOX_X, 0, TEXT_BOX_WIDTH, TEXT_BOX_HEAD_HEIGHT);

        // text boxes
        Color grayBackground = new Color(224, 224, 244);
        for (int i = 0; i < TEXT_BOX_LINE_COUNT; i++) {
            g2.setNonStrokingColor(i % 2 == 1 ? Color.white : grayBackground);
            g2.fillRect(TEXT_BOX_X, TEXT_BOX_HEAD_HEIGHT + i * TEXT_BOX_LINE_HEIGHT, TEXT_BOX_WIDTH,
                    TEXT_BOX_LINE_HEIGHT);
        }

        float boxTextStart = TEXT_BOX_X + 3;
        float boxTextEnd = TEXT_BOX_X + TEXT_BOX_WIDTH - 3;

        // category
        g2.setNonStrokingColor(new Color(38, 66, 140));
        g2.setFont(new Font(FONT, Font.PLAIN, 8));
        float categoryStart = boxTextEnd - g2.getStringWidth(box.getCategory());
        g2.drawString(box.getCategory(), categoryStart, 22);

        // heading text
        try {
            g2.setNonStrokingColor(Color.black);
            g2.setFont(new Font(FONT, Font.BOLD, 9));
            if (g2.getStringWidth(box.getTitle()) <= boxTextEnd - boxTextStart) {
                g2.drawString(box.getTitle(), boxTextStart, 13);
            } else {
                String[] words = splitByWholeSeparator(box.getTitle(),null);
                if (words.length == 1) {
                    throw new ValidationException(ERROR, FORMATTING, String.format(
                            "A cikknév egyetlen hosszú szóbál áll, amit nem lehetett tördelni. Cikkszám: %s Cikknév %s ",
                            box.getArticles().get(0).getNumber(), box.getTitle()));
                }
                StringBuilder firstLine = new StringBuilder(words[0]);
                int wordSplit;
                for (wordSplit = 1; wordSplit < words.length; wordSplit++) {
                    String nextString = firstLine.toString() + " " + words[wordSplit];
                    if (g2.getStringWidth(nextString) <= boxTextEnd - boxTextStart) {
                        firstLine.append(" ").append(words[wordSplit]);
                    } else {
                        g2.drawString(firstLine.toString(), boxTextStart, 10);
                        break;
                    }
                }
                if (wordSplit == words.length) {
                    g2.drawString(firstLine.toString(), boxTextStart, 13);
                    throw new ValidationException(WARN, FORMATTING, "Sikerült kiírni a címsort, de nagyon közel áll a végéhez. " +
                            box.getArticles().get(0).getNumber());
                }
                StringBuilder secondLine = new StringBuilder(words[wordSplit]);
                for (int i = wordSplit + 1; i < words.length; i++) {
                    String nextString = secondLine.toString() + " " + words[i];
                    if (g2.getStringWidth(nextString) > categoryStart - boxTextStart - 3) {
                        final Box.Article firstArticle = box.getArticles().get(0);
                        if (box.getArticles().size() == 1 && firstArticle.isEmptyItemText()) {
                            String newItemText = join(words, ' ', i, words.length);
                            box.getArticles().set(0, new Box.Article(firstArticle.getNumber(), firstArticle.getPrice(),
                                    firstArticle.getDescription() + newItemText, false));
                            userSession.addErrorItem(WARN, FORMATTING, String.format("Nincs pontosvesszős elválasztás a cikknévben, " +
                                    "így autmatikus tördelést alkalmaztunk a címsor és a leírás között. " +
                                    "Cikkszám: %s. Címsor: %s. Leírás: %s", box.getArticles().get(0).getNumber(),
                                    join(firstLine, " ", secondLine), newItemText));
                        } else {
                            userSession.addErrorItem(ERROR, FORMATTING, String.format("Túl hosszú a címsor, le kellett vágni a második sorban. " +
                                    "Cikkszám: %s. Teljes címsor: %s", box.getArticles().get(0).getNumber(), box.getTitle()));
                        }
                        break;
                    }
                    secondLine.append(" ").append(words[i]);
                }
                g2.drawString(secondLine.toString(), boxTextStart, 22);

            }
        } catch (ValidationException e) {
            userSession.addErrorItem(e);
        }

        try {
            int currentLine = 0;
            List<Box.Article> articles = box.getArticles();
            for (int i = 0; i < articles.size(); i++) {
                Box.Article article = articles.get(i);
                if (currentLine >= TEXT_BOX_LINE_COUNT) {
                    throw new ValidationException(ERROR, FORMATTING, String.format("Nem sikerült a tördelés, túl sok egybe függő cikk. " +
                            "Megjelenített cikkek: %s. Levágott cikkek: %s",
                            join(articles.stream().limit(i).map(Box.Article::getNumber).toArray(), ' '),
                            join(articles.stream().skip(i).map(Box.Article::getNumber).toArray(), ' ')));
                }
                // product number
                g2.setNonStrokingColor(Color.black);
                g2.setFont(new Font(FONT, Font.BOLD, 8));
                g2.drawString(article.getNumber(), boxTextStart, getLineYBaseLine(currentLine));
                float middleBoxStart = boxTextStart + g2.getStringWidth(article.getNumber()) + 3;

                // price
                g2.setNonStrokingColor(Color.black);
                g2.setFont(new Font(FONT, Font.BOLD, 8));
                int priceStringWidth = g2.getStringWidth(article.getPrice());
                g2.drawString(article.getPrice(), boxTextEnd - priceStringWidth, getLineYBaseLine(currentLine));
                float middleBoxEnd = boxTextEnd - priceStringWidth - 3;

                // description
                g2.setNonStrokingColor(Color.black);
                g2.setFont(new Font(FONT, Font.PLAIN, 7));
                StringBuilder sb = null;

                final String[] words = splitByWholeSeparator(article.getDescription(), null);
                for (int j = 0; j < words.length; j++) {
                    String word = words[j];
                    if (sb == null) {
                        sb = new StringBuilder(word);
                    } else if (g2.getStringWidth(sb.toString() + " " + word) < middleBoxEnd - middleBoxStart) {
                        sb.append(" ").append(word);
                    } else {
                        g2.drawString(sb.toString(), middleBoxStart, getLineYBaseLine(currentLine));
                        sb = new StringBuilder(word);
                        currentLine++;
                        if (currentLine >= TEXT_BOX_LINE_COUNT) {
                            if (i + 1 < articles.size()) {
                                throw new ValidationException(ERROR, FORMATTING, String.format(
                                        "Nem sikerült a tördelés, túl hosszú cikknév és túl sok egybe függő cikk. " +
                                                "Cikkszám: %s. Levágott tartalom: \"%s\". Ezen kívül még a nem megjelenített cikkek: %s",
                                        article.getNumber(), join(words, ' ', j, words.length),
                                        join(articles.stream().skip(i + 1).map(Box.Article::getNumber).toArray(), ' ')));
                            } else {
                                throw new ValidationException(ERROR, FORMATTING, String.format(
                                        "Nem sikerült a tördelés, túl hosszú cikknév. Cikkszám: %s. Levágott tartalom: \"%s\"",
                                        article.getNumber(), join(words, ' ', j, words.length)));
                            }
                        }
                    }
                }
                if (sb != null) {
                    g2.drawString(sb.toString(), middleBoxStart, getLineYBaseLine(currentLine));
                }
                currentLine++;
            }
        } catch (ValidationException e) {
            userSession.addErrorItem(e);
        }

        // bottom line
        g2.setStrokingColor(Color.lightGray);
        g2.setLineWidth(.5f);
        g2.drawLine(5, BOX_HEIGHT, TEXT_BOX_X + TEXT_BOX_WIDTH, BOX_HEIGHT);
    }

    private float getLineYBaseLine(int line) {
        return TEXT_BOX_HEAD_HEIGHT + (line + 1) * TEXT_BOX_LINE_HEIGHT - 3;
    }

}
