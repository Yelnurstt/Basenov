package com.perplexinggames.ironsoul.terrain.spline;

import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class CatmullRomSplineSampler implements SplineSampler {
    @Override
    public List<SplineSample> sample(SplinePath splinePath, float sampleSpacing) {
        List<SplineSample> samples = new ArrayList<>();
        if (splinePath == null || splinePath.getPoints().size() < 2) {
            return samples;
        }

        List<SplineControlPoint> sourcePoints = splinePath.getPoints();
        Vector2[] controlPoints = buildControlPoints(sourcePoints, splinePath.closed);
        if (controlPoints.length < 2) {
            return samples;
        }

        CatmullRomSpline<Vector2> spline = new CatmullRomSpline<>(controlPoints, splinePath.closed);
        float estimatedLength = estimateLength(sourcePoints);
        int sampleCount = Math.max(sourcePoints.size(), (int) Math.ceil(estimatedLength / Math.max(8f, sampleSpacing)));

        Vector2 previousPosition = null;
        float distance = 0f;
        for (int i = 0; i <= sampleCount; i++) {
            float t = sampleCount == 0 ? 0f : i / (float) sampleCount;
            Vector2 position = spline.valueAt(new Vector2(), t);
            Vector2 tangent = spline.derivativeAt(new Vector2(), t).nor();
            if (previousPosition != null) {
                distance += previousPosition.dst(position);
            }
            samples.add(new SplineSample(position, tangent, distance));
            previousPosition = position;
        }
        return samples;
    }

    private Vector2[] buildControlPoints(List<SplineControlPoint> sourcePoints, boolean closed) {
        ArrayList<Vector2> points = new ArrayList<>();
        if (!closed && sourcePoints.size() >= 2) {
            SplineControlPoint first = sourcePoints.get(0);
            SplineControlPoint second = sourcePoints.get(1);
            points.add(new Vector2(first.x - (second.x - first.x), first.y - (second.y - first.y)));
        }
        for (SplineControlPoint point : sourcePoints) {
            points.add(new Vector2(point.x, point.y));
        }
        if (!closed && sourcePoints.size() >= 2) {
            SplineControlPoint last = sourcePoints.get(sourcePoints.size() - 1);
            SplineControlPoint beforeLast = sourcePoints.get(sourcePoints.size() - 2);
            points.add(new Vector2(last.x + (last.x - beforeLast.x), last.y + (last.y - beforeLast.y)));
        }
        return points.toArray(new Vector2[0]);
    }

    private float estimateLength(List<SplineControlPoint> points) {
        float total = 0f;
        for (int i = 1; i < points.size(); i++) {
            SplineControlPoint previous = points.get(i - 1);
            SplineControlPoint current = points.get(i);
            total += Vector2.dst(previous.x, previous.y, current.x, current.y);
        }
        return total;
    }
}
