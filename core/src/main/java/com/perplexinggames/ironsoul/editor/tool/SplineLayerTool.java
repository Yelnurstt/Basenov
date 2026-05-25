package com.perplexinggames.ironsoul.editor.tool;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.terrain.spline.SplineControlPoint;

public class SplineLayerTool implements EditorToolStrategy {
    @Override
    public String getName() {
        return "SPLINE_LAYER";
    }

    @Override
    public void onMouseDown(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY, int button) {
        SplineControlPoint point = levelEditor.findSplinePointNear(worldX, worldY);
        if (point != null) {
            levelEditor.selectSplinePoint(point.id);
        }
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
}
