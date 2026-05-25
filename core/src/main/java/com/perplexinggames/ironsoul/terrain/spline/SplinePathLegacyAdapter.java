package com.perplexinggames.ironsoul.terrain.spline;

import com.badlogic.gdx.graphics.Color;
import com.perplexinggames.ironsoul.terrain.TerrainPath;
import com.perplexinggames.ironsoul.terrain.TerrainPoint;

import java.util.ArrayList;
import java.util.List;

public final class SplinePathLegacyAdapter {
    public static final String LEGACY_LAYER_SUFFIX = "-legacy-layer";

    private SplinePathLegacyAdapter() {
    }

    public static SplinePath fromTerrainPath(TerrainPath terrainPath) {
        if (terrainPath == null) {
            return null;
        }

        List<SplineControlPoint> points = new ArrayList<>();
        for (TerrainPoint point : terrainPath.getPoints()) {
            points.add(new SplineControlPoint(point.getId(), point.getX(), point.getY()));
        }

        return new SplinePath(
            terrainPath.getId(),
            terrainPath.getId(),
            points,
            SplineCurveType.LINEAR,
            false,
            true,
            terrainPath.getDebugWidth(),
            terrainPath.getMaterial(),
            terrainPath.getMaterial()
        );
    }

    public static SplineLayer createDefaultLayer(SplinePath splinePath) {
        String pathId = splinePath == null ? "" : splinePath.id;
        String name = splinePath == null || splinePath.name == null ? "Legacy Terrain" : splinePath.name;
        return new SplineLayer(
            pathId + LEGACY_LAYER_SUFFIX,
            pathId,
            name + " Layer",
            null,
            null,
            0,
            1f,
            0f,
            splinePath == null ? 32f : Math.max(16f, splinePath.collisionThickness),
            SplineTileMode.STRETCH,
            Color.WHITE,
            true,
            true
        );
    }
}
