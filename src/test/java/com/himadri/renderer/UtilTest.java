package com.himadri.renderer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertArrayEquals;

@RunWith(MockitoJUnitRunner.class)
public class UtilTest {

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

        final String text4 = "proba <b><i>vastag</i></b> szoveg <i>dolt</i><strong>vvv</strong>";
        String[] split4 = Util.splitWithDelimiters(text4, Util.HTML_TAG_PATTERN);
        assertArrayEquals(new String[]{"proba ","<b>","<i>","vastag","</i>","</b>"," szoveg ","<i>","dolt","</i>", "<strong>", "vvv", "</strong>"}, split4);

        String text2 = "megy a vonat Kanizsara";
        assertArrayEquals(new String[]{text2}, Util.splitWithDelimiters(text2, Util.HTML_TAG_PATTERN));

        String text3 = "megy<b> a vonat <p></p>megy< <i>a</i>> vonat <br>kanizsara<>";
        String[] split3 = Util.splitWithDelimiters(text3, Util.HTML_TAG_PATTERN_OR_WHITESPACE);
        assertArrayEquals(new String[]{"megy","<b>"," ", "a", " ", "vonat"," ","<p>","</p>","megy<"," ","<i>","a","</i>",">"," ","vonat"," ","<br>","kanizsara<>"}, split3);

    }



}