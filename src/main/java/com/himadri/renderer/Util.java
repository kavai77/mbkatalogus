package com.himadri.renderer;

import com.himadri.graphics.pdfbox.PDFontService;
import com.himadri.graphics.pdfbox.PdfBoxGraphics;
import com.himadri.model.rendering.Box;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;

@Component
public class Util {
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

    public int getStringWidth(PdfBoxGraphics g2, String text) {
        try {
            return Math.round(g2.getFont().getStringWidth(text) / 1000f * g2.getFontSize());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getStringWidth(Font font, String text) {
        try {
            final PDDocument pdDocument = new PDDocument();
            final PDFont pdFont = pdFontService.getPDFont(pdDocument, font);
            return Math.round(pdFont.getStringWidth(text) / 1000f * font.getSize2D());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Color getBoxMainColor(Box box) {
        return getProductGroupMainColor(box.getIndexOfProductGroup());
    }

    public Color getProductGroupMainColor(int indexOfProductGroup) {
        return PRODUCT_GROUP_COLORS[indexOfProductGroup % PRODUCT_GROUP_COLORS.length];
    }
}
