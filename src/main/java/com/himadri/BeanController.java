package com.himadri;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.himadri.model.service.UserSession;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
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
    public LoadingCache<String, TrueTypeFont> trueTypeFontLoadingCache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build(new CacheLoader<String, TrueTypeFont>() {
                    @Override
                    public TrueTypeFont load(String key) throws Exception {
                        return new TTFParser().parse(BeanController.class.getResourceAsStream(key));
                    }
                });
    }
}
