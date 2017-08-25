package com.himadri.renderer;

import com.himadri.model.rendering.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class Util {
    private static Logger LOG = LoggerFactory.getLogger(Util.class);

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

}
