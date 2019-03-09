package com.himadri;

import com.google.common.cache.Cache;
import com.himadri.dto.*;
import com.himadri.engine.CatalogueReader;
import com.himadri.engine.DocumentEngine;
import com.himadri.engine.PersistenceService;
import com.himadri.exception.ValidationException;
import com.himadri.model.rendering.CsvItem;
import com.himadri.model.rendering.Document;
import com.himadri.model.service.InstanceProperties;
import com.himadri.model.service.UserSession;
import com.himadri.renderer.DocumentRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private static final ValidationException TERMINATED_EXCEPTION = new ValidationException(
            ErrorItem.Severity.ERROR, ErrorItem.ErrorCategory.RUNTIME, "A generálás hiba miatt megszakadt.");

    @Autowired
    private CatalogueReader catalogueReader;

    @Autowired
    private DocumentEngine documentEngine;

    @Autowired
    private DocumentRenderer documentRenderer;

    @Autowired
    private Cache<String, UserSession> userSessionCache;

    @Autowired
    private PersistenceService persistenceService;

    @Value("${pageTitle}")
    private String pageTitle;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @PostMapping("/csvRendering")
    @ResponseBody
    public RequestId csvRendering(@RequestParam MultipartFile csvFile,
                                  @RequestParam String title,
                                  @RequestParam Quality quality,
                                  @RequestParam boolean wholeSaleFormat,
                                  @RequestParam boolean autoLineBreakAfterMinQty,
                                  @RequestParam MultipartFile headerImage,
                                  @RequestParam boolean wideHeaderImage,
                                  @RequestParam MultipartFile footerImage,
                                  @RequestParam boolean wideFooterImage) throws IOException {
        String id = UUID.randomUUID().toString();
        final UserSession userSession = new UserSession();
        userSessionCache.put(id, userSession);
        final UserRequest userRequest = new UserRequest(id, csvFile.getInputStream(), title, quality,
            wholeSaleFormat, autoLineBreakAfterMinQty, headerImage.getInputStream(), wideHeaderImage,
            footerImage.getInputStream(), wideFooterImage);
        executorService.submit(() -> {
            try {
                saveLastUserRequest(userRequest);
                final List<CsvItem> items = catalogueReader.readWithCsvBeanReader(userRequest);
                final Document document = documentEngine.createDocumentFromItems(items, userRequest, userSession);
                documentRenderer.renderDocument(document, userRequest);
            } catch (ValidationException e) {
                userSession.addErrorItem(e);
                userSession.addErrorItem(TERMINATED_EXCEPTION);
            } catch (IOException e) {
                userSession.addErrorItem(ErrorItem.Severity.ERROR, ErrorItem.ErrorCategory.RUNTIME, "IO hiba történt: " + e.getMessage());
                userSession.addErrorItem(TERMINATED_EXCEPTION);
                LOGGER.error("IOException in main worker thread", e);
            } catch (Throwable e) {
                StringBuilder sb = new StringBuilder();
                for (Throwable i = e;i != null; i = i.getCause()) {
                    sb.append(i.getMessage()).append(' ');
                }
                userSession.addErrorItem(ErrorItem.Severity.ERROR, ErrorItem.ErrorCategory.RUNTIME, "Ismeretlen hiba történt: " + sb.toString());
                userSession.addErrorItem(TERMINATED_EXCEPTION);
                LOGGER.error("Unexpected exception in main worker thread", e);
            } finally {
                userSession.setDone();
            }
        });

        return new RequestId(id);
    }

    private void saveLastUserRequest(UserRequest userRequest) {
        final InstanceProperties instanceProperties = persistenceService.getInstanceProperties();
        instanceProperties.setLastCatalogueName(userRequest.getCatalogueTitle());
        instanceProperties.setLastQuality(userRequest.getQuality());
        instanceProperties.setLastWholeSaleFormat(userRequest.isWholeSaleFormat());
        instanceProperties.setLastAutoLineBreakAfterMinQty(userRequest.isAutoLineBreakAfterMinQty());
        persistenceService.persistInstanceProperties();
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

    @GetMapping("/indexbootstrap")
    @ResponseBody
    public IndexBootStrap indexBootStrap() {
        final InstanceProperties instanceProperties = persistenceService.getInstanceProperties();
        return new IndexBootStrap()
            .setPageTitle(pageTitle)
            .setLastDocumentTitle(instanceProperties.getLastCatalogueName())
            .setLastQuality(instanceProperties.getLastQuality())
            .setLastWholeSaleFormat(instanceProperties.getLastWholeSaleFormat() != null ? instanceProperties.getLastWholeSaleFormat().toString() : null)
            .setLastAutoLineBreakAfterMinQty(instanceProperties.isLastAutoLineBreakAfterMinQty())
            .setLastLastWideHeaderImage(instanceProperties.isLastWideHeaderImage())
            .setLastLastWideFooterImage(instanceProperties.isLastWideFooterImage())
            .setProductGroupsWithoutChapter(instanceProperties.getProductGroupsWithoutChapter());
    }

    @PostMapping("/saveproductgroupwithoutchapter")
    public ResponseEntity<?> saveProductGroupWithoutChapter(@RequestBody List<String> productGroupWithoutChapter) {
        persistenceService.getInstanceProperties().setProductGroupsWithoutChapter(productGroupWithoutChapter);
        persistenceService.persistInstanceProperties();
        return ResponseEntity.ok(null);
    }

}
