package com.himadri;

import com.himadri.csv.CatalogueBean;
import com.himadri.csv.CatalogueReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
@Controller
public class AppController extends WebMvcConfigurerAdapter {
    @Autowired
    private RenderingTest renderingTest;

    @Autowired
    private CatalogueReader catalogueReader;

    @RequestMapping("/testRendering")
    @ResponseBody
    public String testRendering() throws IOException {
        final String file = renderingTest.testRendering();
        return String.format("<a href=\"/render/%s\" target=\"_blank\">Rendered PDF</a>", file);
    }

    @RequestMapping("/testCsvParsing")
    @ResponseBody
    public List<CatalogueBean> testCsvParsing() throws IOException {
        return catalogueReader.readWithCsvBeanReader();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/render/*").addResourceLocations("file://" +
                Settings.RENDERING_LOCATION.getAbsolutePath() + "/");
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(AppController.class, args);
    }


}
