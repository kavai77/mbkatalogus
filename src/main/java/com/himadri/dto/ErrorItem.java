package com.himadri.dto;

public class ErrorItem {
    private final Severity severity;
    private final ErrorCategory category;
    private final String message;

    public ErrorItem(Severity severity, ErrorCategory category, String message) {
        this.severity = severity;
        this.category = category;
        this.message = message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public ErrorCategory getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    public enum Severity {INFO, WARN, ERROR}

    public enum ErrorCategory {
        IMAGE, FORMATTING, RUNTIME, INFO
    }
}
