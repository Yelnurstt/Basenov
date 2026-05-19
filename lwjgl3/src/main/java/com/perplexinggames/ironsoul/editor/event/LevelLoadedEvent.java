package com.perplexinggames.ironsoul.editor.event;

public class LevelLoadedEvent implements EditorEvent {
    public final String source;
    public final int blockCount;
    public final boolean emptyFallback;

    public LevelLoadedEvent(String source, int blockCount, boolean emptyFallback) {
        this.source = source;
        this.blockCount = blockCount;
        this.emptyFallback = emptyFallback;
    }
}
