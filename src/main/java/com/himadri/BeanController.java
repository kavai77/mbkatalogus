package com.himadri;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.himadri.engine.CatalogueReader;
import com.himadri.engine.CatalogueReaderWithOpenCsv;
import com.himadri.engine.ItemCategorizerEngine;
import com.himadri.model.service.UserSession;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.fontbox.ttf.OTFParser;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class BeanController {
    private static final String FONT_RESOURCE_PATH = "/fonts/";
    private final Map<String, TTFParser> fontExtensionParser = ImmutableMap.of(
            "ttf", new TTFParser(),
            "otf", new OTFParser());

    @Value("${itemCategorizerEngineClass}")
    private String itemCategorizerEngineClass;

    @Bean
    public CatalogueReader catalogueReader() {
        return new CatalogueReaderWithOpenCsv();
    }

    @Bean
    public ItemCategorizerEngine itemCategorizerEngine() throws Exception {
        return (ItemCategorizerEngine) Class.forName(itemCategorizerEngineClass).newInstance();
    }

    @Bean
    public Cache<String, UserSession> userSessionCache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build();
    }

    @Bean
    public LoadingCache<String, TrueTypeFont> trueTypeFontLoadingCache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build(new CacheLoader<String, TrueTypeFont>() {
                    @Override
                    public TrueTypeFont load(String key) throws Exception {
                        for (Map.Entry<String, TTFParser> entry: fontExtensionParser.entrySet()) {
                            String filePath = FONT_RESOURCE_PATH + key + "." + entry.getKey();
                            if (BeanController.class.getResource(filePath) != null) {
                                return entry.getValue().parse(BeanController.class.getResourceAsStream(filePath));
                            }
                        }
                        throw new RuntimeException("Could not load extension + " + key);
                    }
                });
    }

    @Bean
    public LoadingCache<PDDocumentTrueTypeFont, PDFont> pdFontLoadingCache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build(new CacheLoader<PDDocumentTrueTypeFont, PDFont>() {
                    @Override
                    public PDFont load(PDDocumentTrueTypeFont key) throws Exception {
                        return PDType0Font.load(key.pdDocument, key.trueTypeFont, true);
                    }
                });
    }

    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    public static class PDDocumentTrueTypeFont {
        private final PDDocument pdDocument;
        private final TrueTypeFont trueTypeFont;

        public PDDocumentTrueTypeFont(PDDocument pdDocument, TrueTypeFont trueTypeFont) {
            this.pdDocument = pdDocument;
            this.trueTypeFont = trueTypeFont;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            PDDocumentTrueTypeFont that = (PDDocumentTrueTypeFont) o;

            return new EqualsBuilder()
                    .append(pdDocument, that.pdDocument)
                    .append(trueTypeFont, that.trueTypeFont)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                    .append(pdDocument)
                    .append(trueTypeFont)
                    .toHashCode();
        }
    }
}
