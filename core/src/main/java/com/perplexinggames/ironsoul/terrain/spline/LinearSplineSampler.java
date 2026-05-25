package com.perplexinggames.ironsoul.terrain.spline;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class LinearSplineSampler implements SplineSampler {
    @Override
    public List<SplineSample> sample(SplinePath splinePath, float sampleSpacing) {
        List<SplineSample> samples = new ArrayList<>();
        if (splinePath == null || splinePath.getPoints().isEmpty()) {
            return samples;
        }

        List<SplineControlPoint> points = splinePath.getPoints();
        if (points.size() == 1) {
            samples.add(new SplineSample(new Vector2(points.get(0).x, points.get(0).y), new Vector2(1f, 0f), 0f));
            return samples;
        }
        int segmentCount = splinePath.closed ? points.size() : points.size() - 1;
        float distance = 0f;
        for (int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++) {
            SplineControlPoint startPoint = points.get(segmentIndex);
            SplineControlPoint endPoint = points.get((segmentIndex + 1) % points.size());
            Vector2 start = new Vector2(startPoint.x, startPoint.y);
            Vector2 end = new Vector2(endPoint.x, endPoint.y);
            Vector2 segment = new Vector2(end).sub(start);
            float length = segment.len();
            if (length <= 0.001f) {
                continue;
            }

            Vector2 direction = new Vector2(segment).nor();
            Vector2 normal = new Vector2(-direction.y, direction.x);
            if (samples.isEmpty()) {
                samples.add(new SplineSample(start, direction, normal, 0f));
            }
            float spacing = Math.max(1f, sampleSpacing);
            int subdivisions = Math.max(1, (int) Math.ceil(length / spacing));
            for (int step = 1; step <= subdivisions; step++) {
                float alpha = step / (float) subdivisions;
                Vector2 position = new Vector2(start).lerp(end, alpha);
                distance += position.dst(samples.get(samples.size() - 1).getPosition());
                samples.add(new SplineSample(position, direction, normal, distance));
            }
        }
        return samples;
    }
}
