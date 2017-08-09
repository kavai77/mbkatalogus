package com.himadri.engine;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ItemToBoxConverterTest {
    @Test
    public void stripEnd() throws Exception {
        assertEquals("abc", StringUtils.stripEnd("abc ;", " ;"));
        System.out.println(Integer.MAX_VALUE);
    }
}