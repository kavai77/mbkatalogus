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
        int occupiedSpaceInPage = 0;
        List<Box> pageBoxes = new ArrayList<>();
        for (Box box: boxList) {
            if (occupiedSpaceInPage + box.getOccupiedSpace() > PageRenderer.BOX_PER_PAGE) {
                addNewPage(pageList, pageBoxes, title);
                pageBoxes = new ArrayList<>();
                occupiedSpaceInPage = 0;
            }
            occupiedSpaceInPage += box.getOccupiedSpace();
            pageBoxes.add(box);
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
