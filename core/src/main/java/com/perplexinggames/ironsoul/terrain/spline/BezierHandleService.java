package com.perplexinggames.ironsoul.terrain.spline;

import com.badlogic.gdx.math.Vector2;

import java.util.List;

public final class BezierHandleService {
    private static final float AUTO_SCALE = 0.25f;
    private static final float EPSILON = 0.001f;

    private BezierHandleService() {
    }

    public static void setInHandle(SplineControlPoint point, float newX, float newY) {
        if (point == null) {
            return;
        }
        if (point.handleMode == BezierHandleMode.AUTO) {
            point.handleMode = BezierHandleMode.FREE;
        }
        point.inHandleX = newX;
        point.inHandleY = newY;
        applyHandleModeAfterInChanged(point);
    }

    public static void setOutHandle(SplineControlPoint point, float newX, float newY) {
        if (point == null) {
            return;
        }
        if (point.handleMode == BezierHandleMode.AUTO) {
            point.handleMode = BezierHandleMode.FREE;
        }
        point.outHandleX = newX;
        point.outHandleY = newY;
        applyHandleModeAfterOutChanged(point);
    }

    public static void applyHandleModeAfterInChanged(SplineControlPoint point) {
        if (point == null) {
            return;
        }
        Vector2 changed = point.getInHandleOffset();
        switch (point.handleMode == null ? BezierHandleMode.AUTO : point.handleMode) {
            case MIRRORED -> {
                point.outHandleX = -changed.x;
                point.outHandleY = -changed.y;
            }
            case ALIGNED -> {
                float oppositeLength = point.getOutHandleOffset().len();
                if (oppositeLength <= EPSILON) {
                    oppositeLength = changed.len();
                }
                Vector2 direction = new Vector2(changed).scl(-1f);
                if (direction.len2() > EPSILON) {
                    direction.nor().scl(oppositeLength);
                    point.outHandleX = direction.x;
                    point.outHandleY = direction.y;
                }
            }
            case FREE, AUTO -> {
            }
        }
    }

    public static void applyHandleModeAfterOutChanged(SplineControlPoint point) {
        if (point == null) {
            return;
        }
        Vector2 changed = point.getOutHandleOffset();
        switch (point.handleMode == null ? BezierHandleMode.AUTO : point.handleMode) {
            case MIRRORED -> {
                point.inHandleX = -changed.x;
                point.inHandleY = -changed.y;
            }
            case ALIGNED -> {
                float oppositeLength = point.getInHandleOffset().len();
                if (oppositeLength <= EPSILON) {
                    oppositeLength = changed.len();
                }
                Vector2 direction = new Vector2(changed).scl(-1f);
                if (direction.len2() > EPSILON) {
                    direction.nor().scl(oppositeLength);
                    point.inHandleX = direction.x;
                    point.inHandleY = direction.y;
                }
            }
            case FREE, AUTO -> {
            }
        }
    }

    public static void autoGenerateHandles(SplinePath path) {
        if (path == null || path.points == null || path.points.isEmpty()) {
            return;
        }
        List<SplineControlPoint> points = path.points;
        for (int i = 0; i < points.size(); i++) {
            SplineControlPoint point = points.get(i);
            if (point == null) {
                continue;
            }
            point.handleMode = BezierHandleMode.AUTO;
            applyAutoHandles(path, i);
        }
    }

    public static void ensureValidHandles(SplinePath path) {
        if (path == null || path.points == null) {
            return;
        }
        for (int i = 0; i < path.points.size(); i++) {
            SplineControlPoint point = path.points.get(i);
            if (point == null) {
                continue;
            }
            point.handleMode = point.handleMode == null ? BezierHandleMode.AUTO : point.handleMode;
            if (path.curveType == SplineCurveType.BEZIER && point.handleMode == BezierHandleMode.AUTO) {
                applyAutoHandles(path, i);
            }
        }
    }

    public static void resetHandles(SplinePath path, String pointId) {
        if (path == null || pointId == null) {
            return;
        }
        for (int i = 0; i < path.points.size(); i++) {
            SplineControlPoint point = path.points.get(i);
            if (point != null && pointId.equals(point.id)) {
                point.handleMode = BezierHandleMode.AUTO;
                applyAutoHandles(path, i);
                return;
            }
        }
    }

    public static void applyAutoHandles(SplinePath path, int pointIndex) {
        if (path == null || path.points == null || pointIndex < 0 || pointIndex >= path.points.size()) {
            return;
        }
        List<SplineControlPoint> points = path.points;
        SplineControlPoint point = points.get(pointIndex);
        if (point == null) {
            return;
        }
        if (points.size() == 1) {
            point.inHandleX = 0f;
            point.inHandleY = 0f;
            point.outHandleX = 0f;
            point.outHandleY = 0f;
            return;
        }

        SplineControlPoint previous = getPreviousPoint(points, pointIndex, path.closed);
        SplineControlPoint next = getNextPoint(points, pointIndex, path.closed);
        if (previous == null && next == null) {
            point.inHandleX = 0f;
            point.inHandleY = 0f;
            point.outHandleX = 0f;
            point.outHandleY = 0f;
            return;
        }

        if (!path.closed && pointIndex == 0 && next != null) {
            Vector2 direction = new Vector2(next.x - point.x, next.y - point.y);
            point.inHandleX = 0f;
            point.inHandleY = 0f;
            setVector(point, direction, direction.len() * AUTO_SCALE, false);
            return;
        }
        if (!path.closed && pointIndex == points.size() - 1 && previous != null) {
            Vector2 direction = new Vector2(previous.x - point.x, previous.y - point.y);
            point.outHandleX = 0f;
            point.outHandleY = 0f;
            setVector(point, direction, direction.len() * AUTO_SCALE, true);
            return;
        }

        if (previous == null || next == null) {
            point.inHandleX = 0f;
            point.inHandleY = 0f;
            point.outHandleX = 0f;
            point.outHandleY = 0f;
            return;
        }

        Vector2 prevVector = new Vector2(previous.x - point.x, previous.y - point.y);
        Vector2 nextVector = new Vector2(next.x - point.x, next.y - point.y);
        Vector2 direction = new Vector2(next.x - previous.x, next.y - previous.y);
        if (direction.len2() <= EPSILON) {
            direction = new Vector2(nextVector);
        }
        if (direction.len2() <= EPSILON) {
            direction = new Vector2(prevVector).scl(-1f);
        }
        if (direction.len2() <= EPSILON) {
            point.inHandleX = 0f;
            point.inHandleY = 0f;
            point.outHandleX = 0f;
            point.outHandleY = 0f;
            return;
        }

        direction.nor();
        setVector(point, new Vector2(direction).scl(-1f), prevVector.len() * AUTO_SCALE, true);
        setVector(point, direction, nextVector.len() * AUTO_SCALE, false);
    }

    private static void setVector(SplineControlPoint point, Vector2 direction, float length, boolean inHandle) {
        Vector2 vector = direction.len2() <= EPSILON ? new Vector2() : direction.nor().scl(length);
        if (inHandle) {
            point.inHandleX = vector.x;
            point.inHandleY = vector.y;
        } else {
            point.outHandleX = vector.x;
            point.outHandleY = vector.y;
        }
    }

    private static SplineControlPoint getPreviousPoint(List<SplineControlPoint> points, int index, boolean closed) {
        if (closed) {
            return points.get((index - 1 + points.size()) % points.size());
        }
        return index > 0 ? points.get(index - 1) : null;
    }

    private static SplineControlPoint getNextPoint(List<SplineControlPoint> points, int index, boolean closed) {
        if (closed) {
            return points.get((index + 1) % points.size());
        }
        return index < points.size() - 1 ? points.get(index + 1) : null;
    }
}
