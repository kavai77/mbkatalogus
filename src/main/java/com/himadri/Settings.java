package com.himadri;

import java.io.File;

public class Settings {
    public static final File IMAGE_LOCATION = new File("/Users/himadri/Projects/MBKatalogus/cikkek/");
    public static final File LOGO_IMAGE_LOCATION = new File("/Users/himadri/Projects/MBKatalogus/logok/");
    public static final File RENDERING_LOCATION = new File("/Users/himadri/Projects/MBKatalogus/render/");

    static {
        RENDERING_LOCATION.mkdirs();
    }
}
