package com.himadri.engine;

import com.google.common.cache.Cache;
import com.himadri.dto.ErrorItem;
import com.himadri.dto.Quality;
import com.himadri.dto.UserRequest;
import com.himadri.engine.ItemCategorizerEngine.CsvItemGroup;
import com.himadri.engine.ItemCategorizerEngine.CsvProductGroup;
import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.Page;
import com.himadri.model.service.UserSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.awt.*;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.of;
import static com.himadri.model.rendering.Box.Type.ARTICLE;
import static com.himadri.model.rendering.Box.Type.IMAGE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PageCollectorEngineTest {
    private PageCollectorEngine sut;

    @Mock
    private ItemToBoxConverter itemToBoxConverter;

    @Mock
    private Cache<String, UserSession> userSessionCache;

    @Mock
    private UserSession userSession;

    @Before
    public void setUp() {
        sut = new PageCollectorEngine();
        sut.itemToBoxConverter = itemToBoxConverter;
        sut.userSessionCache = userSessionCache;
        when(userSessionCache.getIfPresent(any())).thenReturn(userSession);
    }

    @Test
    public void testSingleBoxes() throws Exception {
        when(itemToBoxConverter.createArticleBox(any(), anyInt(), any(), any(), any()))
                .thenReturn(createNarrowBoxesWithSize('A', ARTICLE, IntStream.generate(() -> 1).limit(17).toArray()));
        final List<Page> pages = sut.createPages(createDummyItemGroups(1, 1),
                 createUserRequest());
        assertEquals(2, pages.size());
        assertEquals(16, pages.get(0).getBoxes().size());
        assertEquals(1, pages.get(1).getBoxes().size());
        assertBoxPosition(0, 0, "A0", pages.get(0).getBoxes().get(0));
        assertBoxPosition(1, 0, "A1", pages.get(0).getBoxes().get(1));
        assertBoxPosition(7, 0, "A7", pages.get(0).getBoxes().get(7));
        assertBoxPosition(0, 1, "A8", pages.get(0).getBoxes().get(8));
        assertBoxPosition(1, 1, "A9", pages.get(0).getBoxes().get(9));
        assertBoxPosition(7, 1, "A15", pages.get(0).getBoxes().get(15));
        assertBoxPosition(0, 0, "A16", pages.get(1).getBoxes().get(0));
    }

    @Test
    public void testBigBoxes() throws Exception {
        when(itemToBoxConverter.createArticleBox(any(), anyInt(), any(), any(), any()))
                .thenReturn(createNarrowBoxesWithSize('A', ARTICLE, 5, 5, 4, 4, 2 ));
        final List<Page> pages = sut.createPages(createDummyItemGroups(1, 1),
                createUserRequest());
        assertEquals(2, pages.size());
        assertEquals(2, pages.get(0).getBoxes().size());
        assertEquals(3, pages.get(1).getBoxes().size());
        assertBoxPosition(0, 0, "A0", pages.get(0).getBoxes().get(0));
        assertBoxPosition(0, 1, "A1", pages.get(0).getBoxes().get(1));
        assertBoxPosition(0, 0, "A2", pages.get(1).getBoxes().get(0));
        assertBoxPosition(4, 0, "A3", pages.get(1).getBoxes().get(1));
        assertBoxPosition(0, 1, "A4", pages.get(1).getBoxes().get(2));
    }

    @Test
    public void testWideBoxes() throws Exception {
        when(itemToBoxConverter.createArticleBox(any(), anyInt(), any(), any(), any()))
                .thenReturn(
                        createWideBoxesWithSize('A', ARTICLE, 2, 1 ),
                        createNarrowBoxesWithSize('B', ARTICLE, 3, 4)
                );
        // PAGE 1
        // 0: | A0 | A0 |
        // 1: | A0 | A0 |
        // 2: | A1 | A1 |
        // 3: | B0 | B1 |
        // 4: | B0 | B1 |
        // 5: | B0 | B1 |
        // 6: |    | B1 |
        // 7: |    |    |

        final List<Page> pages = sut.createPages(createDummyItemGroups(1, 2),
                createUserRequest());
        assertEquals(1, pages.size());
        assertEquals(4, pages.get(0).getBoxes().size());
        assertBoxPosition(0, 0, "A0", pages.get(0).getBoxes().get(0));
        assertBoxPosition(2, 0, "A1", pages.get(0).getBoxes().get(1));
        assertBoxPosition(3, 0, "B0", pages.get(0).getBoxes().get(2));
        assertBoxPosition(3, 1, "B1", pages.get(0).getBoxes().get(3));
    }

    @Test
    public void testWithHeaderAndFooter() throws Exception {
        when(itemToBoxConverter.createImageBox(any(), anyBoolean(), any(), eq("fejléc")))
            .thenReturn(
                createNarrowBoxesWithSize('H', IMAGE, 1).get(0)
            );
        when(itemToBoxConverter.createImageBox(any(), anyBoolean(), any(), eq("lábléc")))
            .thenReturn(
                createWideBoxesWithSize('F', IMAGE,1).get(0)
            );
        when(itemToBoxConverter.createArticleBox(any(), anyInt(), any(), any(), any()))
            .thenReturn(
                createWideBoxesWithSize('A', ARTICLE, 1, 1 ),
                createNarrowBoxesWithSize('B', ARTICLE, 3, 4),
                createWideBoxesWithSize('A', ARTICLE, 1, 1 ),
                createNarrowBoxesWithSize('B', ARTICLE, 3, 4)
            );
        // PAGE 1
        // 0: | H0 |    |
        // 1: | A0 | A0 |
        // 2: | A1 | A1 |
        // 3: | B0 | B1 |
        // 4: | B0 | B1 |
        // 5: | B0 | B1 |
        // 6: |    | B1 |
        // 7: | F0 | F0 |

        final List<Page> pages = sut.createPages(createDummyItemGroups(1, 2),
            createUserRequest());
        assertEquals(1, pages.size());
        assertEquals(6, pages.get(0).getBoxes().size());
        assertBoxPosition(7, 0, "F0", pages.get(0).getBoxes().get(0));
        assertBoxPosition(0, 0, "H0", pages.get(0).getBoxes().get(1));
        assertBoxPosition(1, 0, "A0", pages.get(0).getBoxes().get(2));
        assertBoxPosition(2, 0, "A1", pages.get(0).getBoxes().get(3));
        assertBoxPosition(3, 0, "B0", pages.get(0).getBoxes().get(4));
        assertBoxPosition(3, 1, "B1", pages.get(0).getBoxes().get(5));
    }

    @Test
    public void testWithHeaderAndFooterWithNewPage() throws Exception {
        when(itemToBoxConverter.createImageBox(any(), anyBoolean(), any(), eq("fejléc")))
            .thenReturn(
                createWideBoxesWithSize('H', IMAGE, 2).get(0)
            );
        when(itemToBoxConverter.createImageBox(any(), anyBoolean(), any(), eq("lábléc")))
            .thenReturn(
                createNarrowBoxesWithSize('F', IMAGE,2).get(0)
            );
        when(itemToBoxConverter.createArticleBox(any(), anyInt(), any(), any(), any()))
            .thenReturn(
                createWideBoxesWithSize('A', ARTICLE, 1 ),
                createNarrowBoxesWithSize('B', ARTICLE, 3, 4),
                createWideBoxesWithSize('A', ARTICLE, 1 ),
                createNarrowBoxesWithSize('B', ARTICLE, 3, 4)
            );
        // PAGE 1
        // 0: | H0 | H0 |
        // 1: | H0 | H0 |
        // 2: | A0 | A0 |
        // 3: | B0 |    |
        // 4: | B0 |    |
        // 5: | B0 |    |
        // 6: |    | F0 |
        // 7: |    | F0 |

        // PAGE 2
        // 0: | B1 |    |
        // 1: | B1 |    |
        // 2: | B1 |    |
        // 3: | B1 |    |
        // 4: |    |    |
        // 5: |    |    |
        // 6: |    |    |
        // 7: |    |    |


        final List<Page> pages = sut.createPages(createDummyItemGroups(1, 2),
            createUserRequest());
        assertEquals(2, pages.size());
        assertEquals(4, pages.get(0).getBoxes().size());
        assertBoxPosition(6, 1, "F0", pages.get(0).getBoxes().get(0));
        assertBoxPosition(0, 0, "H0", pages.get(0).getBoxes().get(1));
        assertBoxPosition(2, 0, "A0", pages.get(0).getBoxes().get(2));
        assertBoxPosition(3, 0, "B0", pages.get(0).getBoxes().get(3));
        assertEquals(1, pages.get(1).getBoxes().size());
        assertBoxPosition(0, 0, "B1", pages.get(1).getBoxes().get(0));

    }


    @Test
    public void testWideOnNewPageBoxes() throws Exception {
        when(itemToBoxConverter.createArticleBox(any(), anyInt(), any(), any(), any()))
                .thenReturn(
                        createWideBoxesWithSize('A', ARTICLE, 2),
                        createNarrowBoxesWithSize('B', ARTICLE, 1),
                        createWideBoxesWithSize('C', ARTICLE, 2)
                );
        // PAGE 1
        // 0: | A0 | A0 |
        // 1: | A0 | A0 |
        // 2: | B0 |    |
        // 3: | C0 | C0 |
        // 4: | C0 | C0 |


        final List<Page> pages = sut.createPages(createDummyItemGroups(3, 1),
                createUserRequest());
        assertEquals(1, pages.size());
        assertEquals(3, pages.get(0).getBoxes().size());
        assertBoxPosition(0, 0, "A0", pages.get(0).getBoxes().get(0));
        assertBoxPosition(2, 0, "B0", pages.get(0).getBoxes().get(1));
        assertBoxPosition(3, 0, "C0", pages.get(0).getBoxes().get(2));
    }

    @Test
    public void testTooBigBox() throws Exception {
        when(itemToBoxConverter.createArticleBox(any(), anyInt(), any(), any(), any()))
                .thenReturn(createNarrowBoxesWithSize('A', ARTICLE, 3, 9 ));
        final List<Page> pages = sut.createPages(createDummyItemGroups(1, 1),createUserRequest());
        assertEquals(1, pages.size());
        assertEquals(1, pages.get(0).getBoxes().size());
        assertBoxPosition(0, 0, "A0", pages.get(0).getBoxes().get(0));
        verify(userSession).addErrorItem(eq(ErrorItem.Severity.ERROR), eq(ErrorItem.ErrorCategory.FORMATTING), anyString());
    }

    @Test
    public void testOnlyWideBoxes() throws Exception {
        when(itemToBoxConverter.createArticleBox(any(), anyInt(), any(), any(), any()))
                .thenReturn(createWideBoxesWithSize('A', ARTICLE, 3, 3, 3, 6 ));
        final List<Page> pages = sut.createPages(createDummyItemGroups(1, 1), createUserRequest());
        assertEquals(3, pages.size());
        assertEquals(2, pages.get(0).getBoxes().size());
        assertEquals(1, pages.get(1).getBoxes().size());
        assertEquals(1, pages.get(2).getBoxes().size());
        assertBoxPosition(0, 0, "A0", pages.get(0).getBoxes().get(0));
        assertBoxPosition(3, 0, "A1", pages.get(0).getBoxes().get(1));
        assertBoxPosition(0, 0, "A2", pages.get(1).getBoxes().get(0));
        assertBoxPosition(0, 0, "A3", pages.get(2).getBoxes().get(0));
    }

    @Test
    public void testAvailableHeights() {
        // 0: |   |   |
        // 1: | X | X |
        // 2: | X |   |
        // 3: |   |   |
        // 4: | X | X |
        // 5: |   | X |
        // 6: |   |   |
        // 7: |   |   |

        boolean[][] boxOccupancyMatrix = {
            new boolean[]{false, false},
            new boolean[]{true, true},
            new boolean[]{true, false},
            new boolean[]{false, false},
            new boolean[]{true, true},
            new boolean[]{false, true},
            new boolean[]{false, false},
            new boolean[]{false, false}
        };
        PageCollectorEngine.PageBuilder pageBuilder00 = new PageCollectorEngine.PageBuilder("title", 0, 0, 0, boxOccupancyMatrix);
        assertEquals(of(1, 1, 3, 1, 2, 2), pageBuilder00.getAvailableBoxHeights(1));
        assertEquals(of(1, 1, 2), pageBuilder00.getAvailableBoxHeights(2));
        assertEquals(of(), pageBuilder00.getAvailableBoxHeights(3));

        PageCollectorEngine.PageBuilder pageBuilde01 = new PageCollectorEngine.PageBuilder("title", 0, 0, 1, boxOccupancyMatrix);
        assertEquals(of(1, 3, 1, 2, 2), pageBuilde01.getAvailableBoxHeights(1));
        assertEquals(of(1, 2), pageBuilde01.getAvailableBoxHeights(2));

        PageCollectorEngine.PageBuilder pageBuilde05 = new PageCollectorEngine.PageBuilder("title", 0, 0, 5, boxOccupancyMatrix);
        assertEquals(of(3, 1, 2, 2), pageBuilde05.getAvailableBoxHeights(1));
        assertEquals(of(2), pageBuilde05.getAvailableBoxHeights(2));

        PageCollectorEngine.PageBuilder pageBuilde13 = new PageCollectorEngine.PageBuilder("title", 0, 1, 3, boxOccupancyMatrix);
        assertEquals(of(1, 2), pageBuilde13.getAvailableBoxHeights(1));
        assertEquals(of(), pageBuilde13.getAvailableBoxHeights(2));
    }

    private void assertBoxPosition(int expectedRow, int expectedColumn, String expectedTitle, Box box) {
        assertEquals(expectedRow, box.getRow());
        assertEquals(expectedColumn, box.getColumn());
        assertEquals(expectedTitle, box.getTitle());
    }


    private List<Box> createNarrowBoxesWithSize(char namePrefix, Box.Type type, int... sizes) {
        return IntStream.range(0, sizes.length)
                .mapToObj(i -> createBoxWithSize(namePrefix, i, 1, sizes[i], type))
                .collect(Collectors.toList());
    }

    private List<Box> createWideBoxesWithSize(char namePrefix, Box.Type type, int... sizes) {
        return IntStream.range(0, sizes.length)
                .mapToObj(i -> createBoxWithSize(namePrefix, i, 2, sizes[i], type))
                .collect(Collectors.toList());
    }

    private Box createBoxWithSize(char namePrefix, int index, int width, int height, Box.Type type) {
        return new Box(null, null, namePrefix + Integer.toString(index), null, "group", Color.blue, width, height, false, false, null, null, type);
    }

    private UserRequest createUserRequest() {
        return new UserRequest("requestId", mock(InputStream.class), "title", Quality.DRAFT, false, false, null, false,null, false);
    }

    private List<CsvProductGroup> createDummyItemGroups(int nbOfGroups, int nbOfItemsEach) {
        return Stream.generate(() -> new CsvProductGroup("group", Optional.of("color"), createDummyItems(nbOfItemsEach)))
                .limit(nbOfGroups).collect(Collectors.toList());
    }

    private List<CsvItemGroup> createDummyItems(int nbOfItems) {
        return Stream.generate(() -> new CsvItemGroup(Collections.emptyList()))
                .limit(nbOfItems).collect(Collectors.toList());
    }
}