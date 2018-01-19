package com.himadri.renderer.imageloader;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.himadri.exception.ImageNotFoundException;
import com.himadri.model.rendering.Box;
import com.himadri.model.rendering.Item;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.*;

public class WebImageLoader implements ImageLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebImageLoader.class);

    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    private final String webImageURLPrefix;
    private final String webImageCacheLocation;
    private final String logoImageLocation;
    private final LoadingCache<LogoImageKey, PDImageXObject> logoImageCache;

    public WebImageLoader(String webImageURLPrefix, String webImageCacheLocation, String logoImageLocation) {
        this.webImageURLPrefix = webImageURLPrefix;
        this.webImageCacheLocation = webImageCacheLocation;
        this.logoImageLocation = logoImageLocation;

        logoImageCache = CacheBuilder.newBuilder()
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build(new CacheLoader<LogoImageKey, PDImageXObject>() {
                    public PDImageXObject load(LogoImageKey imageKey) throws IOException {
                        LOGGER.info("Loading logo file: {}", imageKey.getLogoImage().getAbsolutePath());
                        final BufferedImage image = ImageIO.read(imageKey.getLogoImage());
                        return JPEGFactory.createFromImage(imageKey.getPdDocument(), image);
                    }
                });
    }

    @Override
    public String getImageName(Item item) {
        return stripToEmpty(item.getWebkepnev());
    }

    @Override
    public PDImageXObject loadImage(Box box, PDDocument document) throws IOException, ImageNotFoundException {
        if (isNotBlank(box.getImage())) {
            final File file = new File(webImageCacheLocation, box.getImage());
            final byte[] content;
            if (file.exists() && file.isFile()) {
                content = FileUtils.readFileToByteArray(file);
            } else {
                content = downloadImageFromWeb(box);
                FileUtils.writeByteArrayToFile(file, content);
            }

            return createPDImageXObject(box, document, content);
        } else {
            return null;
        }
    }

    @Override
    public PDImageXObject loadLogoImage(Box box, PDDocument document) throws IOException, ImageNotFoundException {
        if (isNotBlank(box.getBrandImage())) {
            final File logoImageFile = new File(logoImageLocation, box.getBrandImage());
            if (!logoImageFile.exists() || !logoImageFile.isFile()) {
                throw new ImageNotFoundException();
            }
            try {
                return logoImageCache.get(new LogoImageKey(document, logoImageFile));
            } catch (ExecutionException e) {
                throw (IOException) e.getCause();
            }
        } else {
            return null;
        }
    }

    private byte[] downloadImageFromWeb(Box box) throws IOException, ImageNotFoundException {
        final HttpUriRequest request = new HttpGet(webImageURLPrefix + box.getImage());
        CloseableHttpResponse response = httpclient.execute(request);
        try {
            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                byte[] buf = new byte[(int)entity.getContentLength()];
                IOUtils.readFully(entity.getContent(), buf);
                return buf;
            } else {
                throw new ImageNotFoundException();
            }
        } finally {
            response.close();
        }
    }

    private PDImageXObject createPDImageXObject(Box box, PDDocument document, byte[] content) throws IOException {
        if (endsWithIgnoreCase(box.getImage(), ".jpg")) {
            return JPEGFactory.createFromByteArray(document, content);
        } else {
            final BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
            if (image == null) {
                throw new IOException("ImageIO.read is null");
            }
            return LosslessFactory.createFromImage(document, image);
        }
    }

    @Override
    public MemoryUsageSetting getMemoryUsageSettings() {
        return MemoryUsageSetting.setupMainMemoryOnly();
    }

}
