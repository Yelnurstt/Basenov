package com.perplexinggames.ironsoul.world;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorldData {
    public String id;
    public String name;
    public int tileSize;
    public String activeBlockId;
    public List<WorldBlockData> worldBlocks;
    public Map<String, String> metadata;

    public WorldData() {
        worldBlocks = new ArrayList<>();
        metadata = new LinkedHashMap<>();
    }

    public WorldData(String id, String name, int tileSize) {
        this();
        this.id = id;
        this.name = name;
        this.tileSize = tileSize;
    }

    public WorldData copy() {
        WorldData copy = new WorldData(id, name, tileSize);
        copy.activeBlockId = activeBlockId;
        for (WorldBlockData worldBlock : worldBlocks) {
            if (worldBlock != null) {
                copy.worldBlocks.add(worldBlock.copy());
            }
        }
        copy.metadata.putAll(metadata);
        return copy;
    }

    public WorldBlockData findBlock(String blockId) {
        if (blockId == null) {
            return null;
        }
        for (WorldBlockData worldBlock : worldBlocks) {
            if (worldBlock != null && blockId.equals(worldBlock.id)) {
                return worldBlock;
            }
        }
        return null;
    }
}
