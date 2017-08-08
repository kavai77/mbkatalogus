package com.himadri;

import java.io.File;

public class Settings {
    public static final File IMAGE_LOCATION = new File("/Users/himadri/Projects/MBKatalogus/cikkek/");
    public static final File LOGO_IMAGE_LOCATION = new File("/Users/himadri/Projects/MBKatalogus/logok/");
    public static final File RENDERING_LOCATION = new File("/Users/himadri/Projects/MBKatalogus/render/");
    public static final long PDF_DOCUMENT_THRESHOLD = 100_000_000L;
    public static final long PDF_TEXT_ONLY_BYTES_PER_PAGE_ESTIMATE = 600_000L;

    static {
        RENDERING_LOCATION.mkdirs();
    }
}
