package com.perplexinggames.ironsoul.editor.event;

public class LevelSavedEvent implements EditorEvent {
    public final String path;
    public final int blockCount;

    public LevelSavedEvent(String path, int blockCount) {
        this.path = path;
        this.blockCount = blockCount;
    }
}
