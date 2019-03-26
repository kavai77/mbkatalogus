package com.himadri.graphics.pdfbox;

import com.himadri.renderer.Util;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class PdfBoxPageGraphicsTest {
    @Test
    public void testSplitByHtml() {
        String[] split = Util.HTML_TAG_PATTERN.split("proba <b><i>vastag</i></b> szoveg <i>dolt</i><strong>vvv</strong>");
        assertArrayEquals(new String[]{"proba ","<b>","<i>","vastag","</i>","</b>"," szoveg ","<i>","dolt","</i>", "<strong>", "vvv", "</strong>"}, split);
    }
}