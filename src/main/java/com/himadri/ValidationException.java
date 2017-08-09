package com.himadri;

import com.himadri.model.UserSession;

public class ValidationException extends Exception {
    private final UserSession.Severity severity;

    public ValidationException(UserSession.Severity severity, String message) {
        super(message);
        this.severity = severity;
    }

    public UserSession.Severity getSeverity() {
        return severity;
    }
}
