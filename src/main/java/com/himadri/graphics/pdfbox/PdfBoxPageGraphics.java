package com.himadri.graphics.pdfbox;

import com.google.common.annotations.VisibleForTesting;
import com.himadri.dto.ErrorItem;
import com.himadri.model.service.UserSession;
import com.himadri.renderer.Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.regex.Pattern;

public class PdfBoxPageGraphics {
    private static Logger LOG = LoggerFactory.getLogger(Util.class);
    public static final Pattern HTML_PATTERN = Pattern.compile("((?<=<b>)|(?=<b>)|(?<=</b>)|(?=</b>)|(?<=<i>)|(?=<i>)|(?<=</i>)|(?=</i>))");

    private final PDDocument document;
    private final PDPageContentStream contentStream;
    private final float pageHeight;
    private final PDFontService fontService;
    private final PDColorTranslator colorTranslator;
    private final UserSession userSession;

    private PDFont currentFont;
    private Font currentRawFont;

    public PdfBoxPageGraphics(PDDocument document, PDRectangle pageSize,
                              PDFontService fontService, PDColorTranslator colorTranslator,
                              @Nullable UserSession userSession) {
        this.document = document;
        this.fontService = fontService;
        this.pageHeight = pageSize.getHeight();
        this.colorTranslator = colorTranslator;
        this.userSession = userSession;

        final PDPage page = new PDPage(pageSize);
        document.addPage(page);

        try {
            this.contentStream = new PDPageContentStream(document, page);
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    public static PdfBoxPageGraphics createForStringWidthCalculation(PDFontService fontService) {
        return new PdfBoxPageGraphics(new PDDocument(), new PDRectangle(0, 0), fontService, null, null);
    }

    public PDDocument getDocument() {
        return document;
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
        currentRawFont = font;
        currentFont = fontService.getPDFont(document, font);
        try {
            contentStream.setFont(currentFont, font.getSize2D());
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    public PDFont getFont() {
        return currentFont;
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

    public void drawHtmlString(String text, float x, float y) {
        try {
            Font originalRawFont = currentRawFont;
            contentStream.beginText();
            contentStream.setTextMatrix(Matrix.getTranslateInstance(x, pageHeight - y));
            String[] split = HTML_PATTERN.split(removeSpecialCharacters(currentFont, text));
            int currentStyle = 0;
            for (String piece: split) {
                switch (piece) {
                    case "<b>":
                        currentStyle |= Font.BOLD;
                        setFont(originalRawFont.deriveFont(currentStyle));
                        break;
                    case "</b>":
                        currentStyle &= ~Font.BOLD;
                        setFont(originalRawFont.deriveFont(currentStyle));
                        break;
                    case "<i>":
                        currentStyle |= Font.ITALIC;
                        setFont(originalRawFont.deriveFont(currentStyle));
                        break;
                    case "</i>":
                        currentStyle &= ~Font.ITALIC;
                        setFont(originalRawFont.deriveFont(currentStyle));
                        break;
                    default:
                        contentStream.showText(piece);
                }
            }
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
        return getStringWidth(currentFont, currentRawFont.getSize2D(), text);
    }

    public int getStringWidth(PDFont pdFont, float fontSize, String text) {
        try {
            return Math.round(pdFont.getStringWidth(removeSpecialCharacters(pdFont, text)) / 1000f * fontSize);
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    public void drawImage(BufferedImage bufferedImage, float x, float y, float width, float height) {
        try {
            final PDImageXObject pdImageXObject = JPEGFactory.createFromImage(document, bufferedImage);
            drawImage(pdImageXObject, x, y, width, height);
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    public void drawImage(PDImageXObject pdImageXObject, float x, float y, float width, float height) {
        try {
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

    public void drawLineByWidth(float x1, float y1, float width, float height) {
        drawLine(x1, y1, x1 + width, y1 + height);
    }


    public void setLineWidth(float lineWidth) {
        try {
            contentStream.setLineWidth(lineWidth);
        } catch (IOException e) {
            throw new PdfBoxGraphicsException(e);
        }
    }

    public void addRect(float x, float y, float width, float height) {
        try {
            contentStream.addRect(x, pageHeight - y - height, width, height);
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

    @VisibleForTesting
    String removeSpecialCharacters(PDFont pdFont, String text) {
        final int[] specialChars = text.chars().filter(value -> {
            try {
                pdFont.encode(String.valueOf((char) value));
                return false;
            } catch (IOException e) {
                throw new PdfBoxGraphicsException(e);
            } catch (IllegalArgumentException e) {
                return true;
            }
        }).toArray();
        for (int ch: specialChars) {
            if (userSession != null) {
                userSession.addErrorItem(ErrorItem.Severity.WARN, ErrorItem.ErrorCategory.FORMATTING,
                        String.format("Egy speciális karaktert töröltünk %s mivel az nem megjeleníthető a jelenlegi betűtípussal: %s",
                                String.valueOf((char) ch), text));
            }
            text = StringUtils.remove(text, (char) ch);
        }
        return text;
    }
}
