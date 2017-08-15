package com.himadri.engine;

import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.Page;
import com.himadri.renderer.PageRenderer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PageCollectorEngine {
    public List<Page> createPages(List<Box> boxList, String title) {
        List<Page> pageList = new ArrayList<>();
        List<Box> pageBoxes = new ArrayList<>();
        int row = 0;
        int column = 0;
        for (Box box: boxList) {
            if (box.getOccupiedSpace() > PageRenderer.BOX_ROWS_PER_PAGE) {
                throw new RuntimeException("Túl sok egybefüggő cikk a dobozban, nem fér ki egy hasábra: " + box.getArticles().get(0).getNumber());
            }
            if (row + box.getOccupiedSpace() > PageRenderer.BOX_ROWS_PER_PAGE) {
                row = 0;
                column++;
                if (column == PageRenderer.BOX_COLUMNS_PER_PAGE) {
                    addNewPage(pageList, pageBoxes, title);
                    pageBoxes = new ArrayList<>();
                    column = 0;
                }
            }
            box.setDimensions(row, column);
            pageBoxes.add(box);
            row += box.getOccupiedSpace();
        }
        addNewPage(pageList, pageBoxes, title);
        return pageList;
    }

    private void addNewPage(List<Page> pageList, List<Box> pageBoxes, String title) {
        final Box firstBox = pageBoxes.get(0);
        final int pageNumber = pageList.size() + 1;
        Page page = new Page(title, firstBox.getProductGroup(), pageNumber,
                pageNumber % 2 == 0 ? Page.Orientation.RIGHT : Page.Orientation.LEFT, pageBoxes);
        pageList.add(page);
    }
}
