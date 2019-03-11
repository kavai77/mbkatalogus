package com.himadri.graphics.pdfbox;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class PdfBoxPageGraphicsTest {
    @Test
    public void testSplitByHtml() {
        String[] split = PdfBoxPageGraphics.HTML_PATTERN.split("proba <b><i>vastag</i></b> szoveg <i>dolt</i>");
        assertArrayEquals(new String[]{"proba ","<b>","<i>","vastag","</i>","</b>"," szoveg ","<i>","dolt","</i>"}, split);
    }
}