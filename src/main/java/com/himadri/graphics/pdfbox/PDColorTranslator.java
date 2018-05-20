package com.himadri.graphics.pdfbox;

import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class PDColorTranslator {
    private static final Logger LOG = LoggerFactory.getLogger(PDColorTranslator.class);

    public static final float GRAY_THRESHOLD = 0.01f;

    public PDColor translatePDColor(Color color) {
        if (color == null) {
            return new PDColor(new float[]{0, 0, 0, 1f}, PDDeviceCMYK.INSTANCE);
        }

        final float[] rgb = color.getRGBColorComponents(null);
        final float max = Math.max(rgb[0], Math.max(rgb[1], rgb[2]));
        if (max == 0) {
            return new PDColor(new float[]{0, 0, 0, 1}, PDDeviceCMYK.INSTANCE);
        } else if (isGray(rgb)){
            return new PDColor(new float[]{
                    (max - rgb[0]) / max,
                    (max - rgb[1]) / max,
                    (max - rgb[2]) / max,
                    1 - max}, PDDeviceCMYK.INSTANCE);
        } else {
            return new PDColor(rgb, PDDeviceRGB.INSTANCE);
        }
    }

    private boolean isGray(float[] rgb) {
        final float avg = (rgb[0] + rgb[1] + rgb[2]) / 3;
        return Math.abs(avg - rgb[0]) < GRAY_THRESHOLD &&
               Math.abs(avg - rgb[1]) < GRAY_THRESHOLD &&
               Math.abs(avg - rgb[2]) < GRAY_THRESHOLD;
    }
}
