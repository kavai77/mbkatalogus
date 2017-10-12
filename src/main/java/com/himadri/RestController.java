package com.himadri;

import com.google.common.cache.Cache;
import com.himadri.dto.ErrorItem;
import com.himadri.dto.RequestId;
import com.himadri.dto.UserPollingInfo;
import com.himadri.dto.UserRequest;
import com.himadri.engine.CatalogueReader;
import com.himadri.engine.DocumentEngine;
import com.himadri.exception.ValidationException;
import com.himadri.model.rendering.Document;
import com.himadri.model.rendering.Item;
import com.himadri.model.service.UserSession;
import com.himadri.renderer.DocumentRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
@RequestMapping("/service")
public class RestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestController.class);

    @Autowired
    private CatalogueReader catalogueReader;

    @Autowired
    private DocumentEngine documentEngine;

    @Autowired
    private DocumentRenderer documentRenderer;

    @Autowired
    private Cache<String, UserSession> userSessionCache;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @PostMapping("/csvRendering")
    @ResponseBody
    public RequestId csvRendering(@RequestParam MultipartFile file,
                                  @RequestParam String title,
                                  @RequestParam boolean draftMode,
                                  @RequestParam boolean wholeSaleFormat,
                                  @RequestParam boolean autoLineBreakAfterMinQty,
                                  @RequestParam int skipBoxSpaceOnBeginning) throws IOException {
        String id = UUID.randomUUID().toString();
        final UserSession userSession = new UserSession();
        userSessionCache.put(id, userSession);
        final UserRequest userRequest = new UserRequest(id, file.getInputStream(), title, draftMode, wholeSaleFormat,
                autoLineBreakAfterMinQty, skipBoxSpaceOnBeginning);
        executorService.submit(() -> {
            try {
                final List<Item> items = catalogueReader.readWithCsvBeanReader(userRequest);
                final Document document = documentEngine.createDocumentFromItems(items, userRequest);
                documentRenderer.renderDocument(document, userRequest);
            } catch (ValidationException e) {
                userSession.addErrorItem(e);
            } catch (IOException e) {
                userSession.addErrorItem(ErrorItem.Severity.ERROR, ErrorItem.ErrorCategory.RUNTIME, "IO hiba történt: " + e.getMessage());
                LOGGER.error("IOException in main worker thread", e);
            } catch (Throwable e) {
                StringBuilder sb = new StringBuilder();
                for (Throwable i = e;i != null; i = i.getCause()) {
                    sb.append(i.getMessage()).append(' ');
                }
                userSession.addErrorItem(ErrorItem.Severity.ERROR, ErrorItem.ErrorCategory.RUNTIME, "Ismeretlen hiba történt: " + sb.toString());
                LOGGER.error("Unexpected exception in main worker thread", e);
            } finally {
                userSession.setDone();
            }
        });

        return new RequestId(id);
    }

    @GetMapping("/pollUserInfo")
    @ResponseBody
    public UserPollingInfo userPollingInfo(@RequestParam String requestId) {
        final UserSession userSession = userSessionCache.getIfPresent(requestId);
        return userSession != null ? UserPollingInfo.createFromUserSession(userSession) : null;
    }

    @GetMapping("/cancel")
    public ResponseEntity<?> cancel(@RequestParam String requestId) {
        final UserSession userSession = userSessionCache.getIfPresent(requestId);
        if (userSession != null) {
            userSession.setCancelled();
        }
        return ResponseEntity.ok(null);
    }

}
