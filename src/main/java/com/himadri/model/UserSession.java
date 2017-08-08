package com.himadri.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class UserSession {
    public enum Severity {INFO, WARN, ERROR}

    private final Queue<ErrorItem> errorItems = new ConcurrentLinkedQueue<>();
    private final Queue<String> generatedDocuments = new ConcurrentLinkedQueue<>();
    private final AtomicInteger totalPageCount = new AtomicInteger();
    private final AtomicInteger currentPageNumber = new AtomicInteger();
    private final AtomicLong currentPDFImageBytes = new AtomicLong();
    private final AtomicBoolean done = new AtomicBoolean();
    private final AtomicBoolean cancelled = new AtomicBoolean();

    public void addErrorItem(Severity severity, String message) {
        errorItems.add(new ErrorItem(severity, message));
    }

    public void addGeneratedDocument(String fileName) {
        generatedDocuments.add(fileName);
    }

    public List<String> getAllGeneratedDocuments() {
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

    public void addCurrentPDFImageBytes(long bytes) {
        currentPDFImageBytes.addAndGet(bytes);
    }

    public void resetCurrentPDFImageBytes() {
        currentPDFImageBytes.set(0L);
    }

    public long getCurrentPDFImageBytes() {
        return currentPDFImageBytes.get();
    }

    public static class ErrorItem {
        private final Severity severity;
        private final String message;

        public ErrorItem(Severity severity, String message) {
            this.severity = severity;
            this.message = message;
        }

        public Severity getSeverity() {
            return severity;
        }

        public String getMessage() {
            return message;
        }
    }
}
