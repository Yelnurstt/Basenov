package com.perplexinggames.ironsoul.terrain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TerrainCollisionData {
    private final String pathId;
    private final String material;
    private final float debugWidth;
    private final float friction;
    private final List<TerrainSegment> segments;

    public TerrainCollisionData(
        String pathId,
        String material,
        float debugWidth,
        float friction,
        List<TerrainSegment> segments
    ) {
        this.pathId = pathId;
        this.material = material;
        this.debugWidth = debugWidth;
        this.friction = friction;
        this.segments = Collections.unmodifiableList(new ArrayList<>(segments));
    }

    public String getPathId() {
        return pathId;
    }

    public String getMaterial() {
        return material;
    }

    public float getDebugWidth() {
        return debugWidth;
    }

    public float getFriction() {
        return friction;
    }

    public List<TerrainSegment> getSegments() {
        return segments;
    }

    public boolean isEmpty() {
        return segments.isEmpty();
    }
}
