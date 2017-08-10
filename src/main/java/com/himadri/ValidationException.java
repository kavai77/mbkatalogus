package com.himadri;

import com.himadri.model.ErrorItem;

public class ValidationException extends Exception {
    private final ErrorItem.Severity severity;
    private final ErrorItem.ErrorCategory errorCategory;

    public ValidationException(ErrorItem.Severity severity, ErrorItem.ErrorCategory errorCategory, String message) {
        super(message);
        this.severity = severity;
        this.errorCategory = errorCategory;
    }

    public ErrorItem.Severity getSeverity() {
        return severity;
    }

    public ErrorItem.ErrorCategory getErrorCategory() {
        return errorCategory;
    }
}
