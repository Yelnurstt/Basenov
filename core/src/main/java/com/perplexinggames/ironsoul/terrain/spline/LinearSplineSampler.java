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
        float distance = 0f;
        for (int i = 0; i < points.size(); i++) {
            SplineControlPoint current = points.get(i);
            Vector2 currentPosition = new Vector2(current.x, current.y);
            Vector2 tangent;
            if (i < points.size() - 1) {
                SplineControlPoint next = points.get(i + 1);
                tangent = new Vector2(next.x - current.x, next.y - current.y).nor();
            } else if (i > 0) {
                SplineControlPoint previous = points.get(i - 1);
                tangent = new Vector2(current.x - previous.x, current.y - previous.y).nor();
            } else {
                tangent = new Vector2(1f, 0f);
            }
            if (samples.isEmpty()) {
                samples.add(new SplineSample(currentPosition, tangent, 0f));
                continue;
            }

            SplineControlPoint previous = points.get(i - 1);
            Vector2 start = new Vector2(previous.x, previous.y);
            Vector2 end = currentPosition;
            Vector2 segment = new Vector2(end).sub(start);
            float length = segment.len();
            if (length <= 0.001f) {
                continue;
            }

            Vector2 direction = new Vector2(segment).nor();
            float spacing = Math.max(1f, sampleSpacing);
            int subdivisions = Math.max(1, (int) Math.ceil(length / spacing));
            for (int step = 1; step <= subdivisions; step++) {
                float alpha = step / (float) subdivisions;
                Vector2 position = new Vector2(start).lerp(end, alpha);
                distance += step == subdivisions ? position.dst(samples.get(samples.size() - 1).getPosition())
                    : length / subdivisions;
                samples.add(new SplineSample(position, direction, distance));
            }
        }
        return samples;
    }
}
