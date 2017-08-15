package com.himadri.renderer;

import com.himadri.model.rendering.TableOfContent;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import static com.himadri.renderer.Util.getProductGroupMainColor;
import static com.himadri.renderer.Util.getStringWidth;

@Component
public class TableOfContentRenderer {
    private static final int WIDTH = 595;
    private static final int HEIGHT = 842;
    private static final int MARGIN_X = 50;
    private static final int MARGIN_Y = 50;
    private static final int COLUMN_GAP = 10;
    private static final int BOX_HEIGHT = 20;
    private static final int PAGE_BOX_WIDTH = 30;
    private static final String TITLE = "TARTALOMJEGYZÉK";
    private static final Font TITLE_FONT = new Font("Arial Black", Font.PLAIN, 18);
    private static final Font PAGE_FONT = new Font("Arial Black", Font.ITALIC, 10);
    private static final Font CONTENT_FONT = new Font("Arial", Font.PLAIN, 10);


    public void renderTableOfContent(Graphics2D g2, TableOfContent tableOfContent) {
        g2.setPaint(Color.black);
        g2.setStroke(new BasicStroke(.5f));
        g2.translate(MARGIN_X, MARGIN_Y);
        drawPageNbAndBaseLine(g2);
        int i = 0;
        for (Map.Entry<String, Integer> entry: tableOfContent.getTableOfContent().entrySet()) {
            if (g2.getTransform().getTranslateY() + BOX_HEIGHT > HEIGHT - MARGIN_Y) {
                g2.setTransform(new AffineTransform());
                g2.translate((WIDTH + COLUMN_GAP) / 2, MARGIN_Y);
                drawPageNbAndBaseLine(g2);
            }
            g2.translate(0, BOX_HEIGHT);
            g2.setPaint(getProductGroupMainColor(i));
            g2.fill(new Rectangle2D.Float(-0.25f, 0.25f - BOX_HEIGHT, PAGE_BOX_WIDTH,  BOX_HEIGHT - 0.5f));
            g2.setPaint(Color.black);
            g2.draw(new Line2D.Float(0, 0, (WIDTH - COLUMN_GAP) / 2f  - MARGIN_X, 0));
            g2.draw(new Line2D.Float(PAGE_BOX_WIDTH, 0, PAGE_BOX_WIDTH, -BOX_HEIGHT));
            String pageString = entry.getValue() + ".";
            g2.setFont(PAGE_FONT);
            g2.drawString(pageString, PAGE_BOX_WIDTH - 5 - getStringWidth(g2, pageString), -5);
            g2.setFont(CONTENT_FONT);
            g2.drawString(entry.getKey(), PAGE_BOX_WIDTH + 5, -5);
            i++;
        }
        g2.setTransform(new AffineTransform());
        g2.setFont(TITLE_FONT);
        g2.translate(MARGIN_X - 10, MARGIN_Y + getStringWidth(g2, TITLE));
        g2.rotate(- Math.PI / 2);
        g2.drawString(TITLE, 0, 0);
    }

    private void drawPageNbAndBaseLine(Graphics2D g2) {
        g2.draw(new Line2D.Float(0, 0, (WIDTH - COLUMN_GAP) / 2f  - MARGIN_X, 0));
        g2.setFont(PAGE_FONT);
        g2.drawString("Oldalszám", 5, -5);
    }
}
