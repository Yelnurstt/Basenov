package com.perplexinggames.ironsoul.level;

import java.util.ArrayList;
import java.util.List;

public class LevelData {
    public String id;
    public String name;
    public int width;
    public int height;
    public int tileSize;
    public List<BlockData> blocks;

    public LevelData() {
        this.blocks = new ArrayList<>();
    }

    public LevelData(String id, String name, int width, int height, int tileSize) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.tileSize = tileSize;
        this.blocks = new ArrayList<>();
    }

    public LevelData copy() {
        LevelData copy = new LevelData(id, name, width, height, tileSize);
        for (BlockData block : blocks) {
            copy.blocks.add(block.copy());
        }
        return copy;
    }
}
