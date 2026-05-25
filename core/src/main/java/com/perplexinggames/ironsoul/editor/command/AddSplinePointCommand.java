package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.terrain.spline.SplinePath;

public class AddSplinePointCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final String pathId;
    private final String pointId;
    private final float worldX;
    private final float worldY;
    private SplinePath previousPath;
    private SplinePath nextPath;
    private boolean changedState;

    public AddSplinePointCommand(LevelEditor levelEditor, String pathId, String pointId, float worldX, float worldY) {
        this.levelEditor = levelEditor;
        this.pathId = pathId;
        this.pointId = pointId;
        this.worldX = worldX;
        this.worldY = worldY;
    }

    @Override
    public String getName() {
        return "ADD_SPLINE_POINT";
    }

    @Override
    public void execute() {
        previousPath = levelEditor.getSplinePaths().stream().filter(path -> pathId.equals(path.id)).findFirst().orElse(null);
        nextPath = levelEditor.buildSplinePathWithAddedPoint(pathId, pointId, worldX, worldY);
        changedState = nextPath != null && levelEditor.replaceSplinePath(nextPath);
        if (changedState) {
            levelEditor.selectSplinePath(pathId);
            levelEditor.selectSplinePoint(pointId);
        }
    }

    @Override
    public void undo() {
        if (!changedState || previousPath == null) {
            return;
        }
        levelEditor.replaceSplinePath(previousPath);
        levelEditor.selectSplinePoint(null);
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
