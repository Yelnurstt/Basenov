package com.perplexinggames.ironsoul.terrain.spline;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class BezierSplineSampler implements SplineSampler {
    private final int minimumSamplesPerSegment;

    public BezierSplineSampler() {
        this(16);
    }

    public BezierSplineSampler(int minimumSamplesPerSegment) {
        this.minimumSamplesPerSegment = Math.max(4, minimumSamplesPerSegment);
    }

    @Override
    public List<SplineSample> sample(SplinePath splinePath, float sampleSpacing) {
        List<SplineSample> samples = new ArrayList<>();
        if (splinePath == null || splinePath.getPoints().size() < 2) {
            return samples;
        }

        List<SplineControlPoint> points = splinePath.getPoints();
        int segmentCount = splinePath.closed ? points.size() : points.size() - 1;
        Vector2 previousPosition = null;
        float distance = 0f;

        for (int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++) {
            SplineControlPoint current = points.get(segmentIndex);
            SplineControlPoint next = points.get((segmentIndex + 1) % points.size());
            Vector2 p0 = current.getAnchorPosition();
            Vector2 p1 = current.getOutHandleWorldPosition();
            Vector2 p2 = next.getInHandleWorldPosition();
            Vector2 p3 = next.getAnchorPosition();

            float chordLength = p0.dst(p3);
            int segmentSamples = Math.max(minimumSamplesPerSegment,
                (int) Math.ceil(chordLength / Math.max(8f, sampleSpacing)));

            for (int sampleIndex = 0; sampleIndex <= segmentSamples; sampleIndex++) {
                if (segmentIndex > 0 && sampleIndex == 0) {
                    continue;
                }
                float t = sampleIndex / (float) segmentSamples;
                Vector2 position = evaluateBezier(p0, p1, p2, p3, t);
                Vector2 tangent = evaluateBezierDerivative(p0, p1, p2, p3, t);
                if (tangent.len2() == 0f && previousPosition != null) {
                    tangent = new Vector2(position).sub(previousPosition);
                }
                if (tangent.len2() == 0f) {
                    tangent = new Vector2(1f, 0f);
                }
                tangent.nor();
                Vector2 normal = new Vector2(-tangent.y, tangent.x);
                if (previousPosition != null) {
                    distance += previousPosition.dst(position);
                }
                samples.add(new SplineSample(position, tangent, normal, distance));
                previousPosition = position;
            }
        }
        return samples;
    }

    static Vector2 evaluateBezier(Vector2 p0, Vector2 p1, Vector2 p2, Vector2 p3, float t) {
        float oneMinusT = 1f - t;
        float oneMinusTSquared = oneMinusT * oneMinusT;
        float tSquared = t * t;

        return new Vector2(p0).scl(oneMinusTSquared * oneMinusT)
            .add(new Vector2(p1).scl(3f * oneMinusTSquared * t))
            .add(new Vector2(p2).scl(3f * oneMinusT * tSquared))
            .add(new Vector2(p3).scl(tSquared * t));
    }

    static Vector2 evaluateBezierDerivative(Vector2 p0, Vector2 p1, Vector2 p2, Vector2 p3, float t) {
        float oneMinusT = 1f - t;
        return new Vector2(p1).sub(p0).scl(3f * oneMinusT * oneMinusT)
            .add(new Vector2(p2).sub(p1).scl(6f * oneMinusT * t))
            .add(new Vector2(p3).sub(p2).scl(3f * t * t));
    }
}
