package com.perplexinggames.ironsoul.terrain.spline;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BezierHandleServiceTest {
    @Test
    public void mirroredModeUpdatesOppositeHandle() {
        SplineControlPoint point = new SplineControlPoint("p", 0f, 0f, -10f, 0f, 10f, 0f, BezierHandleMode.MIRRORED);
        BezierHandleService.setOutHandle(point, 20f, 5f);
        assertEquals(-20f, point.inHandleX, 0.001f);
        assertEquals(-5f, point.inHandleY, 0.001f);
    }

    @Test
    public void alignedModeKeepsOppositeHandleCollinear() {
        SplineControlPoint point = new SplineControlPoint("p", 0f, 0f, -10f, 0f, 15f, 0f, BezierHandleMode.ALIGNED);
        BezierHandleService.setOutHandle(point, 10f, 10f);
        float dot = point.inHandleX * point.outHandleY - point.inHandleY * point.outHandleX;
        assertEquals(0f, dot, 0.001f);
        assertTrue(point.inHandleX < 0f || point.inHandleY < 0f);
    }

    @Test
    public void autoGeneratesNonZeroHandlesForMiddlePoints() {
        SplinePath path = createOpenPath();
        BezierHandleService.autoGenerateHandles(path);
        SplineControlPoint middle = path.getPoints().get(1);
        assertTrue(Math.abs(middle.inHandleX) > 0.001f || Math.abs(middle.inHandleY) > 0.001f);
        assertTrue(Math.abs(middle.outHandleX) > 0.001f || Math.abs(middle.outHandleY) > 0.001f);
    }

    @Test
    public void openAndClosedPathsHandleEndpointsSafely() {
        SplinePath openPath = createOpenPath();
        BezierHandleService.autoGenerateHandles(openPath);
        assertEquals(0f, openPath.getPoints().get(0).inHandleX, 0.001f);
        assertEquals(0f, openPath.getPoints().get(2).outHandleX, 0.001f);

        SplinePath closedPath = createClosedPath();
        BezierHandleService.autoGenerateHandles(closedPath);
        assertTrue(Math.abs(closedPath.getPoints().get(0).inHandleX) > 0.001f
            || Math.abs(closedPath.getPoints().get(0).inHandleY) > 0.001f);
    }

    private SplinePath createOpenPath() {
        List<SplineControlPoint> points = new ArrayList<>();
        points.add(new SplineControlPoint("a", 0f, 0f));
        points.add(new SplineControlPoint("b", 50f, 30f));
        points.add(new SplineControlPoint("c", 100f, 0f));
        return new SplinePath("path", "Open", points, SplineCurveType.BEZIER, false, true, 4f, "default", null);
    }

    private SplinePath createClosedPath() {
        List<SplineControlPoint> points = new ArrayList<>();
        points.add(new SplineControlPoint("a", 0f, 0f));
        points.add(new SplineControlPoint("b", 50f, 30f));
        points.add(new SplineControlPoint("c", 100f, 0f));
        return new SplinePath("path", "Closed", points, SplineCurveType.BEZIER, true, true, 4f, "default", null);
    }
}
