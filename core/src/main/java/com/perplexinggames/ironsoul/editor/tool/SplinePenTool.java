package com.perplexinggames.ironsoul.editor.tool;

import com.badlogic.gdx.Input;
import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.editor.command.AddSplinePointCommand;
import com.perplexinggames.ironsoul.editor.command.CreateSplinePathCommand;
import com.perplexinggames.ironsoul.terrain.spline.SplineControlPoint;
import com.perplexinggames.ironsoul.terrain.spline.SplineLayer;
import com.perplexinggames.ironsoul.terrain.spline.SplinePath;

public class SplinePenTool implements EditorToolStrategy {
    @Override
    public String getName() {
        return "SPLINE_PEN";
    }

    @Override
    public void onMouseDown(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY, int button) {
        if (button == Input.Buttons.RIGHT) {
            SplineControlPoint nearestPoint = levelEditor.findSplinePointNear(worldX, worldY);
            if (nearestPoint != null && levelEditor.getSelectedSplinePathId() != null) {
                levelEditor.deleteNonBlockAt(worldX, worldY);
            }
            return;
        }

        if (button != Input.Buttons.LEFT) {
            return;
        }

        if (levelEditor.getSelectedSplinePathId() == null) {
            String pathId = levelEditor.createSplinePathId();
            String layerId = levelEditor.createSplineLayerId();
            SplinePath splinePath = levelEditor.createDefaultSplinePath(pathId, "Spline " + pathId);
            SplineLayer mainLayer = levelEditor.createDefaultSplineLayer(layerId, pathId, "Main Terrain", 0);
            levelEditor.executeCommand(new CreateSplinePathCommand(levelEditor, splinePath, mainLayer));
        }

        String activePathId = levelEditor.getSelectedSplinePathId();
        if (activePathId == null) {
            return;
        }

        SplineControlPoint nearestPoint = levelEditor.findSplinePointNear(worldX, worldY);
        if (nearestPoint != null) {
            String pointPathId = levelEditor.findSplinePathIdForPoint(nearestPoint.id);
            levelEditor.selectSplinePath(pointPathId == null ? activePathId : pointPathId);
            levelEditor.selectSplinePoint(nearestPoint.id);
            return;
        }

        String pointId = levelEditor.createSplinePointId();
        levelEditor.executeCommand(new AddSplinePointCommand(levelEditor, activePathId, pointId, worldX, worldY));
    }

    @Override
    public void onMouseDrag(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY) {
    }

    @Override
    public void onMouseUp(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY, int button) {
    }

    @Override
    public void onMouseMove(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY) {
    }

    @Override
    public boolean usesContinuousWorldDrag() {
        return false;
    }
}
