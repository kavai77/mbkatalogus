package com.himadri.engine;

import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.Page;
import com.himadri.model.rendering.TableOfContent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TableOfContentEngine {
    public TableOfContent createTableOfContent(List<Page> pages) {
        TableOfContent tableOfContent = new TableOfContent();
        for (Page page: pages) {
            for (Box box: page.getBoxes()) {
                if (box.getBoxType() == Box.Type.ARTICLE) {
                    TableOfContent.TableOfContentItem tableOfContentItem = new TableOfContent.TableOfContentItem(
                        page.getPageNumber(),
                        box.getProductColor()
                    );
                    tableOfContent.getTableOfContent().putIfAbsent(box.getProductGroup(), tableOfContentItem);
                }
            }
        }
        return tableOfContent;
    }

}
