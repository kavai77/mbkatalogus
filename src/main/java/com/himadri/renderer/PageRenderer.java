package com.himadri.renderer;

import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableMap;
import com.himadri.dto.UserRequest;
import com.himadri.graphics.pdfbox.PdfBoxPageGraphics;
import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.Page;
import com.himadri.model.service.UserSession;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Map;

@Component
public class PageRenderer {
    private static final int WIDTH = (int) Math.round(PDRectangle.A4.getWidth());
    private static final int HEIGHT = (int) Math.round(PDRectangle.A4.getHeight());
    private static final int MARGIN_TOP = 30;
    private static final int MARGIN_BOTTOM = 20;
    public static final int BOX_ROWS_PER_PAGE = 8;
    public static final int BOX_COLUMNS_PER_PAGE = 2;

    private static final Map<Page.Orientation, Integer> MARGIN_LEFT = ImmutableMap.of(
            Page.Orientation.LEFT,40,
            Page.Orientation.RIGHT,30
    );

    private static final Map<Page.Orientation, Integer> MARGIN_RIGHT = ImmutableMap.of(
            Page.Orientation.LEFT,30,
            Page.Orientation.RIGHT,40
    );
    public static final float BOX_WIDTH = (WIDTH - MARGIN_LEFT.get(Page.Orientation.LEFT) - MARGIN_RIGHT.get(Page.Orientation.LEFT)) / 2; // 262.5
    public static final float BOX_HEIGHT = (HEIGHT - MARGIN_TOP - MARGIN_BOTTOM) / BOX_ROWS_PER_PAGE; //99f;

    static {
        assert MARGIN_LEFT.get(Page.Orientation.LEFT) + MARGIN_RIGHT.get(Page.Orientation.LEFT) ==
               MARGIN_LEFT.get(Page.Orientation.RIGHT) + MARGIN_RIGHT.get(Page.Orientation.RIGHT);
    }

    public static final String PAGE_FONT = "Arial";
    public static final String PAGE_FONT_BOLD = "Arial Black";

    @Autowired
    private BoxRenderer boxRenderer;

    @Autowired
    private Cache<String, UserSession> userSessionCache;

    @Autowired
    private Util util;

    @Value("${${pdfLang}.pageName}")
    private String pageName;

    public void drawPage(PdfBoxPageGraphics g2, Page page, UserRequest userRequest) {
        float marginLeft = MARGIN_LEFT.get(page.getOrientation());
        float marginRight = MARGIN_RIGHT.get(page.getOrientation());
        Color mainColor = util.getBoxMainColor(page.getBoxes().get(0));

        // draw middle line
        g2.setStrokingColor(Color.lightGray);
        g2.setLineWidth(.5f);
        g2.drawLine(marginLeft + BOX_WIDTH, MARGIN_TOP,marginLeft + BOX_WIDTH, HEIGHT - MARGIN_BOTTOM);

        // draw the frame
        if (page.getOrientation() == Page.Orientation.LEFT) {
            g2.drawLine(marginLeft, MARGIN_TOP, WIDTH, MARGIN_TOP);
            g2.drawLine(marginLeft, MARGIN_TOP, marginLeft, HEIGHT-MARGIN_BOTTOM);
            g2.drawLine(marginLeft, HEIGHT-MARGIN_BOTTOM, WIDTH, HEIGHT-MARGIN_BOTTOM);
            g2.setNonStrokingColor(mainColor);
            g2.fillRect(0, 0, 30, 700);
        } else {
            g2.drawLine(0, MARGIN_TOP, WIDTH-marginRight, MARGIN_TOP);
            g2.drawLine(WIDTH-marginRight, MARGIN_TOP, WIDTH-marginRight, HEIGHT-MARGIN_BOTTOM);
            g2.drawLine(0, HEIGHT-MARGIN_BOTTOM, WIDTH-marginRight, HEIGHT-MARGIN_BOTTOM);
            g2.setNonStrokingColor(mainColor);
            g2.fillRect(WIDTH - 30, 0, 30, 700);
        }

        // drawing page number
        if (page.getOrientation() == Page.Orientation.LEFT) {
            drawPageNumber(g2, Integer.toString(page.getPageNumber()), new PositionWithAlignment(marginLeft, true),
                    new PositionWithAlignment(marginLeft - 5, false));
        } else {
            drawPageNumber(g2, Integer.toString(page.getPageNumber()),
                    new PositionWithAlignment(WIDTH - marginRight, false),
                    new PositionWithAlignment(WIDTH - marginRight + 5, true));
        }

        // drawing headline
        g2.setNonStrokingColor(mainColor);
        g2.setFont(new Font(PAGE_FONT, Font.PLAIN, 15));
        final float headLineStartX = page.getOrientation() == Page.Orientation.LEFT ? marginLeft :
                WIDTH - marginRight - g2.getStringWidth(page.getHeadLine());
        g2.drawString(page.getHeadLine(), headLineStartX, MARGIN_TOP - 7);

        //drawing category
        g2.setNonStrokingColor(Color.lightGray);
        final float categoryStartX = page.getOrientation() ==  Page.Orientation.LEFT ?
                WIDTH - marginRight - g2.getStringWidth(page.getCategory()) : marginLeft;
        g2.drawString(page.getCategory(), categoryStartX, MARGIN_TOP - 7);

        //drawing the boxes
        for (Box box: page.getBoxes()) {
            final float tx = marginLeft + box.getColumn() * (BOX_WIDTH + 7.5f);
            final float ty = MARGIN_TOP + box.getRow() * BOX_HEIGHT;
            g2.transform(tx, ty);
            boxRenderer.drawBox(g2, box, userRequest);
            g2.transform(-tx, -ty);
        }

    }

    private void drawPageNumber(PdfBoxPageGraphics g2, String number, PositionWithAlignment pageStartPosX, PositionWithAlignment numberStartPosX) {
        g2.setNonStrokingColor(Color.lightGray);
        g2.setFont(new Font(PAGE_FONT, Font.PLAIN, 12));
        g2.drawString(pageName, pageStartPosX.calculatePosX(g2, pageName),HEIGHT - MARGIN_BOTTOM + 3 + 12);
        g2.setNonStrokingColor(Color.black);
        g2.setFont(new Font(PAGE_FONT_BOLD, Font.PLAIN, 12));
        g2.drawString(number, numberStartPosX.calculatePosX(g2, number), HEIGHT - MARGIN_BOTTOM + 3 + 12);
    }

    private class PositionWithAlignment {
        private final float position;
        private final boolean leftPos;

        public PositionWithAlignment(float position, boolean leftPos) {
            this.position = position;
            this.leftPos = leftPos;
        }

        private float calculatePosX(PdfBoxPageGraphics g2, String str) {
            return leftPos ? position : position - g2.getStringWidth(str);
        }
    }

}
