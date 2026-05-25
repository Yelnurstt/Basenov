package com.perplexinggames.ironsoul.terrain.spline;

import java.util.List;

public interface SplineSampler {
    List<SplineSample> sample(SplinePath splinePath, float sampleSpacing);
}
