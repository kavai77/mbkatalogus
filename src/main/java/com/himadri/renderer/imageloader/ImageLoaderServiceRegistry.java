package com.himadri.renderer.imageloader;

import com.himadri.dto.Quality;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.EnumMap;

@Component
public class ImageLoaderServiceRegistry {
    private final EnumMap<Quality, ImageLoader> qualityImageLoaderMap = new EnumMap<>(Quality.class);

    @Value("${imageLocation}")
    String imageLocation;

    @Value("${webImageURLPrefix}")
    String webImageURLPrefix;

    @Value("${webImageCacheLocation}")
    String webImageCacheLocation;

    @Value("${renderingLocation}")
    String renderingLocation;

    @Value("${logoImageLocation}")
    String logoImageLocation;

    @PostConstruct
    public void init() {
        validateDirectory(imageLocation, "imageLocation");
        validateDirectory(logoImageLocation, "logoImageLocation");
        validateDirectory(webImageCacheLocation, "webImageCacheLocation");

        qualityImageLoaderMap.put(Quality.DRAFT, new DraftImageLoader());
        qualityImageLoaderMap.put(Quality.WEB, new WebImageLoader(webImageURLPrefix, webImageCacheLocation, logoImageLocation));
        qualityImageLoaderMap.put(Quality.PRESS, new PressImageLoader(imageLocation, logoImageLocation, renderingLocation));
    }

    private void validateDirectory(String location, String locationDescription) {
        File locationFile = new File(location);
        if (!locationFile.exists() || !locationFile.isDirectory()) {
            throw new RuntimeException(String.format("The configured path for %s does not exist: %s",
                    locationDescription, location));
        }
    }

    public ImageLoader getImageLoader(Quality quality) {
        return qualityImageLoaderMap.get(quality);
    }
}
