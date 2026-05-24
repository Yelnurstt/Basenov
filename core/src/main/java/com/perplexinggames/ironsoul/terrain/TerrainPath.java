package com.perplexinggames.ironsoul.terrain;

import com.badlogic.gdx.utils.Array;

public class TerrainPath {
    private final Array<TerrainSegment> segments = new Array<>();

    public void addSegment(float x1, float y1, float x2, float y2) {
        segments.add(new TerrainSegment(new TerrainPoint(x1, y1), new TerrainPoint(x2, y2)));
    }

    public Array<TerrainSegment> getSegments() {
        return segments;
    }
}
