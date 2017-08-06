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
    private LoadingCache<File, FileImage> logoImageCache;

    @PostConstruct
    public void init() {
        logoImageCache = CacheBuilder.newBuilder()
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build(new CacheLoader<File, FileImage>() {
                            public FileImage load(File file) throws IOException {
                                LOGGER.info("Loading logo file: " + file);
                                return new FileImage(ImageIO.read(file), file.lastModified());
                            }
                        });
    }

    public BufferedImage getLogoImage(File file) throws IOException {
        try {
            FileImage fileImage = logoImageCache.get(file);
            if (file.lastModified() != fileImage.getLastModified()) {
                logoImageCache.invalidate(file);
                return logoImageCache.get(file).getImage();
            } else {
                return fileImage.getImage();
            }
        } catch (ExecutionException e) {
            throw (IOException) e.getCause();
        }
    }

    private static class FileImage {
        private final BufferedImage image;
        private final long lastModified;

        public FileImage(BufferedImage image, long lastModified) {
            this.image = image;
            this.lastModified = lastModified;
        }

        public BufferedImage getImage() {
            return image;
        }

        public long getLastModified() {
            return lastModified;
        }
    }
}
