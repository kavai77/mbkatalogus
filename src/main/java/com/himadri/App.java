package com.himadri;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class App extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/webapp/");
        registry.addResourceHandler("/render/*").addResourceLocations("file://" +
                Settings.RENDERING_LOCATION.getAbsolutePath() + "/");
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(App.class, args);
    }


}
