package com.himadri;

import java.util.logging.Level;

public class ValidationException extends Exception {
    private final Level level;

    public ValidationException(Level level, String message) {
        super(message);
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }
}
