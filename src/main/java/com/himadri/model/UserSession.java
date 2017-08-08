package com.himadri.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class UserSession {
    public enum Severity {INFO, WARN, ERROR}

    private final Queue<ErrorItem> errorItems = new ConcurrentLinkedQueue<>();
    private final Queue<String> generatedDocuments = new ConcurrentLinkedQueue<>();
    private volatile int totalPageCount;
    private final AtomicInteger currentPageNumber = new AtomicInteger();

    public void addErrorItem(Severity severity, String message, Object... args) {
        errorItems.add(new ErrorItem(severity, String.format(message, args)));
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
        return totalPageCount;
    }

    public void setTotalPageCount(int totalPageCount) {
        this.totalPageCount = totalPageCount;
    }

    public int getCurrentPageNumber() {
        return currentPageNumber.get();
    }

    public void incrementCurrentPageNumber() {
        currentPageNumber.incrementAndGet();
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
