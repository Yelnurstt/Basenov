package com.perplexinggames.ironsoul.terrain.spline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SplinePath {
    public String id;
    public String name;
    public ArrayList<SplineControlPoint> points;
    public SplineCurveType curveType;
    public boolean closed;
    public boolean collisionEnabled;
    public float collisionThickness;
    public String material;
    public String physicsMaterial;

    public SplinePath() {
        this("", "Spline", new ArrayList<>(), SplineCurveType.LINEAR, false, true, 4f, "default", null);
    }

    public SplinePath(String id, String name, List<SplineControlPoint> points, SplineCurveType curveType, boolean closed,
                      boolean collisionEnabled, float collisionThickness, String material, String physicsMaterial) {
        this.id = id;
        this.name = name;
        this.points = new ArrayList<>(points == null ? Collections.emptyList() : points);
        this.curveType = curveType;
        this.closed = closed;
        this.collisionEnabled = collisionEnabled;
        this.collisionThickness = collisionThickness;
        this.material = material;
        this.physicsMaterial = physicsMaterial;
    }

    public List<SplineControlPoint> getPoints() {
        if (points == null) {
            points = new ArrayList<>();
        }
        return Collections.unmodifiableList(points);
    }

    public SplinePath copy() {
        ArrayList<SplineControlPoint> pointCopies = new ArrayList<>();
        for (SplineControlPoint point : getPoints()) {
            if (point != null) {
                pointCopies.add(point.copy());
            }
        }
        return new SplinePath(id, name, pointCopies, curveType, closed, collisionEnabled, collisionThickness,
            material, physicsMaterial);
    }
}
