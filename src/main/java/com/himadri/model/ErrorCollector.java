package com.himadri.model;

import java.util.ArrayDeque;
import java.util.Queue;

public class ErrorCollector {
    public enum Severity {INFO, WARN, ERROR}

    private final Queue<ErrorItem> errorItems;

    public ErrorCollector() {
        errorItems = new ArrayDeque<>();
    }

    public void addErrorItem(Severity severity, String message, Object... args) {
        errorItems.add(new ErrorItem(severity, String.format(message, args)));
    }

    public ErrorItem pollErrorItem() {
        return errorItems.poll();
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
