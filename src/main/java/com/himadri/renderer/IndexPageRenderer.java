package com.himadri.renderer;

import com.himadri.graphics.pdfbox.PdfBoxPageGraphics;
import com.himadri.model.rendering.Index;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class IndexPageRenderer {
    private static final float WIDTH = Math.round(PDRectangle.A4.getWidth());

    private static final float MARGIN_LEFT_RIGHT = 30f;
    private static final float MARGIN_TOP = 50f;
    private static final float COLUMN_GAP = 14f;
    private static final float PAGE_COLUMN_WIDTH = 21f;
    private static final float BOX_HEIGHT = 14.5f;
    private static final float TEXT_MARGIN = 4f;

    @Autowired
    private Util util;

    public void renderIndex(PdfBoxPageGraphics g2, java.util.List<Index.Record> content, int from,
                            String keyName, String valueName,
                            String catalogueTitle, String indexName,
                            int rowNb, int columnNb) {
        // drawing catalogue title
        g2.setNonStrokingColor(Color.lightGray);
        g2.setFont(Fonts.PAGE_CATALOGUE_TITLE_FONT);
        g2.drawString(catalogueTitle, MARGIN_LEFT_RIGHT, MARGIN_TOP - 15);

        //drawing index name
        g2.setNonStrokingColor(Color.lightGray);
        g2.drawString(indexName, WIDTH - MARGIN_LEFT_RIGHT - g2.getStringWidth(indexName), MARGIN_TOP - 15);

        g2.transform(MARGIN_LEFT_RIGHT, MARGIN_TOP);
        final float columnWidth = calculateColumnWidth(columnNb);
        final float keyWidth = columnWidth - PAGE_COLUMN_WIDTH;
        g2.setNonStrokingColor(Color.black);
        g2.setLineWidth(.5f);
        for (int col = 0; col < columnNb; col++) {
            g2.setFont(Fonts.INDEX_CONTENT_HEAD);
            if (keyName != null) {
                g2.drawString(keyName, 2, -2);
            }
            if (valueName != null) {
                g2.drawString(valueName, keyWidth + 2, -2);
            }

            g2.addRect(0, 0, columnWidth, rowNb * BOX_HEIGHT);
            g2.drawLine(keyWidth, 0, keyWidth, rowNb * BOX_HEIGHT);
            g2.setFont(Fonts.INDEX_CONTENT_FONT);
            for (int row = 0; row < rowNb; row++) {
                if (row > 0) {
                    g2.drawLine(0, row * BOX_HEIGHT, columnWidth, row * BOX_HEIGHT);
                }
                final int index = from + rowNb * col + row;
                if (index < content.size()) {
                    final float baseLine = (row + 1) * BOX_HEIGHT - TEXT_MARGIN;
                    final Index.Record record = content.get(index);
                    g2.drawString(record.getKey(), TEXT_MARGIN, baseLine);
                    final String pageNbStr = Integer.toString(record.getPageNumber());
                    g2.drawString(pageNbStr, columnWidth - TEXT_MARGIN - g2.getStringWidth(pageNbStr), baseLine);
                }
            }

            g2.transform(columnWidth + COLUMN_GAP, 0);
        }
    }

    public float calculateKeySplitWidth(int columnNb) {
        return calculateColumnWidth(columnNb) - PAGE_COLUMN_WIDTH - TEXT_MARGIN;
    }

    private float calculateColumnWidth(int columnNb) {
        return (WIDTH - 2 * MARGIN_LEFT_RIGHT - (columnNb - 1) * COLUMN_GAP) / columnNb;
    }
}
