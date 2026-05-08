package com.perplexinggames.ironsoul.core.gameplay;

import java.util.ArrayList;
import java.util.List;

public class DialogueSystem {
    private final List<String> lines = new ArrayList<>();
    private int currentLineIndex;

    public void startConversation(List<String> newLines) {
        lines.clear();
        lines.addAll(newLines);
        currentLineIndex = 0;
    }

    public boolean isActive() {
        return !lines.isEmpty();
    }

    public String getCurrentLine() {
        if (!isActive()) {
            return "No active dialogue";
        }
        return lines.get(currentLineIndex);
    }

    public boolean advance() {
        if (!isActive()) {
            return true;
        }

        currentLineIndex++;
        if (currentLineIndex >= lines.size()) {
            lines.clear();
            currentLineIndex = 0;
            return true;
        }
        return false;
    }
}
