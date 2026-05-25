package com.perplexinggames.ironsoul.editor.tool;

import com.badlogic.gdx.Input;
import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.editor.command.DeleteSplinePointCommand;
import com.perplexinggames.ironsoul.editor.command.MoveSplineHandleCommand;
import com.perplexinggames.ironsoul.editor.command.MoveSplinePointCommand;
import com.perplexinggames.ironsoul.editor.command.ResetSplineHandlesCommand;
import com.perplexinggames.ironsoul.terrain.spline.BezierHandleType;
import com.perplexinggames.ironsoul.terrain.spline.SplineControlPoint;
import com.perplexinggames.ironsoul.terrain.spline.SplineHandleHit;

public class SplineEditTool implements EditorToolStrategy {
    private String draggingPointId;
    private String draggingPathId;
    private BezierHandleType draggingHandleType;

    @Override
    public String getName() {
        return "SPLINE_EDIT";
    }

    @Override
    public void onMouseDown(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY, int button) {
        SplineHandleHit handleHit = levelEditor.findSplineHandleNear(worldX, worldY);
        SplineControlPoint nearestPoint = levelEditor.findSplinePointNear(worldX, worldY);
        if (handleHit == null && nearestPoint == null) {
            return;
        }

        if (button == Input.Buttons.RIGHT) {
            if (handleHit != null) {
                levelEditor.executeCommand(new ResetSplineHandlesCommand(levelEditor, handleHit.pathId, handleHit.pointId));
                return;
            }
            if (levelEditor.getSelectedSplinePathId() != null) {
                levelEditor.executeCommand(new DeleteSplinePointCommand(levelEditor,
                    levelEditor.getSelectedSplinePathId(), nearestPoint.id));
            }
            return;
        }

        if (button != Input.Buttons.LEFT) {
            return;
        }

        if (handleHit != null) {
            draggingPointId = handleHit.pointId;
            draggingPathId = handleHit.pathId;
            draggingHandleType = handleHit.handleType;
            levelEditor.selectSplinePath(draggingPathId);
            levelEditor.selectSplineHandle(draggingPointId, draggingHandleType);
            return;
        }

        draggingPointId = nearestPoint.id;
        draggingPathId = levelEditor.findSplinePathIdForPoint(nearestPoint.id);
        if (draggingPathId != null) {
            levelEditor.selectSplinePath(draggingPathId);
        }
        draggingHandleType = null;
        levelEditor.selectSplinePoint(draggingPointId);
    }

    @Override
    public void onMouseDrag(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY) {
        if (draggingPointId == null || draggingPathId == null) {
            return;
        }
        if (draggingHandleType != null) {
            levelEditor.executeCommand(new MoveSplineHandleCommand(levelEditor, draggingPathId, draggingPointId,
                draggingHandleType, worldX, worldY));
            return;
        }
        levelEditor.executeCommand(new MoveSplinePointCommand(levelEditor, draggingPathId, draggingPointId, worldX, worldY));
    }

    @Override
    public void onMouseUp(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY, int button) {
        if (button == Input.Buttons.LEFT) {
            draggingPointId = null;
            draggingPathId = null;
            draggingHandleType = null;
        }
    }

    @Override
    public void onMouseMove(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY) {
    }

    @Override
    public boolean usesContinuousWorldDrag() {
        return true;
    }
}
