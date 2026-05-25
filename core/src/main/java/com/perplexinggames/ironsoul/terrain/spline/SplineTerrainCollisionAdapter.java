package com.perplexinggames.ironsoul.terrain.spline;

import com.perplexinggames.ironsoul.level.RuntimeLevel;
import com.perplexinggames.ironsoul.terrain.TerrainCollisionData;
import com.perplexinggames.ironsoul.terrain.TerrainPoint;
import com.perplexinggames.ironsoul.terrain.TerrainSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SplineTerrainCollisionAdapter {
    private final float sampleSpacing;

    public SplineTerrainCollisionAdapter(float sampleSpacing) {
        this.sampleSpacing = sampleSpacing;
    }

    public Collection<TerrainCollisionData> build(RuntimeLevel runtimeLevel) {
        List<TerrainCollisionData> collisionData = new ArrayList<>();
        if (runtimeLevel == null) {
            return collisionData;
        }

        for (SplinePath splinePath : runtimeLevel.getEffectiveSplinePaths()) {
            if (splinePath == null || !splinePath.collisionEnabled) {
                continue;
            }
            List<SplineSample> samples = SplineSampling.sample(splinePath, sampleSpacing);
            if (samples.size() < 2) {
                continue;
            }

            List<TerrainSegment> segments = new ArrayList<>();
            for (int i = 1; i < samples.size(); i++) {
                SplineSample previous = samples.get(i - 1);
                SplineSample current = samples.get(i);
                TerrainPoint start = new TerrainPoint(splinePath.id + "-sample-" + (i - 1),
                    previous.getPosition().x, previous.getPosition().y);
                TerrainPoint end = new TerrainPoint(splinePath.id + "-sample-" + i,
                    current.getPosition().x, current.getPosition().y);
                segments.add(new TerrainSegment(start, end));
            }
            collisionData.add(new TerrainCollisionData(
                splinePath.id,
                splinePath.physicsMaterial == null ? splinePath.material : splinePath.physicsMaterial,
                splinePath.collisionThickness,
                1f,
                segments
            ));
        }
        return collisionData;
    }
}
