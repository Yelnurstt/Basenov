package com.perplexinggames.ironsoul.terrain;

import java.util.ArrayList;
import java.util.List;

public class TerrainCollisionBuilder {
    public TerrainCollisionData build(TerrainPath terrainPath) {
        List<TerrainSegment> segments = new ArrayList<>();
        List<TerrainPoint> points = terrainPath.getPoints();

        for (int i = 0; i < points.size() - 1; i++) {
            TerrainPoint start = points.get(i);
            TerrainPoint end = points.get(i + 1);
            segments.add(new TerrainSegment(start, end));
        }

        return new TerrainCollisionData(
            terrainPath.getId(),
            terrainPath.getMaterial(),
            terrainPath.getDebugWidth(),
            terrainPath.getFriction(),
            segments
        );
    }
}
