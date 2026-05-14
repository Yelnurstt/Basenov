package com.perplexinggames.ironsoul.editor.event;

public class ToolChangedEvent implements EditorEvent {
    public final String toolName;

    public ToolChangedEvent(String toolName) {
        this.toolName = toolName;
    }
}
