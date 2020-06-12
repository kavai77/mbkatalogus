package com.himadri.renderer;

import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableMap;
import com.himadri.I18NService;
import com.himadri.dto.UserRequest;
import com.himadri.graphics.pdfbox.PdfBoxPageGraphics;
import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.Page;
import com.himadri.model.service.UserSession;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Map;

@Component
public class PageRenderer {
    private static final int WIDTH = Math.round(PDRectangle.A4.getWidth());
    private static final int HEIGHT = Math.round(PDRectangle.A4.getHeight());
    private static final int MARGIN_TOP = 30;
    private static final int MARGIN_BOTTOM = 20;
    public static final int BOX_ROWS_PER_PAGE = 8;
    public static final int BOX_COLUMNS_PER_PAGE = 2;

    static final Map<Page.Orientation, Integer> MARGIN_LEFT = ImmutableMap.of(
            Page.Orientation.LEFT,40,
            Page.Orientation.RIGHT,30
    );

    static final Map<Page.Orientation, Integer> MARGIN_RIGHT = ImmutableMap.of(
            Page.Orientation.LEFT,30,
            Page.Orientation.RIGHT,40
    );
    public static final float BOX_WIDTH = (WIDTH - MARGIN_LEFT.get(Page.Orientation.LEFT) - MARGIN_RIGHT.get(Page.Orientation.LEFT)) / (float) BOX_COLUMNS_PER_PAGE; // 262.5
    public static final float BOX_HEIGHT = (HEIGHT - MARGIN_TOP - MARGIN_BOTTOM) / (float) BOX_ROWS_PER_PAGE; //99f;

    @Autowired
    private BoxRenderer boxRenderer;

    @Autowired
    private Cache<String, UserSession> userSessionCache;

    @Autowired
    private Util util;

    @Autowired
    private I18NService i18NService;

    public void drawPage(PdfBoxPageGraphics g2, Page page, UserRequest userRequest) {
        float marginLeft = MARGIN_LEFT.get(page.getOrientation());
        float marginRight = MARGIN_RIGHT.get(page.getOrientation());
        //Color mainColor = util.getBoxMainColor(page.getBoxes().get(0));
        Color mainColor = page.getBoxes()
                .stream()
                .filter(b -> b.getBoxType() == Box.Type.ARTICLE)
                .map(b -> util.getBoxMainColor(b))
                .findFirst()
                .orElse(util.getProductGroupMainColor(0));

        int wideHeaderBoxHeight = page.getBoxes()
            .stream()
            .filter(b -> b.getBoxType() == Box.Type.IMAGE
                && b.getWidth() == BOX_COLUMNS_PER_PAGE
                && b.getRow() == 0)
            .mapToInt(Box::getHeight)
            .findFirst()
            .orElse(0);
        int wideFooterBoxHeight = page.getBoxes()
            .stream()
            .filter(b -> b.getBoxType() == Box.Type.IMAGE
                && b.getWidth() == BOX_COLUMNS_PER_PAGE
                && b.getRow() > 0)
            .mapToInt(Box::getHeight)
            .findFirst()
            .orElse(0);

        float frameTop = MARGIN_TOP + wideHeaderBoxHeight * BOX_HEIGHT;
        float frameBottom = HEIGHT - MARGIN_BOTTOM - wideFooterBoxHeight * BOX_HEIGHT;

        // draw middle line
        g2.setStrokingColor(Color.lightGray);
        g2.setLineWidth(.5f);
        g2.drawLine(marginLeft + BOX_WIDTH, frameTop,marginLeft + BOX_WIDTH, frameBottom);

        // draw the frame
        int edgeOverFlow = userRequest.getQuality().isDrawCuttingEdges() ? 10 : 0;
        if (page.getOrientation() == Page.Orientation.LEFT) {
            g2.drawLine(marginLeft, frameTop, WIDTH + edgeOverFlow, frameTop);
            g2.drawLine(marginLeft, frameTop, marginLeft, frameBottom);
            g2.drawLine(marginLeft, frameBottom, WIDTH + edgeOverFlow, frameBottom);
            g2.setNonStrokingColor(mainColor);
            g2.fillRect(-edgeOverFlow, -edgeOverFlow, 30 + edgeOverFlow, 700 + edgeOverFlow);
        } else {
            g2.drawLine(-edgeOverFlow, frameTop, WIDTH - marginRight, frameTop);
            g2.drawLine(WIDTH - marginRight, frameTop, WIDTH - marginRight, frameBottom);
            g2.drawLine(-edgeOverFlow, frameBottom, WIDTH - marginRight, frameBottom);
            g2.setNonStrokingColor(mainColor);
            g2.fillRect(WIDTH - 30, -edgeOverFlow, 30 + edgeOverFlow, 700 + edgeOverFlow);
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
        g2.setFont(Fonts.PAGE_CATALOGUE_TITLE_FONT);
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
            final float tx = marginLeft + box.getColumn() * BOX_WIDTH;
            final float ty = MARGIN_TOP + box.getRow() * BOX_HEIGHT;
            g2.transform(tx, ty);
            boxRenderer.drawBox(g2, box, userRequest);
            g2.transform(-tx, -ty);
        }

    }

    private void drawPageNumber(PdfBoxPageGraphics g2, String number, PositionWithAlignment pageStartPosX, PositionWithAlignment numberStartPosX) {
        g2.setNonStrokingColor(Color.lightGray);
        g2.setFont(Fonts.PAGE_NB_OLDAL_FONT);
        String pageName = i18NService.getMessage("pageName");
        g2.drawString(pageName, pageStartPosX.calculatePosX(g2, pageName),HEIGHT - MARGIN_BOTTOM + 12);
        g2.setNonStrokingColor(Color.black);
        g2.setFont(Fonts.PAGE_NB_FONT);
        g2.drawString(number, numberStartPosX.calculatePosX(g2, number), HEIGHT - MARGIN_BOTTOM + 12);
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
