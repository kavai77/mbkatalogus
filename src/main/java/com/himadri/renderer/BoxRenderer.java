package com.himadri.renderer;

import com.himadri.Settings;
import com.himadri.ValidationException;
import com.himadri.model.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class BoxRenderer {
    private static final Logger LOG = LoggerFactory.getLogger(BoxRenderer.class);

    private final float boxWidth;
    private final float boxHeight;
    private static final float IMAGE_WIDTH_MAX = 99f;
    private static final float IMAGE_HEIGHT_MAX = 99f;
    private static final float TEXT_BOX_X = 105f;
    private static final float TEXT_BOX_WIDTH = 150f;
    private static final float TEXT_BOX_HEAD_HEIGHT = 27f;
    private static final float TEXT_BOX_LINE_HEIGHT = 12f;
    private static final int TEXT_BOX_LINE_COUNT = 6;
    private static final String FONT = "Arial Narrow";

    public BoxRenderer(float boxWidth, float boxHeight) {
        this.boxWidth = boxWidth;
        this.boxHeight = boxHeight;
    }

    public void drawBox(Graphics2D g2, Box box) {
        // draw image
        final File imageFile = new File(IMAGE_LOCATION, box.getImage());
        if (!imageFile.exists()) {
            LOG.warn("Nem található a kép: " + box.getImage());
        } else if (!Settings.DISABLE_IMAGES) {
            AffineTransform transform = g2.getTransform();
            try (InputStream fis = new FileInputStream(imageFile)) {
                BufferedImage image = ImageIO.read(fis);
                float scale = Math.min(IMAGE_WIDTH_MAX / image.getWidth(), IMAGE_HEIGHT_MAX / image.getHeight());
                int posX = (int) (TEXT_BOX_X / scale - image.getWidth()) / 2;
                int posY = (int) (boxHeight / scale - image.getHeight()) / 2;
                g2.scale(scale, scale);
                g2.drawImage(image, posX, posY, image.getWidth(), image.getHeight(), null);
            } catch (IOException e) {
                LOG.error("Nem lehetett kirajzolni a képet: " + box.getImage());
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
                    LOG.error( "A cikknév egyetlen hosszú szóbál áll, amit nem lehetett tördelni");
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
                    LOG.warn("Sikerült kiírni a címsort, de nagyon közel áll a végéhez.");
                    throw new ValidationException();
                }
                StringBuilder secondLine = new StringBuilder(words[wordSplit]);
                for (int i = wordSplit + 1; i < words.length; i++) {
                    String nextString = secondLine.toString() + " " + words[i];
                    if (Util.getStringWidth(g2, nextString) > categoryStart - boxTextStart - 3) {
                        LOG.warn( "Túl hosszú a címsor, le kellett vágni a második sorban " + box);
                        break;
                    }
                    secondLine.append(" ").append(words[i]);
                }
                g2.drawString(secondLine.toString(), boxTextStart, 22);

            }
        } catch (ValidationException e) {
            LOG.info("Hibás doboz: " + box);
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
                g2.setFont(new Font(FONT, Font.PLAIN, 8));
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
                            LOG.error("Nem sikerült a tördelés, túl hosszú cikktörzs vagy túl sok egybe függő cikk");
                            throw new ValidationException();
                        }
                    }
                }
                if (sb != null) {
                    g2.drawString(sb.toString(), middleBoxStart, getLineYBaseLine(currentLine));
                }
                currentLine++;
                if (currentLine > TEXT_BOX_LINE_COUNT) {
                    LOG.error("Nem sikerült a tördelés, túl hosszú cikktörzs vagy túl sok egybe függő cikk");
                    throw new ValidationException();
                }
            }
        } catch (ValidationException e) {
            LOG.info("Hibás doboz: " + box);
        }

        // bottom line
        g2.setColor(Color.lightGray);
        g2.setStroke(new BasicStroke(.5f));
        g2.draw(new Line2D.Float(5, boxHeight, TEXT_BOX_X + TEXT_BOX_WIDTH, boxHeight));
    }

    private float getLineYBaseLine(int line) {
        return TEXT_BOX_HEAD_HEIGHT + (line + 1) * TEXT_BOX_LINE_HEIGHT - 3;
    }

}
