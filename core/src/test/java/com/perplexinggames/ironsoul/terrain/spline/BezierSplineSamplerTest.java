package com.perplexinggames.ironsoul.terrain.spline;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BezierSplineSamplerTest {
    @Test
    public void cubicCurveReturnsSamplesWithValidTangentsAndDistance() {
        SplineControlPoint start = new SplineControlPoint("p0", 0f, 0f, 0f, 0f, 30f, 0f, BezierHandleMode.FREE);
        SplineControlPoint end = new SplineControlPoint("p1", 100f, 0f, -30f, 40f, 0f, 0f, BezierHandleMode.FREE);
        SplinePath path = new SplinePath("path", "Bezier", java.util.List.of(start, end),
            SplineCurveType.BEZIER, false, true, 4f, "default", null);

        List<SplineSample> samples = new BezierSplineSampler(16).sample(path, 16f);

        assertFalse(samples.isEmpty());
        assertEquals(0f, samples.get(0).getX(), 0.001f);
        assertEquals(0f, samples.get(0).getY(), 0.001f);
        assertEquals(100f, samples.get(samples.size() - 1).getX(), 0.001f);
        assertEquals(0f, samples.get(samples.size() - 1).getY(), 0.001f);
        assertTrue(Math.abs(samples.get(0).getTangentX()) > 0.0001f || Math.abs(samples.get(0).getTangentY()) > 0.0001f);
        for (int i = 1; i < samples.size(); i++) {
            assertTrue(samples.get(i).getDistanceAlongPath() >= samples.get(i - 1).getDistanceAlongPath());
        }
    }
}
