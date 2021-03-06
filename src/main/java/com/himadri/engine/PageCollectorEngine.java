package com.himadri.engine;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.himadri.dto.UserRequest;
import com.himadri.engine.ItemCategorizerEngine.CsvItemGroup;
import com.himadri.engine.ItemCategorizerEngine.CsvProductGroup;
import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.Page;
import com.himadri.model.service.UserSession;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.himadri.dto.ErrorItem.ErrorCategory.FORMATTING;
import static com.himadri.dto.ErrorItem.Severity.ERROR;
import static com.himadri.dto.ErrorItem.Severity.WARN;
import static com.himadri.renderer.PageRenderer.BOX_COLUMNS_PER_PAGE;
import static com.himadri.renderer.PageRenderer.BOX_ROWS_PER_PAGE;
import static com.himadri.renderer.Util.trueValueSet;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang3.StringUtils.defaultString;

@Component
public class PageCollectorEngine {
    @Autowired
    ItemToBoxConverter itemToBoxConverter;

    @Autowired
    Cache<String, UserSession> userSessionCache;

    public List<Page> createPages(List<CsvProductGroup> productGroups, UserRequest userRequest) {
        Box headerImageBox = itemToBoxConverter.createImageBox(userRequest.getHeaderImageStream(),
                userRequest.isWideHeaderImage(), userRequest, "fejléc");
        Box footerImageBox = itemToBoxConverter.createImageBox(userRequest.getFooterImageStream(),
                userRequest.isWideFooterImage(), userRequest, "lábléc");

        PageListFactory pageListFactory = new PageListFactory(productGroups, userRequest, itemToBoxConverter, userSessionCache, headerImageBox);
        List<Page> pages = pageListFactory.createPages(null, 0);
        if (footerImageBox != null) {
            PageListFactory newPageListFactory = new PageListFactory(productGroups, userRequest, itemToBoxConverter, userSessionCache, headerImageBox);
            List<Page> newPages = newPageListFactory.createPages(footerImageBox, pages.size());
            if (newPages.size() > pages.size()) {
                userSessionCache.getIfPresent(userRequest.getRequestId()).addErrorItem(WARN, FORMATTING,
                        "A láblécképet nem sikerült az utolsó oldalra tenni, mivel az eredeti utolsó oldal " +
                                "telített volt. Így a lábléckép az utolsó előtti oldalra került.");
            }
            return newPages;
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
        private final Box headerImageBox;

        public PageListFactory(List<CsvProductGroup> productGroups, UserRequest userRequest,
                               ItemToBoxConverter itemToBoxConverter, Cache<String, UserSession> userSessionCache, Box headerImageBox) {
            this.productGroups = productGroups;
            this.userRequest = userRequest;
            this.itemToBoxConverter = itemToBoxConverter;
            this.userSessionCache = userSessionCache;
            this.headerImageBox = headerImageBox;
        }

        public List<Page> createPages(Box footerImageBox, int lastPageNumber) {

            createNewPageBuilder(footerImageBox, lastPageNumber);
            if (headerImageBox != null) {
                boolean added = currentPageBuilder.addBoxToPage(headerImageBox);
                if (!added) {
                    userSessionCache.getIfPresent(userRequest.getRequestId()).addErrorItem(ERROR, FORMATTING,
                        "Nem sikerült a fejlécképet hozzáadni, mivel a túl magas kép egy oldalonhoz.");
                }
            }
            for (int indexOfProductGroup = 0; indexOfProductGroup < productGroups.size(); indexOfProductGroup++) {
                CsvProductGroup productGroup = productGroups.get(indexOfProductGroup);
                for (CsvItemGroup csvItemGroup : productGroup.getItemGroups()) {
                    final boolean wideBox = csvItemGroup.getItems().stream()
                        .anyMatch(i -> trueValueSet.contains(defaultString(i.getNagykep()).toLowerCase()));
                    final List<Box> itemBoxes = itemToBoxConverter.createArticleBox(csvItemGroup, indexOfProductGroup,
                        productGroup, userRequest, currentPageBuilder.getAvailableBoxHeights(wideBox ? BOX_COLUMNS_PER_PAGE : 1));
                    for (Box box : itemBoxes) {
                        final boolean added = currentPageBuilder.addBoxToPage(box);
                        if (!added) {
                            addCurrentBuilderToPage();
                            createNewPageBuilder(footerImageBox, lastPageNumber);
                            final boolean addedToNewPage = currentPageBuilder.addBoxToPage(box);
                            if (!addedToNewPage) {
                                userSessionCache.getIfPresent(userRequest.getRequestId()).addErrorItem(ERROR, FORMATTING,
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
                boolean added = currentPageBuilder.addBoxToBottom(footerImageBox);
                if (!added) {
                    userSessionCache.getIfPresent(userRequest.getRequestId()).addErrorItem(ERROR, FORMATTING,
                        "Nem sikerült a láblécképet hozzáadni, mivel a túl magas kép egy oldalonhoz.");
                }
            }
        }

        private void addCurrentBuilderToPage() {
            final Optional<Page> newPage = currentPageBuilder.build();
            newPage.ifPresent(pageList::add);
        }
    }

    static class PageBuilder {
        private final String title;
        private int row;
        private int column;
        private List<Box> pageBoxes = new ArrayList<>();
        private final boolean[][] boxOccupancyMatrix;
        private final int pageNumber;

        PageBuilder(String title, int pageNumber) {
            this.title = title;
            this.pageNumber = pageNumber;
            boxOccupancyMatrix = new boolean[BOX_ROWS_PER_PAGE][];
            for (int i = 0; i < BOX_ROWS_PER_PAGE; i++) {
                boxOccupancyMatrix[i] = new boolean[BOX_COLUMNS_PER_PAGE];
            }
        }

        @VisibleForTesting
        PageBuilder(String title, int pageNumber, int column, int row, boolean[][] boxOccupancyMatrix) {
            this.title = title;
            this.boxOccupancyMatrix = boxOccupancyMatrix;
            this.pageNumber = pageNumber;
            this.row = row;
            this.column = column;
        }

        int getPageNumber() {
            return pageNumber;
        }

        boolean addBoxToPage(Box box) {
            if (!searchNextEmptySpot(box) || forcedNewPage(box)) {
                return false;
            }
            box.setDimensions(row, column);
            occupyBox(box);
            return true;
        }

        private boolean forcedNewPage(Box box) {
            return BooleanUtils.isTrue(box.getOnNewPage()) &&
                    pageBoxes.stream().anyMatch(it -> it.getBoxType() == Box.Type.ARTICLE);
        }

        boolean addBoxToBottom(Box box) {
            if (BOX_ROWS_PER_PAGE < box.getHeight() || BOX_COLUMNS_PER_PAGE < box.getWidth()) {
                return false;
            }
            final int row = BOX_ROWS_PER_PAGE - box.getHeight();
            final int column = BOX_COLUMNS_PER_PAGE - box.getWidth();
            box.setDimensions(row, column);
            occupyBox(box);
            return true;
        }

        private void occupyBox(Box box) {
            pageBoxes.add(box);
            for (int r = 0; r < box.getHeight(); r++) {
                for (int c = 0; c < box.getWidth(); c++) {
                    boxOccupancyMatrix[box.getRow() + r][box.getColumn() + c] = true;
                }
            }
        }

        private boolean searchNextEmptySpot(Box box) {
            for (int c = column; c < BOX_COLUMNS_PER_PAGE; c++) {
                for (int r = (c == column ? row : 0); r < BOX_ROWS_PER_PAGE; r++) {
                    if (fitsInPlace(c, r, box.getWidth(), box.getHeight())) {
                        row = r;
                        column = c;
                        return true;
                    }
                }
            }
            return false;
        }

        @VisibleForTesting
        List<Integer> getAvailableBoxHeights(int width) {
            final List<Integer> availableBoxHeights = new ArrayList<>();
            for (int c = column; c < BOX_COLUMNS_PER_PAGE - width + 1; c++) {
                int currentHeight = 0;
                for (int r = (c == column ? row : 0); r < BOX_ROWS_PER_PAGE; r++) {
                    if (fitsInPlace(c, r, width, 1)) {
                        currentHeight++;
                    } else {
                        if (currentHeight > 0) {
                            availableBoxHeights.add(currentHeight);
                            currentHeight = 0;
                        }
                    }
                }
                if (currentHeight > 0) {
                    availableBoxHeights.add(currentHeight);
                }
            }
            return availableBoxHeights;
        }

        private boolean fitsInPlace(int column, int row, int width, int height) {
            if ( width > BOX_COLUMNS_PER_PAGE - column ||
                height > BOX_ROWS_PER_PAGE - row) {
                return false;
            }
            for (int c = 0; c < width; c++) {
                for (int r = 0; r < height; r++) {
                    if (boxOccupancyMatrix[r + row][c + column]) {
                        return false;
                    }
                }
            }
            return true;
        }


        Optional<Page> build() {
            if (pageBoxes.isEmpty()) {
                return empty();
            }
            final String firstBoxProductGroup = pageBoxes
                    .stream()
                    .filter(b -> b.getBoxType() == Box.Type.ARTICLE)
                    .map(Box::getProductGroup)
                    .findFirst()
                    .orElse("");
            return of(new Page(title, firstBoxProductGroup, pageNumber,
                    pageNumber % 2 == 0 ? Page.Orientation.LEFT : Page.Orientation.RIGHT, pageBoxes));
        }


    }
}
