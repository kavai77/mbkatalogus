package com.himadri;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class AppController extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/render/*").addResourceLocations("file://" +
                Settings.RENDERING_LOCATION.getAbsolutePath() + "/");
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(AppController.class, args);
    }


}
