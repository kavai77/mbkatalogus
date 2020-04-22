package com.himadri.renderer;

import com.google.common.collect.ImmutableSet;
import com.himadri.graphics.pdfbox.PDFontService;
import com.himadri.graphics.pdfbox.PdfBoxPageGraphics;
import com.himadri.model.rendering.Box;
import com.himadri.model.service.Paragraph;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;

@Component
public class Util {
    public static final Set<String> trueValueSet = ImmutableSet.of("i", "igen", "y", "yes", "t", "true", "1");
    public static final Set<String> lineBreaks = ImmutableSet.of(";;", "<p>", "<br>");
    public static final Pattern LINE_BREAK_PATTERN = Pattern.compile(String.join("|", lineBreaks));
    public static final Pattern HTML_TAG_PATTERN = Pattern.compile("</?[a-zA-Z]+>");
    public static final Pattern HTML_TAG_PATTERN_OR_WHITESPACE = Pattern.compile("</?[a-zA-Z]+>|\\s");
    private static final int PRESS_PAGE_MARGIN = 20;
    private static final float PRESS_CUT_EDGE = 13;

    private static Logger LOG = LoggerFactory.getLogger(Util.class);

    public static final String FORCE_LINE_BREAK_CHARACTERS = ";;";

    @Autowired
    PDFontService pdFontService;

    private static final Color[] PRODUCT_GROUP_COLORS = new Color[] {
            new Color(124, 171, 185),
            new Color(157, 200, 223),
            new Color(229, 198, 144),
            new Color(239, 156, 138),
            new Color(139, 177, 176),
            new Color(245, 148, 49),
            new Color(253, 236, 215)
    };

    public static void validateDirectory(String location, String locationDescription) {
        File locationFile = new File(location);
        if (!locationFile.exists() || !locationFile.isDirectory()) {
            throw new RuntimeException(String.format("The configured path for %s does not exist: %s",
                    locationDescription, location));
        }
    }

    public Color getBoxMainColor(Box box) {
        return getProductGroupMainColor(box.getIndexOfProductGroup());
    }

    public Color getProductGroupMainColor(int indexOfProductGroup) {
        return PRODUCT_GROUP_COLORS[indexOfProductGroup % PRODUCT_GROUP_COLORS.length];
    }

    public Paragraph splitMultiLineText(PdfBoxPageGraphics g2, Font font, String text, float... width) {
        Paragraph paragraph = new Paragraph(g2, pdFontService, font);
        String unescapedText = StringEscapeUtils.unescapeHtml4(text);
        String[] forcedLines = LINE_BREAK_PATTERN.split(unescapedText);
        for (String forcedLine: forcedLines) {
            String[] words = splitWithDelimiters(forcedLine, HTML_TAG_PATTERN_OR_WHITESPACE);
            for (String word: words) {
                if (HTML_TAG_PATTERN.matcher(word).matches()) {
                    if (PdfBoxPageGraphics.SUPPORTED_HTML_TAGS.contains(word)) {
                        switch (word) {
                            case "<b>":
                            case "<strong>":
                                paragraph.addToCurrentStyle(Font.BOLD);
                                break;
                            case "</b>":
                            case "</strong>":
                                paragraph.removeFromCurrentStyle(Font.BOLD);
                                break;
                            case "<i>":
                                paragraph.addToCurrentStyle(Font.ITALIC);
                                break;
                            case "</i>":
                                paragraph.removeFromCurrentStyle(Font.ITALIC);
                                break;
                            case "<u>":
                                paragraph.startUnderLine();
                                break;
                            case "</u>":
                                paragraph.addUnderline();
                                break;
                        }
                    }
                } else {
                    paragraph.addWord(word, width);
                }
            }
            paragraph.lineBreak();
        }
        return paragraph;
    }

    public static PDRectangle getStandardPageSize(boolean shouldDrawCuttingEdges) {
        return shouldDrawCuttingEdges ?
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

    public static String[] splitWithDelimiters(String str, Pattern p) {
        List<String> parts = new ArrayList<>();

        Matcher m = p.matcher(str);

        int lastEnd = 0;
        while(m.find()) {
            int start = m.start();
            if(lastEnd != start) {
                String nonDelim = str.substring(lastEnd, start);
                parts.add(nonDelim);
            }
            String delim = m.group();
            parts.add(delim);

            int end = m.end();
            lastEnd = end;
        }

        if(lastEnd != str.length()) {
            String nonDelim = str.substring(lastEnd);
            parts.add(nonDelim);
        }

        return parts.toArray(new String[]{});
    }

    public String removeLeadingHtmlBreaks(String str) {
        return StringUtils.removePattern(str, String.format("^(%s|\\s+)+", String.join("|", lineBreaks)));
    }
}
