package com.perplexinggames.ironsoul.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorldValidationResult {
    private final List<WorldValidationIssue> issues = new ArrayList<>();

    public void addError(String message) {
        issues.add(new WorldValidationIssue(WorldValidationIssue.Severity.ERROR, message));
    }

    public void addWarning(String message) {
        issues.add(new WorldValidationIssue(WorldValidationIssue.Severity.WARNING, message));
    }

    public boolean isValid() {
        for (WorldValidationIssue issue : issues) {
            if (issue.getSeverity() == WorldValidationIssue.Severity.ERROR) {
                return false;
            }
        }
        return true;
    }

    public List<WorldValidationIssue> getIssues() {
        return Collections.unmodifiableList(issues);
    }
}
