package com.himadri.graphics.pdfbox;

import com.himadri.dto.ErrorItem;
import com.himadri.model.service.UserSession;
import com.himadri.renderer.Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class PdfBoxGraphics {
    private static Logger LOG = LoggerFactory.getLogger(Util.class);

    private final PDDocument document;
    private final PDPage page;
    private final PDPageContentStream contentStream;
    private final float pageHeight;
    private final PDFontService fontService;
    private final PDColorTranslator colorTranslator;
    private final UserSession userSession;

    private PDFont currentFont;
    private float currentFontSize;

    public PdfBoxGraphics(PDDocument document, PDPage page, PDFontService fontService, PDColorTranslator colorTranslator, UserSession userSession) {
        this.document = document;
        this.page = page;
        this.fontService = fontService;
        this.pageHeight = page.getMediaBox().getHeight();
        this.colorTranslator = colorTranslator;
        this.userSession = userSession;

        try {
            this.contentStream = new PDPageContentStream(document, page);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setNonStrokingColor(Color color) {
        try {
            final PDColor pdColor = colorTranslator.translatePDColor(color);
            contentStream.setNonStrokingColor(pdColor);
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    public void setStrokingColor(Color color) {
        try {
            final PDColor pdColor = colorTranslator.translatePDColor(color);
            contentStream.setStrokingColor(pdColor);
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    public void setFont(Font font) {
        currentFont = fontService.getPDFont(document, font);
        currentFontSize = font.getSize2D();
        try {
            contentStream.setFont(currentFont, currentFontSize);
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    public PDFont getFont() {
        return currentFont;
    }

    public float getFontSize() {
        return currentFontSize;
    }

    public void drawString(String text, float x, float y) {
        try {
            contentStream.beginText();
            contentStream.setTextMatrix(Matrix.getTranslateInstance(x, pageHeight - y));
            contentStream.showText(removeSpecialCharacters(currentFont, text));
            contentStream.endText();
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    public void drawString(String text, float x, float y, double rotate) {
        try {
            contentStream.beginText();
            Matrix matrix = new Matrix();
            matrix.translate(x, pageHeight - y);
            matrix.rotate(-rotate);
            contentStream.setTextMatrix(matrix);
            contentStream.showText(removeSpecialCharacters(currentFont, text));
            contentStream.endText();
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    public int getStringWidth(String text) {
        try {
            return Math.round(currentFont.getStringWidth(removeSpecialCharacters(currentFont, text)) / 1000f * currentFontSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getStringWidth(Font font, String text) {
        try {
            final PDDocument pdDocument = new PDDocument();
            final PDFont pdFont = fontService.getPDFont(pdDocument, font);
            return Math.round(pdFont.getStringWidth(removeSpecialCharacters(pdFont, text)) / 1000f * font.getSize2D());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void drawImage(BufferedImage bufferedImage, float x, float y, float width, float height) {
        try {
            final PDImageXObject pdImageXObject = LosslessFactory.createFromImage(document, bufferedImage);
            contentStream.drawImage(pdImageXObject, x, pageHeight - y - height, width, height);
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    public void drawLine(float x1, float y1, float x2, float y2) {
        try {
            contentStream.moveTo(x1, pageHeight - y1);
            contentStream.lineTo(x2, pageHeight - y2);
            contentStream.stroke();
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }


    public void setLineWidth(float lineWidth) {
        try {
            contentStream.setLineWidth(lineWidth);
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    public void fillRect(float x, float y, float width, float height) {
        try {
            contentStream.addRect(x, pageHeight - y - height, width, height);
            contentStream.fill();
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    public void closeStream() {
        try {
            contentStream.close();
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    public void saveGraphicsState() {
        try {
            contentStream.saveGraphicsState();
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    public void restoreGraphicsState() {
        try {
            contentStream.restoreGraphicsState();
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    public void transform(float tx, float ty) {
        try {
            contentStream.transform(Matrix.getTranslateInstance(tx, -ty));
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    private String removeSpecialCharacters(PDFont pdFont, String text) {
        final int[] specialChars = text.chars().filter(value -> {
            try {
                pdFont.encode(String.valueOf((char) value));
                return false;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                return true;
            }
        }).toArray();
        for (int ch: specialChars) {
            userSession.addErrorItem(ErrorItem.Severity.WARN, ErrorItem.ErrorCategory.FORMATTING,
                    String.format("Egy speciális karaktert töröltünk %s mivel az nem megjeleníthető a jelenlegi betűtípussal: %s",
                            String.valueOf((char) ch), text));
            text = StringUtils.remove(text, (char) ch);
        }
        return text;
    }
}
