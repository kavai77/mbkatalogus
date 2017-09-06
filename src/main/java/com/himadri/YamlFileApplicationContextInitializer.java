package com.himadri;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.IOException;

public class YamlFileApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            Resource resource = applicationContext.getResource("classpath:localization.yaml");
            YamlPropertySourceLoader sourceLoader = new YamlPropertySourceLoader();
            PropertySource<?> localizationProperties = sourceLoader.load("localizationProperties", resource, null);
            applicationContext.getEnvironment().getPropertySources().addLast(localizationProperties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
