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

    public List<Page> createPages(List<CsvProductGroup> productGroups, UserRequest userRequest) {
        PageListFactory pageListFactory = new PageListFactory(productGroups, userRequest, itemToBoxConverter, userSessionCache);
        List<Page> pages = pageListFactory.createPages(null, 0);
        Box footerImageBox = itemToBoxConverter.createImageBox(userRequest.getFooterImageStream(),
            userRequest.isWideFooterImage(), userRequest, "végső");
        if (footerImageBox != null) {
            return pageListFactory.createPages(footerImageBox, pages.size());
        } else {
            return pages;
        }
    }

    private static class PageListFactory {
        private PageBuilder currentPageBuilder;
        private final List<Page> pageList = new ArrayList<>();
        private final List<CsvProductGroup> productGroups;
        private final UserRequest userRequest;
        private final ItemToBoxConverter itemToBoxConverter;
        private final Cache<String, UserSession> userSessionCache;


        public PageListFactory(List<CsvProductGroup> productGroups, UserRequest userRequest,
                               ItemToBoxConverter itemToBoxConverter, Cache<String, UserSession> userSessionCache) {
            this.productGroups = productGroups;
            this.userRequest = userRequest;
            this.itemToBoxConverter = itemToBoxConverter;
            this.userSessionCache = userSessionCache;
        }

        public List<Page> createPages(Box footerImageBox, int lastPageNumber) {
            Box headerImageBox = itemToBoxConverter.createImageBox(userRequest.getHeaderImageStream(),
                userRequest.isWideHeaderImage(), userRequest, "kezdő");
            createNewPageBuilder(footerImageBox, lastPageNumber);
            if (headerImageBox != null) {
                currentPageBuilder.addBoxToPage(headerImageBox);
            }
            for (int indexOfProductGroup = 0; indexOfProductGroup < productGroups.size(); indexOfProductGroup++) {
                CsvProductGroup productGroup = productGroups.get(indexOfProductGroup);
                for (CsvItemGroup csvItemGroup : productGroup.getItemGroups()) {
                    final List<Box> itemBoxes = itemToBoxConverter.createArticleBox(csvItemGroup, indexOfProductGroup,
                        productGroup.getName(), userRequest, currentPageBuilder.getBottomFreeSpacesFromCurrentColumn());
                    for (Box box : itemBoxes) {
                        final boolean added = currentPageBuilder.addBoxToPage(box);
                        if (!added) {
                            addCurrentBuilderToPage();
                            createNewPageBuilder(footerImageBox, lastPageNumber);
                            final boolean addedToNewPage = currentPageBuilder.addBoxToPage(box);
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
            addCurrentBuilderToPage();

            return pageList;
        }

        private void createNewPageBuilder(Box footerImageBox, int lastPageNumber) {
            currentPageBuilder = new PageBuilder(userRequest.getCatalogueTitle(), pageList.size() + 1);
            if (footerImageBox != null && currentPageBuilder.getPageNumber() == lastPageNumber) {
                currentPageBuilder.addBoxToBottom(footerImageBox);
            }
        }

        private void addCurrentBuilderToPage() {
            final Optional<Page> newPage = currentPageBuilder.build();
            newPage.ifPresent(pageList::add);
        }
    }

    private static class PageBuilder {
        private final String title;
        private int column;
        private List<Box> pageBoxes = new ArrayList<>();
        private boolean wideBoxPossible = true;
        private final boolean[][] boxOccupancyMatrix;
        private final int pageNumber;

        PageBuilder(String title, int pageNumber) {
            this.title = title;
            this.pageNumber = pageNumber;
            boxOccupancyMatrix = new boolean[PageRenderer.BOX_ROWS_PER_PAGE][];
            for (int i = 0; i < PageRenderer.BOX_ROWS_PER_PAGE; i++) {
                boxOccupancyMatrix[i] = new boolean[PageRenderer.BOX_COLUMNS_PER_PAGE];
            }
        }

        public int getPageNumber() {
            return pageNumber;
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
            occupyBox(box);
            return true;
        }

        void addBoxToBottom(Box box) {
            final int row = PageRenderer.BOX_ROWS_PER_PAGE - box.getHeight();
            final int column = PageRenderer.BOX_COLUMNS_PER_PAGE - box.getWidth();
            box.setDimensions(row, column);
            occupyBox(box);
        }

        private void occupyBox(Box box) {
            pageBoxes.add(box);
            for (int r = 0; r < box.getHeight(); r++) {
                for (int c = 0; c < box.getWidth(); c++) {
                    boxOccupancyMatrix[box.getRow() + r][box.getColumn() + c] = true;
                }
            }
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



        Optional<Page> build() {
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
