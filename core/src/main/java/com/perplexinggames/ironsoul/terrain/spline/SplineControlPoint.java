package com.perplexinggames.ironsoul.terrain.spline;

import com.badlogic.gdx.math.Vector2;

public class SplineControlPoint {
    public String id;
    public float x;
    public float y;
    public float inHandleX;
    public float inHandleY;
    public float outHandleX;
    public float outHandleY;
    public BezierHandleMode handleMode;

    public SplineControlPoint() {
        this("", 0f, 0f, 0f, 0f, 0f, 0f, BezierHandleMode.AUTO);
    }

    public SplineControlPoint(String id, float x, float y) {
        this(id, x, y, 0f, 0f, 0f, 0f, BezierHandleMode.AUTO);
    }

    public SplineControlPoint(String id, float x, float y, float inHandleX, float inHandleY,
                              float outHandleX, float outHandleY, BezierHandleMode handleMode) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.inHandleX = inHandleX;
        this.inHandleY = inHandleY;
        this.outHandleX = outHandleX;
        this.outHandleY = outHandleY;
        this.handleMode = handleMode;
    }

    public Vector2 getAnchorPosition() {
        return new Vector2(x, y);
    }

    public Vector2 getInHandleOffset() {
        return new Vector2(inHandleX, inHandleY);
    }

    public Vector2 getOutHandleOffset() {
        return new Vector2(outHandleX, outHandleY);
    }

    public Vector2 getInHandleWorldPosition() {
        return new Vector2(x + inHandleX, y + inHandleY);
    }

    public Vector2 getOutHandleWorldPosition() {
        return new Vector2(x + outHandleX, y + outHandleY);
    }

    public SplineControlPoint copy() {
        return new SplineControlPoint(id, x, y, inHandleX, inHandleY, outHandleX, outHandleY, handleMode);
    }
}
