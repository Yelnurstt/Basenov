package com.perplexinggames.ironsoul.terrain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TerrainPath {
    public enum CurveType {
        LINEAR
    }

    private String id;
    private ArrayList<TerrainPoint> points;
    private CurveType curveType;
    private String material;
    private float debugWidth;
    private float friction;

    public TerrainPath() {
        this("", new ArrayList<>(), CurveType.LINEAR, "default", 4f, 1f);
    }

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
        this.points = new ArrayList<>(points == null ? Collections.emptyList() : points);
        this.curveType = curveType;
        this.material = material;
        this.debugWidth = debugWidth;
        this.friction = friction;
    }

    public String getId() {
        return id;
    }

    public List<TerrainPoint> getPoints() {
        if (points == null) {
            points = new ArrayList<>();
        }
        return Collections.unmodifiableList(points);
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

    public TerrainPath copy() {
        List<TerrainPoint> sourcePoints = getPoints();
        List<TerrainPoint> pointCopies = new ArrayList<>(sourcePoints.size());
        for (TerrainPoint point : sourcePoints) {
            if (point != null) {
                pointCopies.add(point.copy());
            }
        }
        return new TerrainPath(id, pointCopies, curveType == null ? CurveType.LINEAR : curveType,
            material == null ? "default" : material, debugWidth, friction);
    }
}
