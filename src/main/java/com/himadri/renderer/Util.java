package com.himadri.renderer;

import com.himadri.model.rendering.Box;

import java.awt.*;

public class Util {
    private static final Color[] PRODUCT_GROUP_COLORS = new Color[] {
            new Color(176, 208, 228),
            new Color(237, 162, 143),
            new Color(142, 180, 193),
            new Color(254, 240, 223),
            new Color(149, 176, 153)
    };

    public static int getStringWidth(Graphics2D g2, String string) {
        return g2.getFontMetrics(g2.getFont()).stringWidth(string);
    }

    public static Color getBoxMainColor(Box box) {
        return PRODUCT_GROUP_COLORS[box.getIndexOfProductGroup() % PRODUCT_GROUP_COLORS.length];
    }
}
