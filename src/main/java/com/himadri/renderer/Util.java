package com.himadri.renderer;

import com.himadri.graphics.pdfbox.PDFontService;
import com.himadri.graphics.pdfbox.PdfBoxPageGraphics;
import com.himadri.model.rendering.Box;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;
import static org.apache.commons.lang3.StringUtils.splitByWholeSeparator;
import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;

@Component
public class Util {
    private static final int PRESS_PAGE_MARGIN = 20;
    private static final float PRESS_CUT_EDGE = 13;

    private static Logger LOG = LoggerFactory.getLogger(Util.class);

    public static final String FORCE_LINE_BREAK_CHARACTERS = ";;";

    @Autowired
    private PDFontService pdFontService;

    private static final Color[] PRODUCT_GROUP_COLORS = new Color[] {
            new Color(124, 171, 185),
            new Color(157, 200, 223),
            new Color(229, 198, 144),
            new Color(239, 156, 138),
            new Color(139, 177, 176),
            new Color(245, 148, 49),
            new Color(253, 236, 215)
    };

    public Color getBoxMainColor(Box box) {
        return getProductGroupMainColor(box.getIndexOfProductGroup());
    }

    public Color getProductGroupMainColor(int indexOfProductGroup) {
        return PRODUCT_GROUP_COLORS[indexOfProductGroup % PRODUCT_GROUP_COLORS.length];
    }

    public String[] splitGraphicsText(PdfBoxPageGraphics g2, Font font, String text, float... width) {
        List<String> lines = new ArrayList<>();
        String[] forcedLines = splitByWholeSeparator(text, FORCE_LINE_BREAK_CHARACTERS);
        PDFont pdFont = pdFontService.getPDFont(g2.getDocument(), font);
        for (String forcedLine: forcedLines) {
            String[] words = splitByWholeSeparator(forcedLine, null);
            if (words.length > 0) {
                StringBuilder line = new StringBuilder(words[0]);
                for (int wordSplit = 1; wordSplit < words.length; wordSplit++) {
                    String nextString = line.toString() + " " + words[wordSplit];
                    if (g2.getStringWidth(pdFont, font.getSize2D(), nextString) <= width[min(lines.size(), width.length - 1)]) {
                        line.append(" ").append(words[wordSplit]);
                    } else {
                        lines.add(line.toString());
                        line = new StringBuilder(words[wordSplit]);
                    }
                }
                lines.add(line.toString());
            }
        }
        return lines.toArray(new String[lines.size()]);
    }

    public static PDRectangle getStandardPageSize(boolean pressPageMode) {
        return pressPageMode ?
                new PDRectangle(A4.getWidth() + 2 * PRESS_PAGE_MARGIN, A4.getHeight() + 2 * PRESS_PAGE_MARGIN) :
                A4;
    }

    public static void pressTranslateAndDrawCuttingEdges(PdfBoxPageGraphics g2) {
        // translate
        g2.transform(PRESS_PAGE_MARGIN, PRESS_PAGE_MARGIN);

        g2.setStrokingColor(Color.BLACK);
        g2.setLineWidth(.5f);

        // left up corner
        g2.drawLineByWidth(-PRESS_PAGE_MARGIN, 0, PRESS_CUT_EDGE, 0);
        g2.drawLineByWidth(0, -PRESS_PAGE_MARGIN, 0, PRESS_CUT_EDGE);

        // left down corner
        g2.drawLineByWidth(-PRESS_PAGE_MARGIN, A4.getHeight(), PRESS_CUT_EDGE, 0);
        g2.drawLineByWidth(0, A4.getHeight() + PRESS_PAGE_MARGIN - PRESS_CUT_EDGE, 0, PRESS_CUT_EDGE);

        // right up corner
        g2.drawLineByWidth(A4.getWidth() + PRESS_PAGE_MARGIN - PRESS_CUT_EDGE, 0, PRESS_CUT_EDGE, 0);
        g2.drawLineByWidth(A4.getWidth(), -PRESS_PAGE_MARGIN, 0, PRESS_CUT_EDGE);

        // right down corner
        g2.drawLineByWidth(A4.getWidth() + PRESS_PAGE_MARGIN - PRESS_CUT_EDGE, A4.getHeight(), PRESS_CUT_EDGE, 0);
        g2.drawLineByWidth(A4.getWidth(), A4.getHeight() + PRESS_PAGE_MARGIN - PRESS_CUT_EDGE, 0, PRESS_CUT_EDGE);
    }
}
