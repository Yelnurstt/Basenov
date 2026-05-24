package com.perplexinggames.ironsoul.terrain;

public class TerrainPoint {
    private final String id;
    private final float x;
    private final float y;

    public TerrainPoint(String id, float x, float y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
