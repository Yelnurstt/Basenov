package com.perplexinggames.ironsoul.world;

import java.util.LinkedHashMap;
import java.util.Map;

public class WorldElementData {
    public String id;
    public String type;
    public float x;
    public float y;
    public String debugName;
    public Map<String, String> metadata;

    public WorldElementData() {
        metadata = new LinkedHashMap<>();
    }

    public WorldElementData(String id, String type, float x, float y, String debugName) {
        this();
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.debugName = debugName;
    }

    public WorldElementData copy() {
        WorldElementData copy = new WorldElementData(id, type, x, y, debugName);
        copy.metadata.putAll(metadata);
        return copy;
    }
}
