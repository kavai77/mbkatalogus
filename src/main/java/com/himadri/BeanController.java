package com.himadri;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.himadri.model.service.DocumentFont;
import com.himadri.model.service.UserSession;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

import java.util.concurrent.TimeUnit;

@Controller
public class BeanController {
    @Bean
    public Cache<String, UserSession> userSessionCache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build();
    }

    @Bean
    public LoadingCache<DocumentFont, PDFont> pdFontCache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build(new CacheLoader<DocumentFont, PDFont>() {
                    @Override
                    public PDFont load(DocumentFont key) throws Exception {
                        return PDType0Font.load(key.getPdDocument(), BeanController.class.getResourceAsStream(key.getFontName()));
                    }
                });
    }
}
