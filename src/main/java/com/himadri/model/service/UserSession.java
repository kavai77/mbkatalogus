package com.himadri.model.service;

import com.himadri.dto.ErrorItem;
import com.himadri.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class UserSession {

    private final Queue<ErrorItem> errorItems = new ConcurrentLinkedQueue<>();
    private final Queue<GeneratedDocument> generatedDocuments = new ConcurrentLinkedQueue<>();
    private final AtomicInteger totalPageCount = new AtomicInteger();
    private final AtomicInteger currentPageNumber = new AtomicInteger();
    private final AtomicLong totalImageSize = new AtomicLong();
    private final AtomicLong totalLogoImageSize = new AtomicLong();
    private final AtomicBoolean done = new AtomicBoolean();
    private final AtomicBoolean cancelled = new AtomicBoolean();

    public void addErrorItem(ErrorItem.Severity severity, ErrorItem.ErrorCategory errorCategory, String message) {
        errorItems.add(new ErrorItem(severity, errorCategory, message));
    }

    public void addErrorItem(ValidationException validationException) {
        errorItems.add(new ErrorItem(validationException.getSeverity(), validationException.getErrorCategory(),
                validationException.getMessage()));
    }

    public void addGeneratedDocument(String relativeUrl, String displayName) {
        generatedDocuments.add(new GeneratedDocument(relativeUrl, displayName));
    }

    public List<GeneratedDocument> getAllGeneratedDocuments() {
        return new ArrayList<>(generatedDocuments);
    }

    public List<ErrorItem> pollErrorItems() {
        List<ErrorItem> items = new ArrayList<>();
        ErrorItem item;
        while ((item = errorItems.poll()) != null) {
            items.add(item);
        }
        return items;
    }

    public int getTotalPageCount() {
        return totalPageCount.get();
    }

    public void setTotalPageCount(int totalPageCount) {
        this.totalPageCount.set(totalPageCount);
    }

    public int getCurrentPageNumber() {
        return currentPageNumber.get();
    }

    public void incrementCurrentPageNumber() {
        currentPageNumber.incrementAndGet();
    }

    public boolean isDone() {
        return done.get();
    }

    public void setDone() {
        done.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void setCancelled() {
        cancelled.set(true);
    }

    public AtomicLong getTotalImageSize() {
        return totalImageSize;
    }

    public AtomicLong getTotalLogoImageSize() {
        return totalLogoImageSize;
    }
}
