package com.himadri.renderer;

import com.himadri.engine.PDFontService;
import com.himadri.model.rendering.Box;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;

@Component
public class Util {
    @Autowired
    private PDFontService pdFontService;

    private static final Color[] PRODUCT_GROUP_COLORS = new Color[] {
            new Color(176, 208, 228),
            new Color(237, 162, 143),
            new Color(142, 180, 193),
            new Color(254, 240, 223),
            new Color(149, 176, 153)
    };
    private PDDocument pdDocument = new PDDocument();

    public int getStringWidth(Graphics2D g2, String text) {
        try {
            return Math.round(pdFontService.getPDFont(pdDocument, g2.getFont()).getStringWidth(text) / 1000f * g2.getFont().getSize2D());
        } catch (IOException e) {
            return g2.getFontMetrics(g2.getFont()).stringWidth(text);
        }
    }

    public Color getBoxMainColor(Box box) {
        return getProductGroupMainColor(box.getIndexOfProductGroup());
    }

    public Color getProductGroupMainColor(int indexOfProductGroup) {
        return PRODUCT_GROUP_COLORS[indexOfProductGroup % PRODUCT_GROUP_COLORS.length];
    }
}
