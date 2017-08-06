package com.himadri.renderer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class LogoImageCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogoImageCache.class);
    private LoadingCache<File, BufferedImage> logoImageCache;

    @PostConstruct
    public void init() {
        logoImageCache = CacheBuilder.newBuilder()
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build(new CacheLoader<File, BufferedImage>() {
                            public BufferedImage load(File file) throws IOException {
                                LOGGER.info("Loading logo file: " + file);
                                return ImageIO.read(file);
                            }
                        });
    }

    public BufferedImage getLogoImage(File file) throws IOException {
        try {
            return logoImageCache.get(file);
        } catch (ExecutionException e) {
            throw (IOException) e.getCause();
        }
    }
}
