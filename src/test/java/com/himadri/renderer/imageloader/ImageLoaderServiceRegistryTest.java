package com.himadri.renderer.imageloader;

import com.himadri.dto.Quality;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ImageLoaderServiceRegistryTest {
    final ImageLoaderServiceRegistry sut = new ImageLoaderServiceRegistry();

    @Before
    public void setUp() throws Exception {
        sut.imageLocation = ".";
        sut.logoImageLocation = ".";
        sut.webImageCacheLocation = ".";
        sut.init();
    }

    @Test
    public void testEachQualityIsCovered() throws Exception {
        for (Quality quality: Quality.values()) {
            assertNotNull("There is no imageLoader for quality: " + quality, sut.getImageLoader(quality));
        }
    }

}