package com.himadri.model.service

import com.himadri.graphics.pdfbox.PDFontService
import com.himadri.graphics.pdfbox.PdfBoxPageGraphics
import java.awt.Font

class Paragraph (
    val g2: PdfBoxPageGraphics,
    val pdFontService: PDFontService,
    var font: Font
){
    val lines: MutableList<Line> = arrayListOf()
    private var line: Line = Line()
    private var pdFont = pdFontService.getPDFont(g2.document, font);
    private var currentStyle = font.style
    private var underline = false

    fun lineBreak() {
        if (line.isNotBlank()) {
            line.removeBlankWordsAtEnd()
            if (underline) {
                line.addPdfObject(UnderlinePdfObject(g2, line.underLineXStart, line.totalWidth))
            }
            lines.add(line)
            line = Line()
        }
    }

    fun addWord(word: String, width: FloatArray) {
        val wordWidth = g2.getStringWidth(pdFont, font.size2D, word)
        if (line.totalWidth + wordWidth > width[minOf(lines.size, width.size - 1)]) {
            lineBreak()
        }
        line.addPdfObject(TextPdfObject(g2, word, wordWidth))
    }

    fun addToCurrentStyle(style: Int) = changeCurrentStyle(currentStyle or style)

    fun removeFromCurrentStyle(style: Int) = changeCurrentStyle(currentStyle and style.inv())

    private fun changeCurrentStyle(style: Int) {
        currentStyle = style
        val derivedFont = font.deriveFont(currentStyle)
        pdFont = pdFontService.getPDFont(g2.document, derivedFont)
        line.addPdfObject(FontChangePdfObject(g2, derivedFont))
    }

    fun startUnderLine() {
        line.underLineXStart = line.totalWidth
        underline = true
    }

    fun addUnderline() {
        underline = false
        line.addPdfObject(UnderlinePdfObject(g2, line.underLineXStart, line.totalWidth))
    }
}

class Line {
    val objects: MutableList<PdfObject> = arrayListOf()
    var totalWidth = 0f
    var underLineXStart = 0f

    fun isBlank() = !isNotBlank()

    fun isNotBlank() =
        objects.any {it is TextPdfObject && it.text.isNotBlank()}

    fun addPdfObject(obj: PdfObject) {
        if (obj is TextPdfObject && obj.text.isBlank() && isBlank()) {
            return
        }
        objects.add(obj)
        totalWidth += obj.width()
    }

    fun removeBlankWordsAtEnd() {
        val iterator = objects.listIterator(objects.size)
        while (iterator.hasPrevious()) {
            val current = iterator.previous()
            if (current is TextPdfObject) {
                if (current.text.isBlank()) {
                    iterator.remove()
                    totalWidth -= current.width()
                } else {
                    return
                }
            }
        }
    }

}


interface PdfObject {
    fun render(x: Float, y: Float)
    fun width(): Float
}

class TextPdfObject (
    val g2: PdfBoxPageGraphics,
    val text: String,
    val width: Float
) : PdfObject {
    override fun render(x: Float, y: Float) {
        g2.showText(text)
    }

    override fun width() = width
}

class UnderlinePdfObject (
    val g2: PdfBoxPageGraphics,
    val underLineX1: Float,
    val underLineX2: Float
) : PdfObject {
    override fun render(x: Float, y: Float) {
        g2.drawLine(underLineX1 + x, y + 0.7f, underLineX2 + x, y + 0.7f)
    }

    override fun width() = 0f
}

class FontChangePdfObject (
    val g2: PdfBoxPageGraphics,
    val font: Font
) : PdfObject {
    override fun render(x: Float, y: Float) {
        g2.setFont(font)
    }

    override fun width() = 0f
}