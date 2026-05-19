package com.perplexinggames.ironsoul.editor.event;

import com.perplexinggames.ironsoul.level.BlockType;

public class BlockErasedEvent implements EditorEvent {
    public final int x;
    public final int y;
    public final BlockType blockType;

    public BlockErasedEvent(int x, int y, BlockType blockType) {
        this.x = x;
        this.y = y;
        this.blockType = blockType;
    }
}
