package com.himadri.engine;

import com.himadri.graphics.pdfbox.PDFontService;
import com.himadri.graphics.pdfbox.PdfBoxPageGraphics;
import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.Index;
import com.himadri.model.rendering.Page;
import com.himadri.renderer.IndecesRenderer;
import com.himadri.renderer.IndexPageRenderer;
import com.himadri.renderer.Util;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.himadri.renderer.IndexPageRenderer.CONTENT_FONT;
import static org.apache.commons.lang3.StringUtils.removeEnd;

@Component
public class IndexEngine {
    @Autowired
    private PDFontService pdFontService;

    @Autowired
    private IndexPageRenderer indexPageRenderer;

    @Autowired
    private Util util;

    public Index createIndex(List<Page> pages) {
        Index index = new Index();
        SortedSet<Index.Record> productNameSet = new TreeSet<>();
        PdfBoxPageGraphics g2 = new PdfBoxPageGraphics(new PDDocument(), pdFontService, null, null);
        for (Page page: pages) {
            for (Box box: page.getBoxes()) {
                String title = getProductNameForIndex(g2, box.getTitle());
                productNameSet.add(new Index.Record(title, page.getPageNumber()));
                for (Box.Article article: box.getArticles()) {
                    index.getProductNumberIndex().add(new Index.Record(article.getNumber(), page.getPageNumber()));
                }
            }
        }
        index.getProductNameIndex().addAll(productNameSet);
        Collections.sort(index.getProductNumberIndex());
        g2.closeStream();
        return index;
    }

    private String getProductNameForIndex(PdfBoxPageGraphics g2, String boxTitle) {
        final String[] splitTitle = util.splitGraphicsText(g2, CONTENT_FONT, boxTitle,
                indexPageRenderer.calculateKeySplitWidth(IndecesRenderer.PRODUCT_NAME_BOX_COLUMN_NB));
        return removeEnd(removeEnd(splitTitle[0], ","), ";");
    }
}
