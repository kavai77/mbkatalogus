package com.himadri.renderer;

import com.himadri.model.rendering.Page;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PageRendererTest {
    @Test
    public void testMargins() {
        assertEquals(
            PageRenderer.MARGIN_LEFT.get(Page.Orientation.LEFT) + PageRenderer.MARGIN_RIGHT.get(Page.Orientation.LEFT),
            PageRenderer.MARGIN_LEFT.get(Page.Orientation.RIGHT) + PageRenderer.MARGIN_RIGHT.get(Page.Orientation.RIGHT));
    }
}