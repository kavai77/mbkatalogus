package com.himadri;

import java.io.File;

public class Settings {
    public static final File IMAGE_LOCATION = new File("/Users/himadri/Projects/MBKatalogus/cikkek/");
    public static final File CSV_LOCATION = new File("/Users/himadri/Projects/MBKatalogus/katalogus.csv");
    public static final File RENDERING_LOCATION = new File("/Users/himadri/Projects/MBKatalogus/render/");
    public static final boolean DISABLE_IMAGES = true;

    static {
        RENDERING_LOCATION.mkdirs();
    }
}
