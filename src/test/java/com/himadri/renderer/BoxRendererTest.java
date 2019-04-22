package com.himadri.renderer;

import org.junit.Test;

import static com.himadri.renderer.BoxRenderer.HEAD_LINE_POS_MAP;
import static com.himadri.renderer.BoxRenderer.MAX_HEADLINE_LINES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BoxRendererTest {
    @Test
    public void testHeadLinePositions() {
        for (int i = 1; i <= MAX_HEADLINE_LINES; i++) {
            assertNotNull(HEAD_LINE_POS_MAP.get(i));
            assertEquals(i, HEAD_LINE_POS_MAP.get(i).length);
        }
    }
}