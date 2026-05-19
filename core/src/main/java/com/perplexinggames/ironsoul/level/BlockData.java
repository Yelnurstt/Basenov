package com.perplexinggames.ironsoul.level;

public class BlockData {
    public int x;
    public int y;
    public BlockType type;

    public BlockData() {
    }

    public BlockData(int x, int y, BlockType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public BlockData copy() {
        return new BlockData(x, y, type);
    }
}
