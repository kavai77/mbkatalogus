package com.himadri.renderer;

import com.himadri.graphics.pdfbox.PDFontService;
import com.himadri.graphics.pdfbox.PdfBoxPageGraphics;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.awt.*;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtilTest {
    @Mock
    private PDFontService pdFontService;

    private Util util;

    @Before
    public void setUp() throws Exception {
        util = new Util();
        util.pdFontService = pdFontService;
        when(pdFontService.getPDFont(any(), any())).thenReturn(mock(PDFont.class));
    }

    @Test
    public void forceLineBreakTest() {
        String text = "megy;;a vonat<p>kanizsara<br>.";
        String[] split = Util.LINE_BREAK_PATTERN.split(text);
        assertArrayEquals(new String[] {"megy", "a vonat", "kanizsara", "."}, split);
    }

    @Test
    public void splitHTMLCharacters() {
        String text = "megy<b> a vonat <p></p>megy< <i>a</i>> vonat <br>kanizsara<>";
        String[] split = Util.splitWithDelimiters(text, Util.HTML_TAG_PATTERN);
        assertArrayEquals(new String[]{"megy","<b>"," a vonat ","<p>","</p>","megy< ","<i>","a","</i>","> vonat ","<br>","kanizsara<>"}, split);

        String text2 = "megy a vonat Kanizsara";
        assertArrayEquals(new String[]{text2}, Util.splitWithDelimiters(text2, Util.HTML_TAG_PATTERN));

        String text3 = "megy<b> a vonat <p></p>megy< <i>a</i>> vonat <br>kanizsara<>";
        String[] split3 = Util.splitWithDelimiters(text, Util.HTML_TAG_PATTERN_OR_WHITESPACE);
        assertArrayEquals(new String[]{"megy","<b>"," ", "a", " ", "vonat"," ","<p>","</p>","megy<"," ","<i>","a","</i>",">"," ","vonat"," ","<br>","kanizsara<>"}, split3);

    }

    @Test
    public void splitGraphicsText() {
        PdfBoxPageGraphics g2 = mock(PdfBoxPageGraphics.class);
        when(g2.getDocument()).thenReturn(mock(PDDocument.class));
        when(g2.getStringWidth(any(), anyFloat(), anyString())).thenAnswer((Answer<Integer>) it -> ((String) it.getArgument(2)).length());
        String text = " megy<b> a vonat <p><ul></p>megy< <i>a</i>> vonat <br>kanizsara<>";

        String[] split35 = util.splitGraphicsText(g2, mock(Font.class), text, 3f, 5f);

        assertArrayEquals(new String[]{"megy<b>", "a", "vonat", "megy<", "<i>a</i>>", "vonat", "kanizsara<>"}, split35);

        String[] split30 = util.splitGraphicsText(g2, mock(Font.class), text, 30f);
        assertArrayEquals(new String[]{"megy<b> a vonat", "megy< <i>a</i>> vonat", "kanizsara<>"}, split30);

    }
}