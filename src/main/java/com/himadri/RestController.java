package com.himadri;

import com.google.common.cache.Cache;
import com.himadri.engine.CatalogueReader;
import com.himadri.engine.ModelTransformerEngine;
import com.himadri.model.ErrorCollector;
import com.himadri.model.Item;
import com.himadri.model.Page;
import com.himadri.model.UserRequest;
import com.himadri.renderer.DocumentRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
@RequestMapping("/service")
public class RestController {
    @Autowired
    private CatalogueReader catalogueReader;

    @Autowired
    private ModelTransformerEngine modelTransformerEngine;

    @Autowired
    private DocumentRenderer documentRenderer;

    @Autowired
    private Cache<String, ErrorCollector> userSessionCache;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @PostMapping("/csvRendering")
    @ResponseBody
    public String csvRendering(@RequestParam MultipartFile file,
                                 @RequestParam String title,
                                 @RequestParam boolean imageIncluded) throws IOException {
        String id = UUID.randomUUID().toString();
        userSessionCache.put(id, new ErrorCollector());
        final UserRequest userRequest = new UserRequest(id, file.getInputStream(), title, imageIncluded);
        executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final List<Item> items = catalogueReader.readWithCsvBeanReader(userRequest);
                final List<Page> pages = modelTransformerEngine.createPagesFromItems(items, userRequest);
                final List<String> fileNames = documentRenderer.renderDocument(pages, userRequest);
                return null;
            }
        });

        return "{\"requestId\": \"" + id +"\"}";
    }

}
