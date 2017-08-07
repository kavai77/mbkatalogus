package com.himadri.renderer;

import com.google.common.cache.Cache;
import com.himadri.ValidationException;
import com.himadri.model.Box;
import com.himadri.model.ErrorCollector;
import com.himadri.model.UserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.himadri.Settings.IMAGE_LOCATION;
import static com.himadri.Settings.LOGO_IMAGE_LOCATION;
import static com.himadri.model.ErrorCollector.Severity.*;
import static com.himadri.renderer.PageRenderer.BOX_HEIGHT;
import static org.apache.commons.lang.StringUtils.stripToEmpty;

@Component
public class BoxRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoxRenderer.class);

    private static final float IMAGE_WIDTH_MAX = 99f;
    private static final float IMAGE_HEIGHT_MAX = 99f;
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
    private Cache<String, ErrorCollector> userSessionCache;

    public void drawBox(Graphics2D g2, Box box, UserRequest userRequest) {
        final ErrorCollector errorCollector = userSessionCache.getIfPresent(userRequest.getRequestId());

        // draw image
        final File imageFile = new File(IMAGE_LOCATION, stripToEmpty(box.getImage()));
        if (!imageFile.exists() || !imageFile.isFile()) {
            errorCollector.addErrorItem(WARN, "Nem található a kép: " + box.getImage());
        } else if (userRequest.isEnableImages()) {
            AffineTransform transform = g2.getTransform();
            try (InputStream fis = new FileInputStream(imageFile)) {
                BufferedImage image = ImageIO.read(fis);
                float scale = Math.min(IMAGE_WIDTH_MAX / image.getWidth(), IMAGE_HEIGHT_MAX / image.getHeight());
                int posX = (int) (TEXT_BOX_X / scale - image.getWidth()) / 2;
                int posY = (int) (BOX_HEIGHT / scale - image.getHeight()) / 2;
                g2.scale(scale, scale);
                g2.drawImage(image, posX, posY, image.getWidth(), image.getHeight(), null);
            } catch (IOException e) {
                errorCollector.addErrorItem(ERROR, String.format("Nem lehetett kirajzolni a képet: %s. Hibaüzenet: %s",
                        box.getImage(), e.getMessage()));
                LOGGER.error("Could not paint image {}", box, e);
            } finally {
                g2.setTransform(transform);
            }
        }

        // draw the logo
        final File logoImageFile = new File(LOGO_IMAGE_LOCATION, stripToEmpty(box.getBrandImage()));
        if (!logoImageFile.exists() || !logoImageFile.isFile()) {
            errorCollector.addErrorItem(WARN, "Nem található a logo file kép: " + box.getBrandImage());
        } else if (userRequest.isEnableImages()) {
            AffineTransform transform = g2.getTransform();
            try {
                BufferedImage logoImage = logoImageCache.getLogoImage(logoImageFile);
                float scale = Math.min(1f, LOGO_IMAGE_WIDTH_MAX / logoImage.getWidth());
                g2.scale(scale, scale);
                g2.drawImage(logoImage, (int)(3f / scale), (int)(3f / scale), logoImage.getWidth(), logoImage.getHeight(), null);
            } catch (IOException e) {
                errorCollector.addErrorItem(ERROR, String.format("Nem lehetett kirajzolni a logo képet: %s. Hibaüzenet: %s",
                        box.getImage(), e.getMessage()));
                LOGGER.error("Could not paint logo image", e);
            } finally {
                g2.setTransform(transform);
            }
        }

        // headline box
        final Color mainColor = Util.getBoxMainColor(box);
        g2.setPaint(new LinearGradientPaint(TEXT_BOX_X, TEXT_BOX_HEAD_HEIGHT, TEXT_BOX_X + TEXT_BOX_WIDTH, 0,
                new float[]{0.0f, 0.5f, 1f}, new Color[]{mainColor, Color.white, mainColor}));
        g2.fill(new Rectangle2D.Float(TEXT_BOX_X, 0, TEXT_BOX_WIDTH, TEXT_BOX_HEAD_HEIGHT));

        // text boxes
        Color grayBackground = new Color(224, 224, 244);
        for (int i = 0; i < TEXT_BOX_LINE_COUNT; i++) {
            g2.setPaint(i % 2 == 1 ? Color.white : grayBackground);
            g2.fill(new Rectangle2D.Float(TEXT_BOX_X, TEXT_BOX_HEAD_HEIGHT + i * TEXT_BOX_LINE_HEIGHT, TEXT_BOX_WIDTH,
                    TEXT_BOX_LINE_HEIGHT));
        }

        float boxTextStart = TEXT_BOX_X + 3;
        float boxTextEnd = TEXT_BOX_X + TEXT_BOX_WIDTH - 3;

        // category
        g2.setPaint(new Color(38, 66, 140));
        g2.setFont(new Font(FONT, Font.PLAIN, 8));
        float categoryStart = boxTextEnd - Util.getStringWidth(g2, box.getCategory());
        g2.drawString(box.getCategory(), categoryStart, 22);

        // heading text
        try {
            g2.setPaint(Color.black);
            g2.setFont(new Font(FONT, Font.BOLD, 9));
            if (Util.getStringWidth(g2, box.getTitle()) <= boxTextEnd - boxTextStart) {
                g2.drawString(box.getTitle(), boxTextStart, 13);
            } else {
                String[] words = box.getTitle().split(" ");
                if (words.length == 0) {
                    errorCollector.addErrorItem(ERROR, "A cikknév egyetlen hosszú szóbál áll, amit nem lehetett tördelni");
                    throw new ValidationException();
                }
                StringBuilder firstLine = new StringBuilder(words[0]);
                int wordSplit;
                for (wordSplit = 1; wordSplit < words.length; wordSplit++) {
                    String nextString = firstLine.toString() + " " + words[wordSplit];
                    if (Util.getStringWidth(g2, nextString) <= boxTextEnd - boxTextStart) {
                        firstLine.append(" ").append(words[wordSplit]);
                    } else {
                        g2.drawString(firstLine.toString(), boxTextStart, 10);
                        break;
                    }
                }
                if (wordSplit == words.length) {
                    g2.drawString(firstLine.toString(), boxTextStart, 13);
                    errorCollector.addErrorItem(WARN, "Sikerült kiírni a címsort, de nagyon közel áll a végéhez.");
                    throw new ValidationException();
                }
                StringBuilder secondLine = new StringBuilder(words[wordSplit]);
                for (int i = wordSplit + 1; i < words.length; i++) {
                    String nextString = secondLine.toString() + " " + words[i];
                    if (Util.getStringWidth(g2, nextString) > categoryStart - boxTextStart - 3) {
                        errorCollector.addErrorItem(WARN, "Túl hosszú a címsor, le kellett vágni a második sorban " + box);
                        break;
                    }
                    secondLine.append(" ").append(words[i]);
                }
                g2.drawString(secondLine.toString(), boxTextStart, 22);

            }
        } catch (ValidationException e) {
            errorCollector.addErrorItem(INFO, "Hibás doboz: " + box);
        }

        try {
            int currentLine = 0;
            for (Box.Article article : box.getArticles()) {
                // item number
                g2.setPaint(Color.black);
                g2.setFont(new Font(FONT, Font.BOLD, 8));
                g2.drawString(article.getNumber(), boxTextStart, getLineYBaseLine(currentLine));
                float middleBoxStart = boxTextStart + Util.getStringWidth(g2, article.getNumber()) + 3;

                // price
                g2.setPaint(Color.black);
                g2.setFont(new Font(FONT, Font.BOLD, 8));
                int priceStringWidth = Util.getStringWidth(g2, article.getPrice());
                g2.drawString(article.getPrice(), boxTextEnd - priceStringWidth, getLineYBaseLine(currentLine));
                float middleBoxEnd = boxTextEnd - priceStringWidth - 3;

                // description
                g2.setPaint(Color.black);
                g2.setFont(new Font(FONT, Font.PLAIN, 7));
                StringBuilder sb = null;

                for (String word : article.getDescription().split(" ")) {
                    if (sb == null) {
                        sb = new StringBuilder(word);
                    } else if (Util.getStringWidth(g2, sb.toString() + " " + word) < middleBoxEnd - middleBoxStart) {
                        sb.append(" ").append(word);
                    } else {
                        g2.drawString(sb.toString(), middleBoxStart, getLineYBaseLine(currentLine));
                        sb = new StringBuilder(word);
                        currentLine++;
                        if (currentLine > TEXT_BOX_LINE_COUNT) {
                            errorCollector.addErrorItem(ERROR, "Nem sikerült a tördelés, túl hosszú cikktörzs vagy túl sok egybe függő cikk");
                            throw new ValidationException();
                        }
                    }
                }
                if (sb != null) {
                    g2.drawString(sb.toString(), middleBoxStart, getLineYBaseLine(currentLine));
                }
                currentLine++;
                if (currentLine > TEXT_BOX_LINE_COUNT) {
                    errorCollector.addErrorItem(ERROR, "Nem sikerült a tördelés, túl hosszú cikktörzs vagy túl sok egybe függő cikk");
                    throw new ValidationException();
                }
            }
        } catch (ValidationException e) {
            errorCollector.addErrorItem(INFO, "Hibás doboz: " + box);
        }

        // bottom line
        g2.setColor(Color.lightGray);
        g2.setStroke(new BasicStroke(.5f));
        g2.draw(new Line2D.Float(5, BOX_HEIGHT, TEXT_BOX_X + TEXT_BOX_WIDTH, BOX_HEIGHT));
    }

    private float getLineYBaseLine(int line) {
        return TEXT_BOX_HEAD_HEIGHT + (line + 1) * TEXT_BOX_LINE_HEIGHT - 3;
    }

}
