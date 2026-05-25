package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.terrain.spline.BezierHandleType;
import com.perplexinggames.ironsoul.terrain.spline.SplineControlPoint;
import com.perplexinggames.ironsoul.terrain.spline.SplinePath;

public class MoveSplineHandleCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final String pathId;
    private final String pointId;
    private final BezierHandleType handleType;
    private final float worldX;
    private final float worldY;
    private SplinePath previousPath;
    private SplinePath nextPath;
    private float oldHandleX;
    private float oldHandleY;
    private float newHandleX;
    private float newHandleY;
    private float oldOppositeHandleX;
    private float oldOppositeHandleY;
    private float newOppositeHandleX;
    private float newOppositeHandleY;
    private boolean changedState;

    public MoveSplineHandleCommand(LevelEditor levelEditor, String pathId, String pointId, BezierHandleType handleType,
                                   float worldX, float worldY) {
        this.levelEditor = levelEditor;
        this.pathId = pathId;
        this.pointId = pointId;
        this.handleType = handleType;
        this.worldX = worldX;
        this.worldY = worldY;
    }

    @Override
    public String getName() {
        return "MOVE_SPLINE_HANDLE";
    }

    @Override
    public void execute() {
        previousPath = levelEditor.getSplinePaths().stream().filter(path -> pathId.equals(path.id)).findFirst().orElse(null);
        cacheHandleValues(previousPath, true);
        nextPath = levelEditor.buildSplinePathWithMovedHandle(pathId, pointId, handleType, worldX, worldY);
        cacheHandleValues(nextPath, false);
        changedState = nextPath != null && levelEditor.replaceSplinePath(nextPath);
        if (changedState) {
            levelEditor.selectSplinePath(pathId);
            levelEditor.selectSplineHandle(pointId, handleType);
        }
    }

    @Override
    public void undo() {
        if (!changedState || previousPath == null) {
            return;
        }
        levelEditor.replaceSplinePath(previousPath);
        levelEditor.selectSplineHandle(pointId, handleType);
    }

    @Override
    public boolean canUndo() {
        return true;
    }

    @Override
    public boolean didChangeState() {
        return changedState;
    }

    private void cacheHandleValues(SplinePath path, boolean oldValues) {
        if (path == null) {
            return;
        }
        for (SplineControlPoint point : path.getPoints()) {
            if (!pointId.equals(point.id)) {
                continue;
            }
            if (handleType == BezierHandleType.IN) {
                if (oldValues) {
                    oldHandleX = point.inHandleX;
                    oldHandleY = point.inHandleY;
                    oldOppositeHandleX = point.outHandleX;
                    oldOppositeHandleY = point.outHandleY;
                } else {
                    newHandleX = point.inHandleX;
                    newHandleY = point.inHandleY;
                    newOppositeHandleX = point.outHandleX;
                    newOppositeHandleY = point.outHandleY;
                }
            } else {
                if (oldValues) {
                    oldHandleX = point.outHandleX;
                    oldHandleY = point.outHandleY;
                    oldOppositeHandleX = point.inHandleX;
                    oldOppositeHandleY = point.inHandleY;
                } else {
                    newHandleX = point.outHandleX;
                    newHandleY = point.outHandleY;
                    newOppositeHandleX = point.inHandleX;
                    newOppositeHandleY = point.inHandleY;
                }
            }
            return;
        }
    }
}
