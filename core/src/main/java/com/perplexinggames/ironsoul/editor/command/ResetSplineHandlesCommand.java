package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.terrain.spline.SplinePath;

public class ResetSplineHandlesCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final String pathId;
    private final String pointId;
    private SplinePath previousPath;
    private SplinePath nextPath;
    private boolean changedState;

    public ResetSplineHandlesCommand(LevelEditor levelEditor, String pathId, String pointId) {
        this.levelEditor = levelEditor;
        this.pathId = pathId;
        this.pointId = pointId;
    }

    @Override
    public String getName() {
        return "RESET_SPLINE_HANDLES";
    }

    @Override
    public void execute() {
        previousPath = levelEditor.getSplinePaths().stream().filter(path -> pathId.equals(path.id)).findFirst().orElse(null);
        nextPath = levelEditor.buildSplinePathWithResetHandles(pathId, pointId);
        changedState = nextPath != null && levelEditor.replaceSplinePath(nextPath);
        if (changedState) {
            levelEditor.selectSplinePoint(pointId);
        }
    }

    @Override
    public void undo() {
        if (changedState && previousPath != null) {
            levelEditor.replaceSplinePath(previousPath);
            levelEditor.selectSplinePoint(pointId);
        }
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
