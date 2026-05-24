package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.terrain.TerrainPath;

public class AddTerrainPointCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final String pointId;
    private final int gridX;
    private final int gridY;
    private TerrainPath previousPath;
    private TerrainPath nextPath;
    private boolean changedState;

    public AddTerrainPointCommand(LevelEditor levelEditor, String pointId, int gridX, int gridY) {
        this.levelEditor = levelEditor;
        this.pointId = pointId;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    @Override
    public String getName() {
        return "ADD_TERRAIN_POINT";
    }

    @Override
    public void execute() {
        previousPath = levelEditor.getPrimaryTerrainPath();
        nextPath = levelEditor.buildPrimaryTerrainPathWithAddedPoint(pointId, gridX, gridY);
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
