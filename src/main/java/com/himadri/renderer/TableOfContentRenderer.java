package com.himadri.renderer;

import com.himadri.I18NService;
import com.himadri.graphics.pdfbox.PdfBoxPageGraphics;
import com.himadri.model.rendering.Document;
import com.himadri.model.rendering.TableOfContent;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Map;

@Component
public class TableOfContentRenderer {
    private static final int WIDTH = (int) Math.round(PDRectangle.A4.getWidth());
    private static final int HEIGHT = (int) Math.round(PDRectangle.A4.getHeight());
    private static final int MARGIN_X = 50;
    private static final int MARGIN_Y = 50;
    private static final int COLUMN_GAP = 10;
    private static final int BOX_HEIGHT = 20;
    private static final int PAGE_BOX_WIDTH = 30;
    private static final Font TITLE_FONT = new Font("Arial Black", Font.PLAIN, 18);
    private static final Font PAGE_FONT = new Font("Arial Black", Font.ITALIC, 10);
    private static final Font CONTENT_FONT = new Font("Arial", Font.PLAIN, 10);

    @Autowired
    private Util util;

    @Autowired
    private I18NService i18NService;

    public void renderTableOfContent(PdfBoxPageGraphics g2, TableOfContent tableOfContent) {
        g2.saveGraphicsState();
        float tx = MARGIN_X;
        float ty = MARGIN_Y;
        g2.transform(tx, ty);
        drawPageNbAndBaseLine(g2);
        int i = 0;
        for (Map.Entry<String, TableOfContent.TableOfContentItem> entry: tableOfContent.getTableOfContent().entrySet()) {
            TableOfContent.TableOfContentItem tableOfContentItem = entry.getValue();
            if (ty + BOX_HEIGHT > HEIGHT - MARGIN_Y) {
                g2.transform(-tx, -ty);
                tx = (WIDTH + COLUMN_GAP) / 2;
                ty = MARGIN_Y;
                g2.transform(tx, ty);
                drawPageNbAndBaseLine(g2);
            }
            ty += BOX_HEIGHT;
            g2.transform(0, BOX_HEIGHT);
            g2.setNonStrokingColor(tableOfContentItem.getColor());
            g2.fillRect(-0.25f, 0.25f - BOX_HEIGHT, PAGE_BOX_WIDTH,  BOX_HEIGHT - 0.5f);
            g2.setStrokingColor(Color.black);
            g2.drawLine(0, 0, (WIDTH - COLUMN_GAP) / 2f  - MARGIN_X, 0);
            g2.drawLine(PAGE_BOX_WIDTH, 0, PAGE_BOX_WIDTH, -BOX_HEIGHT);
            String pageString = tableOfContentItem.getPageNumber() + ".";
            g2.setNonStrokingColor(Color.black);
            g2.setFont(PAGE_FONT);
            g2.drawString(pageString, PAGE_BOX_WIDTH - 5 - g2.getStringWidth(pageString), -5);
            g2.setFont(CONTENT_FONT);
            g2.drawString(entry.getKey(), PAGE_BOX_WIDTH + 5, -5);
            i++;
        }
        g2.transform(-tx, -ty);
        g2.setFont(TITLE_FONT);
        String tableOfContentTitle = i18NService.getMessage("tableOfContentTitle");
        g2.drawString(tableOfContentTitle, MARGIN_X - 10, MARGIN_Y + g2.getStringWidth(tableOfContentTitle),- Math.PI / 2);
        g2.restoreGraphicsState();
    }

    private void drawPageNbAndBaseLine(PdfBoxPageGraphics g2) {
        g2.setStrokingColor(Color.black);
        g2.setLineWidth(.5f);
        g2.drawLine(0, 0, (WIDTH - COLUMN_GAP) / 2f  - MARGIN_X, 0);
        g2.setFont(PAGE_FONT);
        g2.drawString(i18NService.getMessage("tableOfContentPageNb"), 5, -5);
    }
}
