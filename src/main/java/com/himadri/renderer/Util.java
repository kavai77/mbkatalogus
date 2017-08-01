package com.himadri.renderer;

import java.awt.*;

public class Util {
    public static int getStringWidth(Graphics2D g2, String string) {
        return g2.getFontMetrics(g2.getFont()).stringWidth(string);
    }
}
