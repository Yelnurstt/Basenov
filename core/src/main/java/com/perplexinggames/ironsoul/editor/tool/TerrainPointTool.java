package com.perplexinggames.ironsoul.editor.tool;

import com.badlogic.gdx.Input;
import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.editor.command.AddTerrainPointCommand;
import com.perplexinggames.ironsoul.editor.command.MoveTerrainPointCommand;
import com.perplexinggames.ironsoul.editor.command.RemoveTerrainPointCommand;
import com.perplexinggames.ironsoul.terrain.TerrainPoint;

public class TerrainPointTool implements EditorToolStrategy {
    private String draggingPointId;

    @Override
    public String getName() {
        return "TERRAIN_POINTS";
    }

    @Override
    public void onMouseDown(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY, int button) {
        TerrainPoint nearestPoint = levelEditor.findTerrainPointNear(worldX, worldY);

        if (button == Input.Buttons.RIGHT) {
            if (nearestPoint != null) {
                levelEditor.executeCommand(new RemoveTerrainPointCommand(levelEditor, nearestPoint.getId()));
            }
            draggingPointId = null;
            return;
        }

        if (button != Input.Buttons.LEFT) {
            return;
        }

        if (nearestPoint != null) {
            draggingPointId = nearestPoint.getId();
            levelEditor.selectTerrainPoint(draggingPointId);
            return;
        }

        draggingPointId = levelEditor.createTerrainPointId();
        levelEditor.executeCommand(new AddTerrainPointCommand(levelEditor, draggingPointId, worldX, worldY));
    }

    @Override
    public void onMouseDrag(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY) {
        if (draggingPointId == null) {
            return;
        }
        levelEditor.executeCommand(new MoveTerrainPointCommand(levelEditor, draggingPointId, worldX, worldY));
    }

    @Override
    public void onMouseUp(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY, int button) {
        if (button == Input.Buttons.LEFT) {
            draggingPointId = null;
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
