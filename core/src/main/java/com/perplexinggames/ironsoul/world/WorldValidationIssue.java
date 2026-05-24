package com.perplexinggames.ironsoul.world;

public class WorldValidationIssue {
    public enum Severity {
        ERROR,
        WARNING
    }

    private final Severity severity;
    private final String message;

    public WorldValidationIssue(Severity severity, String message) {
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
