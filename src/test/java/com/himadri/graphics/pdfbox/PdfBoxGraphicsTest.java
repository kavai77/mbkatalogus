package com.himadri.graphics.pdfbox;

import com.himadri.model.service.UserSession;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PdfBoxGraphicsTest {

    @Mock
    private PDFontService pdFontService;

    @Mock
    private PDColorTranslator pdColorTranslator;

    @Mock
    private UserSession userSession;

    private final PDDocument document = new PDDocument();
    private final PDPage page = new PDPage();

    private PdfBoxGraphics sut;

    @Before
    public void setUp() throws Exception {
        document.addPage(page);
        sut = new PdfBoxGraphics(document, page, pdFontService, pdColorTranslator, userSession);
    }

    @Test
    public void removeSpecialCharacters() throws Exception {
        PDFont pdFont = PDType0Font.load(document,
                new TTFParser().parse(PdfBoxGraphics.class.getResourceAsStream("/fonts/arial.ttf")), true);
        assertEquals("", sut.removeSpecialCharacters(pdFont, ""));
        assertEquals("test", sut.removeSpecialCharacters(pdFont, "test"));
        assertEquals("éáűőúíöüóÉÁŰŐÚÍÖÜÓ", sut.removeSpecialCharacters(pdFont, "éáűőúíöüóÉÁŰŐÚÍÖÜÓ"));
        assertEquals("testtest", sut.removeSpecialCharacters(pdFont, "test\uFFFDtest\uFFFE"));
    }

}