package com.himadri;

import com.himadri.engine.CatalogueReader;
import com.himadri.engine.ModelTransformerEngine;
import com.himadri.engine.UserSession;
import com.himadri.model.Item;
import com.himadri.model.Page;
import com.himadri.model.UserRequest;
import com.himadri.renderer.DocumentRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller

public class RestController {
    @Autowired
    private CatalogueReader catalogueReader;

    @Autowired
    private ModelTransformerEngine modelTransformerEngine;

    @Autowired
    private DocumentRenderer documentRenderer;

    @Autowired
    private UserSession userSession;

    @RequestMapping("/csvRendering")
    @ResponseBody
    public String testCsvParsing() throws IOException {
        final UserRequest userRequest = new UserRequest();
        userRequest.setCatalogueName("Kéziszerszám Katalógus");
        userRequest.setLocalCsvFile("/Users/himadri/Projects/MBKatalogus/filtered_katalogus.csv");
        userSession.setUserRequest(userRequest);
        final List<Item> items = catalogueReader.readWithCsvBeanReader();
        final List<Page> pages = modelTransformerEngine.createPagesFromItems(items);
        final List<String> fileNames = documentRenderer.renderDocument(pages);
        return convertFileListToLinks(fileNames);
    }

    @RequestMapping("/unknownImages")
    @ResponseBody
    public String unknownImages() throws IOException {
        final UserRequest userRequest = new UserRequest();
        userRequest.setLocalCsvFile("/Users/himadri/Projects/MBKatalogus/katalogus.csv");
        userSession.setUserRequest(userRequest);
        final List<Item> items = catalogueReader.readWithCsvBeanReader();
        final Set<String> kepek = items.stream().map(Item::getKepnev).collect(Collectors.toSet());
        final Set<String> files = Files.list(Paths.get(Settings.IMAGE_LOCATION.getAbsolutePath())).map(a -> a.getFileName().toString()).collect(Collectors.toSet());
        files.removeAll(kepek);
        return files.toString();

    }

    private String convertFileListToLinks(List<String> files) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < files.size(); i++) {
            sb.append(String.format("<a href=\"/render/%s\" target=\"_blank\">Rendered PDF #%d</a>%n", files.get(i), i));
        }
        return sb.toString();
    }

}
