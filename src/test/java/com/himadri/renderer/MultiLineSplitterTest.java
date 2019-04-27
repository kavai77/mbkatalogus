package com.himadri.renderer;

import com.google.common.collect.ImmutableList;
import com.himadri.graphics.pdfbox.PDFontService;
import com.himadri.graphics.pdfbox.PdfBoxPageGraphics;
import com.himadri.model.service.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.awt.*;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class MultiLineSplitterTest {
    private static final Font font = Fonts.BOX_PRODUCT_DESCRIPTION_FONT;
    @Mock
    private PDFontService pdFontService;

    private static PdfBoxPageGraphics g2 = mock(PdfBoxPageGraphics.class);

    private Util util;

    private final String text;
    private final float[] widths;
    private final List<List<PdfObject>> expectedLines;


    public MultiLineSplitterTest(String text, float[] widths, List<List<PdfObject>> expectedLines) {
        this.text = text;
        this.widths = widths;
        this.expectedLines = expectedLines;
    }

    private static List<PdfObject> wrapBeginEndText(PdfObject... pdfObjects) {
        return ImmutableList.<PdfObject>builder()
            .add(new BeginTextPdfObject(g2))
            .add(pdfObjects)
            .add(new EndTextPdfObject(g2))
            .build();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {

        FontChangePdfObject boldItalicFontChange = new FontChangePdfObject(g2, font.deriveFont(Font.BOLD | Font.ITALIC));
        FontChangePdfObject boldFontChange = new FontChangePdfObject(g2, font.deriveFont(Font.BOLD));
        
        return of(
            new Object[] {
                " megy<b> a vonat <p><ul></p>megy< <i>a</i>> vonat <br>kanizsara<>",
                new float[] {3, 5},
                of(
                    wrapBeginEndText(new TextPdfObject(g2, "megy", 0), boldFontChange),
                    wrapBeginEndText(new TextPdfObject(g2, "a", 0)),
                    wrapBeginEndText(new TextPdfObject(g2, "vonat", 0)),
                    wrapBeginEndText(new TextPdfObject(g2, "megy<", 0)),
                    wrapBeginEndText(boldItalicFontChange, new TextPdfObject(g2, "a", 0), boldFontChange, new TextPdfObject(g2, ">", 0)),
                    wrapBeginEndText(new TextPdfObject(g2, "vonat", 0)),
                    wrapBeginEndText(new TextPdfObject(g2, "kanizsara<>", 0))
                )
            },
            new Object[] {
                " megy<b> a vonat <p><ul></p>megy< <i>a</i>> vonat <br>kanizsara<>",
                new float[] {30},
                of(
                    wrapBeginEndText(
                        new TextPdfObject(g2, "megy", 0),
                        boldFontChange,
                        new TextPdfObject(g2, " ", 0),
                        new TextPdfObject(g2, "a", 0),
                        new TextPdfObject(g2, " ", 0),
                        new TextPdfObject(g2, "vonat", 0)
                    ),
                    wrapBeginEndText(
                        new TextPdfObject(g2, "megy<", 0),
                        new TextPdfObject(g2, " ", 0),
                        boldItalicFontChange,
                        new TextPdfObject(g2, "a", 0),
                        boldFontChange,
                        new TextPdfObject(g2, ">", 0),
                        new TextPdfObject(g2, " ", 0),
                        new TextPdfObject(g2, "vonat", 0)

                    ),
                    wrapBeginEndText(new TextPdfObject(g2, "kanizsara<>", 0))
                )
            },
            new Object[] {
                " megy<b> a vonat <p><ul></p>megy< <i>a</i>> vonat <br>kanizsara<>",
                new float[] {9, 5, 7},
                of(
                    wrapBeginEndText(
                        new TextPdfObject(g2, "megy", 0),
                        boldFontChange,
                        new TextPdfObject(g2, " ", 0),
                        new TextPdfObject(g2, "a", 0)
                    ),
                    wrapBeginEndText(new TextPdfObject(g2, "vonat", 0)),
                    wrapBeginEndText(
                        new TextPdfObject(g2, "megy<", 0),
                        new TextPdfObject(g2, " ", 0),
                        boldItalicFontChange,
                        new TextPdfObject(g2, "a", 0),
                        boldFontChange
                    ),
                    wrapBeginEndText(
                        new TextPdfObject(g2, ">", 0),
                        new TextPdfObject(g2, " ", 0),
                        new TextPdfObject(g2, "vonat", 0)
                    ),
                    wrapBeginEndText(new TextPdfObject(g2, "kanizsara<>", 0))
                )
            }
        );
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        when(g2.getDocument()).thenReturn(mock(PDDocument.class));
        when(g2.getStringWidth(any(), anyFloat(), anyString())).thenAnswer((Answer<Float>) it -> (float) ((String) it.getArgument(2)).length());
        when(g2.removeSpecialCharacters(any(), anyString())).then((Answer<String>) it -> (String) it.getArgument(1));
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        util = new Util();
        util.pdFontService = pdFontService;
        when(pdFontService.getPDFont(any(), any())).thenReturn(mock(PDFont.class));
    }

    @Test
    public void splitGraphicsText() {
        Paragraph actualParagraph = util.splitMultiLineText(g2, font, text, widths);

        assertEquals(expectedLines.size(), actualParagraph.getLines().size());
        for (int i = 0; i < expectedLines.size(); i++) {
            assertLineEquals(expectedLines.get(i), actualParagraph.getLines().get(i));
        }

    }

    private void assertLineEquals(List<PdfObject> expectedPdfObjects, Line actualLine) {
        assertEquals(expectedPdfObjects.size(), actualLine.getObjects().size());
        for (int i = 0; i < expectedPdfObjects.size(); i++) {
            PdfObject actualPdfObject = actualLine.getObjects().get(i);
            PdfObject expectedPdfObject = expectedPdfObjects.get(i);
            assertEquals(expectedPdfObject.getClass(), actualPdfObject.getClass());
            switch (actualPdfObject.getClass().getSimpleName()) {
                case "TextPdfObject":
                    assertEquals(((TextPdfObject) expectedPdfObject).getText(), ((TextPdfObject) actualPdfObject).getText());
                    break;
                case "UnderlinePdfObject":
                    assertEquals(((UnderlinePdfObject) expectedPdfObject).getUnderLineX1(), ((UnderlinePdfObject) actualPdfObject).getUnderLineX1(), 0.0f);
                    assertEquals(((UnderlinePdfObject) expectedPdfObject).getUnderLineX2(), ((UnderlinePdfObject) actualPdfObject).getUnderLineX2(), 0.0f);
                    break;
                case "FontChangePdfObject":
                    assertEquals(((FontChangePdfObject) expectedPdfObject).getFont().getStyle(), ((FontChangePdfObject) actualPdfObject).getFont().getStyle());
                    break;
                case "BeginTextPdfObject":
                case "EndTextPdfObject":
                    break;
                default:
                    fail("Unrecognized PdfObject " + actualPdfObject.getClass().getSimpleName());
            }
        }
    }

}
