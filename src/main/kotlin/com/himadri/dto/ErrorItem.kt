package com.himadri.dto

data class ErrorItem(val severity: Severity, val category: ErrorCategory, val message: String) {
    enum class Severity { INFO, WARN, ERROR }
    enum class ErrorCategory {
        IMAGE, FORMATTING, RUNTIME, INFO
    }
}

