package com.himadri.renderer;

import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableMap;
import com.himadri.dto.UserRequest;
import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.Page;
import com.himadri.model.service.UserSession;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
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
    private static final String OLDAL = "oldal";

    static {
        assert MARGIN_LEFT.get(Page.Orientation.LEFT) + MARGIN_RIGHT.get(Page.Orientation.LEFT) ==
               MARGIN_LEFT.get(Page.Orientation.RIGHT) + MARGIN_RIGHT.get(Page.Orientation.RIGHT);
    }

    private static final String PAGE_FONT = "Arial";
    private static final String PAGE_FONT_BOLD = "Arial Black";

    @Autowired
    private BoxRenderer boxRenderer;

    @Autowired
    private Cache<String, UserSession> userSessionCache;

    @Autowired
    private Util util;

    public void drawPage(Graphics2D g2, Page page, UserRequest userRequest) {
        final UserSession errorCollector = userSessionCache.getIfPresent(userRequest.getRequestId());
        float marginLeft = MARGIN_LEFT.get(page.getOrientation());
        float marginRight = MARGIN_RIGHT.get(page.getOrientation());
        Color mainColor = util.getBoxMainColor(page.getBoxes().get(0));

        // draw middle line
        g2.setPaint(Color.lightGray);
        g2.setStroke(new BasicStroke(.5f));
        g2.draw(new Line2D.Float(marginLeft + BOX_WIDTH, MARGIN_TOP,
                marginLeft + BOX_WIDTH, HEIGHT - MARGIN_BOTTOM));

        // draw the frame
        if (page.getOrientation() == Page.Orientation.LEFT) {
            g2.draw(new Line2D.Float(marginLeft, MARGIN_TOP, WIDTH, MARGIN_TOP));
            g2.draw(new Line2D.Float(marginLeft, MARGIN_TOP, marginLeft, HEIGHT-MARGIN_BOTTOM));
            g2.draw(new Line2D.Float(marginLeft, HEIGHT-MARGIN_BOTTOM, WIDTH, HEIGHT-MARGIN_BOTTOM));
            g2.setPaint(mainColor);
            g2.fill(new Rectangle2D.Float(0, 0, 30, 700));
        } else {
            g2.draw(new Line2D.Float(0, MARGIN_TOP, WIDTH-marginRight, MARGIN_TOP));
            g2.draw(new Line2D.Float(WIDTH-marginRight, MARGIN_TOP, WIDTH-marginRight, HEIGHT-MARGIN_BOTTOM));
            g2.draw(new Line2D.Float(0, HEIGHT-MARGIN_BOTTOM, WIDTH-marginRight, HEIGHT-MARGIN_BOTTOM));
            g2.setPaint(mainColor);
            g2.fill(new Rectangle2D.Float(WIDTH - 30, 0, 30, 700));
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
        g2.setPaint(mainColor);
        g2.setFont(new Font(PAGE_FONT, Font.PLAIN, 15));
        final float headLineStartX = page.getOrientation() == Page.Orientation.LEFT ? marginLeft :
                WIDTH - marginRight - util.getStringWidth(g2, page.getHeadLine());
        g2.drawString(page.getHeadLine(), headLineStartX, MARGIN_TOP - 7);

        //drawing category
        g2.setPaint(Color.lightGray);
        final float categoryStartX = page.getOrientation() ==  Page.Orientation.LEFT ?
                WIDTH - marginRight - util.getStringWidth(g2, page.getCategory()) : marginLeft;
        g2.drawString(page.getCategory(), categoryStartX, MARGIN_TOP - 7);

        //drawing the boxes
        for (Box box: page.getBoxes()) {
            g2.setTransform(AffineTransform.getTranslateInstance(marginLeft + box.getColumn() * (BOX_WIDTH + 7.5f),
                    MARGIN_TOP + box.getRow() * BOX_HEIGHT));
            boxRenderer.drawBox(g2, box, userRequest);
        }

    }

    private void drawPageNumber(Graphics2D g2, String number, PositionWithAlignment pageStartPosX, PositionWithAlignment numberStartPosX) {
        g2.setPaint(Color.lightGray);
        g2.setFont(new Font(PAGE_FONT, Font.PLAIN, 12));
        g2.drawString(OLDAL, pageStartPosX.calculatePosX(g2, OLDAL),HEIGHT - MARGIN_BOTTOM + 3 + 12);
        g2.setPaint(Color.black);
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

        private float calculatePosX(Graphics2D g2,  String str) {
            return leftPos ? position : position - util.getStringWidth(g2, str);
        }
    }

}
