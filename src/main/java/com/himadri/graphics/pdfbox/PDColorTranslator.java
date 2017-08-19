package com.himadri.graphics.pdfbox;

import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class PDColorTranslator {
    public PDColor translatePDColor(Color color) {
        if (color == null)
            return new PDColor(new float[] { 0, 0, 0, 1f }, PDDeviceCMYK.INSTANCE);

        float[] c = color.getRGBColorComponents(null);
        float k = 1 - Math.max(c[0], Math.max(c[1], c[2]));
        if (k == 1) {
            return new PDColor(new float[]{0, 0, 0, 1}, PDDeviceCMYK.INSTANCE);
        } else {
            return new PDColor(new float[]{
                    (1 - c[0] - k) / (1 - k),
                    (1 - c[1] - k) / (1 - k),
                    (1 - c[2] - k) / (1 - k),
                    k}, PDDeviceCMYK.INSTANCE);
        }
    }
}
