package com.himadri.engine;

import com.himadri.model.Box;
import com.himadri.model.Page;
import com.himadri.renderer.PageRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PageCollectorEngine {
    @Autowired
    private UserSession userSession;

    public List<Page> createPages(List<Box> boxList) {
        List<Page> pageList = new ArrayList<>();
        int occupiedSpaceInPage = 0;
        List<Box> pageBoxes = new ArrayList<>();
        for (Box box: boxList) {
            if (occupiedSpaceInPage + box.getOccupiedSpace() > PageRenderer.BOX_PER_PAGE) {
                addNewPage(pageList, pageBoxes);
                pageBoxes = new ArrayList<>();
                occupiedSpaceInPage = 0;
            }
            occupiedSpaceInPage += box.getOccupiedSpace();
            pageBoxes.add(box);
        }
        addNewPage(pageList, pageBoxes);
        return pageList;
    }

    private void addNewPage(List<Page> pageList, List<Box> pageBoxes) {
        final Box firstBox = pageBoxes.get(0);
        final int pageNumber = pageList.size() + 1;
        Page page = new Page(userSession.getUserRequest().getCatalogueName(),
               firstBox.getProductGroup(), pageNumber,
                pageNumber % 2 == 0 ? Page.Orientation.RIGHT : Page.Orientation.LEFT, pageBoxes);
        pageList.add(page);
    }
}
