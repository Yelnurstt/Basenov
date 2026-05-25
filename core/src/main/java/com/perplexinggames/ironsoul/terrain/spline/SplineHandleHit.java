package com.perplexinggames.ironsoul.terrain.spline;

public class SplineHandleHit {
    public final String pathId;
    public final String pointId;
    public final BezierHandleType handleType;
    public final float worldX;
    public final float worldY;

    public SplineHandleHit(String pathId, String pointId, BezierHandleType handleType, float worldX, float worldY) {
        this.pathId = pathId;
        this.pointId = pointId;
        this.handleType = handleType;
        this.worldX = worldX;
        this.worldY = worldY;
    }
}
