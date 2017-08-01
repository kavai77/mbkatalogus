package com.himadri.renderer;

import com.himadri.model.Box;

import java.awt.*;

public class Util {
    private static final Color[] PRODUCT_GROUP_COLORS = new Color[] {
            new Color(164, 203, 226),
            new Color(229, 198, 144)
    };

    public static int getStringWidth(Graphics2D g2, String string) {
        return g2.getFontMetrics(g2.getFont()).stringWidth(string);
    }

    public static Color getBoxMainColor(Box box) {
        return PRODUCT_GROUP_COLORS[box.getProductGroupNb() % PRODUCT_GROUP_COLORS.length];
    }
}
