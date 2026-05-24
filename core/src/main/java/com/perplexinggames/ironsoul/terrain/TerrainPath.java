package com.perplexinggames.ironsoul.terrain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TerrainPath {
    public enum CurveType {
        LINEAR
    }

    private final String id;
    private final List<TerrainPoint> points;
    private final CurveType curveType;
    private final String material;
    private final float debugWidth;
    private final float friction;

    public TerrainPath(String id, List<TerrainPoint> points) {
        this(id, points, CurveType.LINEAR, "default", 4f, 1f);
    }

    public TerrainPath(
        String id,
        List<TerrainPoint> points,
        CurveType curveType,
        String material,
        float debugWidth,
        float friction
    ) {
        this.id = id;
        this.points = Collections.unmodifiableList(new ArrayList<>(points));
        this.curveType = curveType;
        this.material = material;
        this.debugWidth = debugWidth;
        this.friction = friction;
    }

    public String getId() {
        return id;
    }

    public List<TerrainPoint> getPoints() {
        return points;
    }

    public CurveType getCurveType() {
        return curveType;
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
}
