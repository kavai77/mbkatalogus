package com.himadri;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;

import static org.apache.commons.lang3.StringUtils.appendIfMissing;

@SpringBootApplication
public class App extends WebMvcConfigurerAdapter {

    @Value("${renderingLocation}")
    private String renderingLocation;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/webapp/");
        registry.addResourceHandler("/render/*").addResourceLocations("file://" +
                appendIfMissing(new File(renderingLocation).getAbsolutePath(), "/"));
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(App.class)
                .initializers(new YamlFileApplicationContextInitializer())
                .run(args);
    }


}
