package com.perplexinggames.ironsoul.editor.tool;

import com.badlogic.gdx.Input;
import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.editor.command.DeleteSplinePointCommand;
import com.perplexinggames.ironsoul.editor.command.MoveSplinePointCommand;
import com.perplexinggames.ironsoul.terrain.spline.SplineControlPoint;

public class SplineEditTool implements EditorToolStrategy {
    private String draggingPointId;
    private String draggingPathId;

    @Override
    public String getName() {
        return "SPLINE_EDIT";
    }

    @Override
    public void onMouseDown(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY, int button) {
        SplineControlPoint nearestPoint = levelEditor.findSplinePointNear(worldX, worldY);
        if (nearestPoint == null) {
            return;
        }

        if (button == Input.Buttons.RIGHT) {
            if (levelEditor.getSelectedSplinePathId() != null) {
                levelEditor.executeCommand(new DeleteSplinePointCommand(levelEditor,
                    levelEditor.getSelectedSplinePathId(), nearestPoint.id));
            }
            return;
        }

        if (button != Input.Buttons.LEFT) {
            return;
        }

        draggingPointId = nearestPoint.id;
        draggingPathId = levelEditor.findSplinePathIdForPoint(nearestPoint.id);
        if (draggingPathId != null) {
            levelEditor.selectSplinePath(draggingPathId);
        }
        levelEditor.selectSplinePoint(draggingPointId);
    }

    @Override
    public void onMouseDrag(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY) {
        if (draggingPointId == null || draggingPathId == null) {
            return;
        }
        levelEditor.executeCommand(new MoveSplinePointCommand(levelEditor, draggingPathId, draggingPointId, worldX, worldY));
    }

    @Override
    public void onMouseUp(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY, int button) {
        if (button == Input.Buttons.LEFT) {
            draggingPointId = null;
            draggingPathId = null;
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
