package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.terrain.TerrainPath;

public class RemoveTerrainPointCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final String pointId;
    private TerrainPath previousPath;
    private TerrainPath nextPath;
    private boolean changedState;

    public RemoveTerrainPointCommand(LevelEditor levelEditor, String pointId) {
        this.levelEditor = levelEditor;
        this.pointId = pointId;
    }

    @Override
    public String getName() {
        return "REMOVE_TERRAIN_POINT";
    }

    @Override
    public void execute() {
        previousPath = levelEditor.getPrimaryTerrainPath();
        nextPath = levelEditor.buildPrimaryTerrainPathWithRemovedPoint(pointId);
        if (previousPath == null || nextPath == null) {
            changedState = false;
            return;
        }
        changedState = levelEditor.replacePrimaryTerrainPath(nextPath);
        if (changedState) {
            levelEditor.selectTerrainPoint(null);
        }
    }

    @Override
    public void undo() {
        if (!changedState) {
            return;
        }
        levelEditor.replacePrimaryTerrainPath(previousPath);
        levelEditor.selectTerrainPoint(pointId);
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
