package com.perplexinggames.ironsoul.editor.event;

import com.perplexinggames.ironsoul.editor.EditorMode;

public class EditorModeChangedEvent implements EditorEvent {
    public final EditorMode previousMode;
    public final EditorMode currentMode;

    public EditorModeChangedEvent(EditorMode previousMode, EditorMode currentMode) {
        this.previousMode = previousMode;
        this.currentMode = currentMode;
    }
}
