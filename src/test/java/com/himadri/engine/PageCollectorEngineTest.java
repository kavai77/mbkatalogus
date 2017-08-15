package com.himadri.engine;

import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.Page;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class PageCollectorEngineTest {
    private PageCollectorEngine sut;

    @Before
    public void setUp() throws Exception {
        sut = new PageCollectorEngine();
    }

    @Test
    public void testSingleBoxes() throws Exception {
        final List<Page> pages = sut.createPages(createNBoxesWithSize(IntStream.generate(() -> 1).limit(17).toArray()),
                null);
        assertEquals(2, pages.size());
        assertEquals(16, pages.get(0).getBoxes().size());
        assertEquals(1, pages.get(1).getBoxes().size());
        assertBoxPosition(0, 0, "0", pages.get(0).getBoxes().get(0));
        assertBoxPosition(1, 0, "1", pages.get(0).getBoxes().get(1));
        assertBoxPosition(7, 0, "7", pages.get(0).getBoxes().get(7));
        assertBoxPosition(0, 1, "8", pages.get(0).getBoxes().get(8));
        assertBoxPosition(1, 1, "9", pages.get(0).getBoxes().get(9));
        assertBoxPosition(7, 1, "15", pages.get(0).getBoxes().get(15));
        assertBoxPosition(0, 0, "16", pages.get(1).getBoxes().get(0));
    }

    @Test
    public void testBigBoxes() throws Exception {
        final List<Page> pages = sut.createPages(createNBoxesWithSize(5, 5, 4, 4, 2 ),
                null);
        assertEquals(2, pages.size());
        assertEquals(2, pages.get(0).getBoxes().size());
        assertEquals(3, pages.get(1).getBoxes().size());
        assertBoxPosition(0, 0, "0", pages.get(0).getBoxes().get(0));
        assertBoxPosition(0, 1, "1", pages.get(0).getBoxes().get(1));
        assertBoxPosition(0, 0, "2", pages.get(1).getBoxes().get(0));
        assertBoxPosition(4, 0, "3", pages.get(1).getBoxes().get(1));
        assertBoxPosition(0, 1, "4", pages.get(1).getBoxes().get(2));
    }

    @Test(expected = RuntimeException.class)
    public void testTooBigBox() throws Exception {
        sut.createPages(createNBoxesWithSize(3, 9 ),null);
    }

    private void assertBoxPosition(int expectedRow, int expectedColumn, String expectedTitle, Box box) {
        assertEquals(expectedRow, box.getRow());
        assertEquals(expectedColumn, box.getColumn());
        assertEquals(expectedTitle, box.getTitle());
    }


    private List<Box> createNBoxesWithSize(int... sizes) {
        final List<Box> boxes = new ArrayList<>();
        for (int i = 0; i < sizes.length; i++) {
            boxes.add(createBoxWithSize(i, sizes[i]));
        }
        return boxes;
    }

    private Box createBoxWithSize(int index, int size) {
        return new Box(null, null, Integer.toString(index), null, null, 0, size, null);
    }
}