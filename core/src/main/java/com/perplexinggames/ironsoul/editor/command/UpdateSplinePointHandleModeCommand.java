package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.terrain.spline.BezierHandleMode;
import com.perplexinggames.ironsoul.terrain.spline.SplinePath;

public class UpdateSplinePointHandleModeCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final String pathId;
    private final String pointId;
    private final BezierHandleMode nextHandleMode;
    private SplinePath previousPath;
    private SplinePath nextPath;
    private boolean changedState;

    public UpdateSplinePointHandleModeCommand(LevelEditor levelEditor, String pathId, String pointId, BezierHandleMode nextHandleMode) {
        this.levelEditor = levelEditor;
        this.pathId = pathId;
        this.pointId = pointId;
        this.nextHandleMode = nextHandleMode;
    }

    @Override
    public String getName() {
        return "UPDATE_SPLINE_POINT_HANDLE_MODE";
    }

    @Override
    public void execute() {
        previousPath = levelEditor.getSplinePaths().stream().filter(path -> pathId.equals(path.id)).findFirst().orElse(null);
        nextPath = levelEditor.updateSplinePointHandleMode(pathId, pointId, nextHandleMode);
        changedState = nextPath != null && levelEditor.replaceSplinePath(nextPath);
        if (changedState) {
            levelEditor.selectSplinePath(pathId);
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
