package com.perplexinggames.ironsoul.terrain.spline;

public class SplineControlPoint {
    public String id;
    public float x;
    public float y;

    public SplineControlPoint() {
        this("", 0f, 0f);
    }

    public SplineControlPoint(String id, float x, float y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public SplineControlPoint copy() {
        return new SplineControlPoint(id, x, y);
    }
}
