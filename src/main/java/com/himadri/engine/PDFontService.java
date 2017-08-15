package com.himadri.engine;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.himadri.exception.FontFileNotFoundException;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.replace;

@Component
public class PDFontService {
    @Autowired
    private LoadingCache<String, TrueTypeFont> trueTypeFontLoadingCache;

    public PDFont getPDFont(PDDocument pdDocument, Font font) throws FontFileNotFoundException {
        final String fontFileName = getFontFileName(font);
        try {
            final TrueTypeFont typeFont = trueTypeFontLoadingCache.getUnchecked(fontFileName);
            return PDType0Font.load(pdDocument, typeFont, true);
        } catch (UncheckedExecutionException e) {
            throw new FontFileNotFoundException("Could not load font file: " + fontFileName, e);
        } catch (IOException e) {
            throw new FontFileNotFoundException("Could not embed true type font: " + fontFileName, e);
        }
    }

    private String getFontFileName(Font font) {
        StringBuilder name = new StringBuilder("/fonts/");
        name.append(replace(lowerCase(font.getName()), " ", "-"));
        if (font.isBold()) {
            name.append("-bold");
        }
        if (font.isItalic()) {
            name.append("-italic");
        }
        name.append(font.getName().startsWith("Avenir") ? ".ttc" : ".ttf");
        return name.toString();
    }
}
