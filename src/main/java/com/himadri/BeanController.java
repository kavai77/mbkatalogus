package com.himadri;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.himadri.model.UserSession;
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
}
