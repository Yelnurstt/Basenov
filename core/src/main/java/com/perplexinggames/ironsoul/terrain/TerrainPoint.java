package com.perplexinggames.ironsoul.terrain;

public class TerrainPoint {
    private String id;
    private float x;
    private float y;

    public TerrainPoint() {
        this("", 0f, 0f);
    }

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

    public TerrainPoint copy() {
        return new TerrainPoint(id, x, y);
    }
}
