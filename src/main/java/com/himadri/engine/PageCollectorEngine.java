package com.himadri.engine;

import com.google.common.cache.Cache;
import com.himadri.dto.ErrorItem;
import com.himadri.dto.UserRequest;
import com.himadri.engine.ItemCategorizerEngine.CsvItemGroup;
import com.himadri.engine.ItemCategorizerEngine.CsvProductGroup;
import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.Page;
import com.himadri.model.service.UserSession;
import com.himadri.renderer.PageRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Component
public class PageCollectorEngine {
    @Autowired
    ItemToBoxConverter itemToBoxConverter;

    @Autowired
    Cache<String, UserSession> userSessionCache;

    public List<Page> createPages(List<CsvProductGroup> productGroups, String title, UserRequest userRequest) {
        final List<Page> pageList = new ArrayList<>();
        final int firstPageRow = userRequest.getSkipBoxSpaceOnBeginning() % PageRenderer.BOX_ROWS_PER_PAGE;
        final int firstPageColumn = userRequest.getSkipBoxSpaceOnBeginning() / PageRenderer.BOX_ROWS_PER_PAGE;
        PageConstructor pageConstructor = new PageConstructor(title, firstPageRow, firstPageColumn);
        for (int indexOfProductGroup = 0; indexOfProductGroup < productGroups.size(); indexOfProductGroup++) {
            CsvProductGroup productGroup = productGroups.get(indexOfProductGroup);
            for (CsvItemGroup boxItems : productGroup.getItemGroups()) {
                final List<Box> itemBoxes = itemToBoxConverter.createBox(boxItems, indexOfProductGroup,
                        productGroup.getName(), userRequest, new int[]{});
                for (Box box : itemBoxes) {
                    final boolean added = pageConstructor.addBoxToPage(box);
                    if (!added) {
                        final Optional<Page> newPage = pageConstructor.createNewPage(pageList.size() + 1);
                        newPage.ifPresent(pageList::add);
                        pageConstructor = new PageConstructor(title);
                        final boolean addedToNewPage = pageConstructor.addBoxToPage(box);
                        if (!addedToNewPage) {
                            final UserSession userSession = userSessionCache.getIfPresent(userRequest.getRequestId());
                            userSession.addErrorItem(ErrorItem.Severity.ERROR, ErrorItem.ErrorCategory.FORMATTING,
                                "Nem sikerült a dobozt hozzáadni egy üres oldalhoz, ezért kihagyjuk: " +
                                        box.getTitle());
                        }
                    }
                }
            }
        }
        final Optional<Page> newPage = pageConstructor.createNewPage(pageList.size() + 1);
        newPage.ifPresent(pageList::add);
        return pageList;
    }

    private static class PageConstructor {
        private final String title;
        private int column;
        private List<Box> pageBoxes = new ArrayList<>();
        private boolean wideBoxPossible = true;
        private final boolean[][] boxOccupancyMatrix;

        PageConstructor(String title) {
            this(title, 0, 0);
        }

        PageConstructor(String title, int row, int column) {
            this.title = title;
            this.column = column;
            boxOccupancyMatrix = new boolean[PageRenderer.BOX_ROWS_PER_PAGE][];
            for (int i = 0; i < PageRenderer.BOX_ROWS_PER_PAGE; i++) {
                boxOccupancyMatrix[i] = new boolean[PageRenderer.BOX_COLUMNS_PER_PAGE];
            }
            for (int r = 0; r < row; r++) {
                boxOccupancyMatrix[r][column] = true;
            }
        }

        boolean addBoxToPage(Box box) {
            if (box.getWidth() > 1 && !wideBoxPossible) {
                return false;
            }
            if (box.getWidth() == 1) {
                wideBoxPossible = false;
            }
            int[] bottomFreeSpaces = getBottomFreeSpacesFromCurrentColumn();
            final OptionalInt firstFittingColumn =
                    IntStream.range(0, bottomFreeSpaces.length)
                    .filter(c -> isFreeSpaceFromColumn(bottomFreeSpaces, c, box))
                    .findFirst();
            if (!firstFittingColumn.isPresent()) {
                return false;
            }
            column += firstFittingColumn.getAsInt();
            final int row = PageRenderer.BOX_ROWS_PER_PAGE - bottomFreeSpaces[firstFittingColumn.getAsInt()];
            box.setDimensions(row, column);
            pageBoxes.add(box);
            for (int r = 0; r < box.getHeight(); r++) {
                for (int c = 0; c < box.getWidth(); c++) {
                    boxOccupancyMatrix[row + r][column + c] = true;
                }
            }
            return true;
        }

        private boolean isFreeSpaceFromColumn(int[] bottomFreeSpaces, int startColumn, Box box) {
            if (box.getWidth() > bottomFreeSpaces.length - startColumn) {
                return false;
            }
            for (int c = 0; c < box.getWidth(); c++) {
                if (bottomFreeSpaces[c + startColumn] < box.getHeight()) {
                    return false;
                }
            }
            return true;
        }

        Optional<Page> createNewPage(int pageNumber) {
            if (pageBoxes.isEmpty()) {
                return empty();
            }
            final Box firstBox = pageBoxes.get(0);
            return of(new Page(title, firstBox.getProductGroup(), pageNumber,
                    pageNumber % 2 == 0 ? Page.Orientation.LEFT : Page.Orientation.RIGHT, pageBoxes));
        }

        int[] getBottomFreeSpacesFromCurrentColumn() {
            final int[] bottomFreeSpaces = new int[PageRenderer.BOX_COLUMNS_PER_PAGE - column];
            for (int c = column; c < PageRenderer.BOX_COLUMNS_PER_PAGE; c++) {
                int r = PageRenderer.BOX_ROWS_PER_PAGE - 1;
                while (r >= 0 && !boxOccupancyMatrix[r][c]) {
                    bottomFreeSpaces[c - column]++;
                    r--;
                }
            }
            return bottomFreeSpaces;
        }
    }
}
