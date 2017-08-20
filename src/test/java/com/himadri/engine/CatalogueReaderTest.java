package com.himadri.engine;

import org.junit.Test;
import org.supercsv.comment.CommentMatcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CatalogueReaderTest {
    @Test
    public void commentMatcherTest() throws Exception {
        final CommentMatcher commentMatcher = CatalogueReader.EXCEL_NORTH_EUROPE_PREFERENCE.getCommentMatcher();
        assertTrue(commentMatcher.isComment(";"));
        assertTrue(commentMatcher.isComment(";;;;;"));
        assertFalse(commentMatcher.isComment(""));
        assertFalse(commentMatcher.isComment(";;;;;abc"));
        assertFalse(commentMatcher.isComment(";;;;;abc;"));
    }

}