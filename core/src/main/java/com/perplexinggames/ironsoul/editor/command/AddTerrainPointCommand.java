package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.terrain.TerrainPath;

public class AddTerrainPointCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final String pointId;
    private final float worldX;
    private final float worldY;
    private TerrainPath previousPath;
    private TerrainPath nextPath;
    private boolean changedState;

    public AddTerrainPointCommand(LevelEditor levelEditor, String pointId, float worldX, float worldY) {
        this.levelEditor = levelEditor;
        this.pointId = pointId;
        this.worldX = worldX;
        this.worldY = worldY;
    }

    @Override
    public String getName() {
        return "ADD_TERRAIN_POINT";
    }

    @Override
    public void execute() {
        previousPath = levelEditor.getPrimaryTerrainPath();
        nextPath = levelEditor.buildPrimaryTerrainPathWithAddedPoint(pointId, worldX, worldY);
        changedState = levelEditor.replacePrimaryTerrainPath(nextPath);
        if (changedState) {
            levelEditor.selectTerrainPoint(pointId);
        }
    }

    @Override
    public void undo() {
        if (!changedState) {
            return;
        }
        levelEditor.replacePrimaryTerrainPath(previousPath);
        levelEditor.selectTerrainPoint(null);
    }

    @Override
    public boolean canUndo() {
        return true;
    }

    @Override
    public boolean didChangeState() {
        return changedState;
    }
}
