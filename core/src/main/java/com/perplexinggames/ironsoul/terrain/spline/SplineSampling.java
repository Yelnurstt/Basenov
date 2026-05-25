package com.perplexinggames.ironsoul.terrain.spline;

import java.util.Collections;
import java.util.List;

public final class SplineSampling {
    private static final SplineSampler LINEAR = new LinearSplineSampler();
    private static final SplineSampler CATMULL_ROM = new CatmullRomSplineSampler();

    private SplineSampling() {
    }

    public static List<SplineSample> sample(SplinePath splinePath, float sampleSpacing) {
        if (splinePath == null) {
            return Collections.emptyList();
        }
        return switch (splinePath.curveType == null ? SplineCurveType.LINEAR : splinePath.curveType) {
            case LINEAR -> LINEAR.sample(splinePath, sampleSpacing);
            case CATMULL_ROM -> CATMULL_ROM.sample(splinePath, sampleSpacing);
            case BEZIER -> CATMULL_ROM.sample(splinePath, sampleSpacing);
        };
    }
}
