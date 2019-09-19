package com.himadri.renderer.imageloader;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.himadri.exception.ImageNotFoundException;
import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.CsvItem;
import com.himadri.model.service.UserSession;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.*;

public class PressImageLoader implements ImageLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PressImageLoader.class);
    private final String diskImageLocation;
    private final String logoImageLocation;
    private final String renderingLocation;
    private final LoadingCache<LogoImageKey, PDImageXObject> logoImageCache;
    private final LoadingCache<ResourceImageKey, PDImageXObject> resourceImageCache;


    public PressImageLoader(String diskImageLocation, String logoImageLocation, String renderingLocation) {
        this.diskImageLocation = diskImageLocation;
        this.logoImageLocation = logoImageLocation;
        this.renderingLocation = renderingLocation;

        logoImageCache = CacheBuilder.newBuilder()
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build(new CacheLoader<LogoImageKey, PDImageXObject>() {
                    public PDImageXObject load(LogoImageKey imageKey) throws IOException {
                        LOGGER.info("Loading logo file: {}", imageKey.getLogoImage().getAbsolutePath());
                        final BufferedImage image = ImageIO.read(imageKey.getLogoImage());
                        if (image == null) {
                            throw new IOException("ImageIO.read is null");
                        }
                        imageKey.getUserSession().getTotalLogoImageSize().addAndGet(imageKey.getLogoImage().length());
                        return LosslessFactory.createFromImage(imageKey.getPdDocument(), image);
                    }
                });

        resourceImageCache = CacheBuilder.newBuilder()
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .build(new CacheLoader<ResourceImageKey, PDImageXObject>() {
                public PDImageXObject load(ResourceImageKey resourceImageKey) throws IOException {
                    LOGGER.info("Loading resource file: {}", resourceImageKey.getResource());
                    final BufferedImage image = ImageIO.read(getClass().getResourceAsStream(resourceImageKey.getResource()));
                    if (image == null) {
                        throw new IOException("ImageIO.read is null");
                    }
                    return LosslessFactory.createFromImage(resourceImageKey.getPdDocument(), image);
                }
            });
    }

    @Override
    public String getImageName(CsvItem item) {
        return stripToEmpty(item.getKepnev());
    }

    @Override
    public PDImageXObject loadImage(Box box, PDDocument document, UserSession userSession) throws IOException, ImageNotFoundException {
        if (isBlank(box.getImage())) {
            return null;
        }
        final File imageFile = new File(diskImageLocation, box.getImage());
        if (!imageFile.exists() || !imageFile.isFile()) {
            throw new ImageNotFoundException();
        }
        userSession.getTotalImageSize().addAndGet(imageFile.length());
        try (InputStream fis = new FileInputStream(imageFile)) {
            if (endsWithAny(lowerCase(imageFile.getName()), ".jpg", ".jpeg")) {
                return JPEGFactory.createFromStream(document, fis);
            } else {
                final BufferedImage image = ImageIO.read(fis);
                if (image == null) {
                    throw new IOException("ImageIO.read is null");
                }
                return LosslessFactory.createFromImage(document, image);
            }
        }
    }

    @Override
    public PDImageXObject loadLogoImage(Box box, PDDocument document, UserSession userSession) throws IOException, ImageNotFoundException {
        if (isNotBlank(box.getBrandImage())) {
            final File logoImageFile = new File(logoImageLocation, box.getBrandImage());
            if (!logoImageFile.exists() || !logoImageFile.isFile()) {
                throw new ImageNotFoundException();
            }
            try {
                return logoImageCache.get(new LogoImageKey(document, logoImageFile, userSession));
            } catch (ExecutionException e) {
                throw (IOException) e.getCause();
            }
        } else {
            return null;
        }
    }

    @Override
    public PDImageXObject loadResourceImage(String resource, PDDocument document) throws IOException {
        try {
            return resourceImageCache.get(new ResourceImageKey(resource, document));
        } catch (ExecutionException e) {
            throw (IOException) e.getCause();
        }
    }

    @Override
    public MemoryUsageSetting getMemoryUsageSettings() {
        final MemoryUsageSetting memoryUsageSetting = MemoryUsageSetting.setupTempFileOnly();
        memoryUsageSetting.setTempDir(new File(renderingLocation));
        return memoryUsageSetting;
    }
}
