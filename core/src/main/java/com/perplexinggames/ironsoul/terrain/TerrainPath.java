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

    public void addSegment(float x1, float y1, float x2, float y2) {
        if (points == null) {
            points = new ArrayList<>();
        }

        TerrainPoint start = new TerrainPoint("point-" + points.size(), x1, y1);
        TerrainPoint end = new TerrainPoint("point-" + (points.size() + 1), x2, y2);

        if (points.isEmpty()) {
            points.add(start);
        } else {
            TerrainPoint lastPoint = points.get(points.size() - 1);
            if (Float.compare(lastPoint.x, x1) != 0 || Float.compare(lastPoint.y, y1) != 0) {
                points.add(start);
            }
        }
        points.add(end);
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

    public List<TerrainSegment> getSegments() {
        List<TerrainSegment> segments = new ArrayList<>();
        List<TerrainPoint> sourcePoints = getPoints();
        for (int i = 0; i < sourcePoints.size() - 1; i++) {
            segments.add(new TerrainSegment(sourcePoints.get(i), sourcePoints.get(i + 1)));
        }
        return segments;
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
        List<TerrainPoint> pointCopies = new ArrayList<>(getPoints().size());
        for (TerrainPoint point : getPoints()) {
            if (point != null) {
                pointCopies.add(point.copy());
            }
        }
        return new TerrainPath(
            id,
            pointCopies,
            curveType == null ? CurveType.LINEAR : curveType,
            material == null ? "default" : material,
            debugWidth,
            friction
        );
    }
}
