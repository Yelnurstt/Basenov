package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.terrain.spline.SplinePath;

public class ConvertSplineToBezierCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final String pathId;
    private SplinePath previousPath;
    private SplinePath nextPath;
    private boolean changedState;

    public ConvertSplineToBezierCommand(LevelEditor levelEditor, String pathId) {
        this.levelEditor = levelEditor;
        this.pathId = pathId;
    }

    @Override
    public String getName() {
        return "CONVERT_SPLINE_TO_BEZIER";
    }

    @Override
    public void execute() {
        previousPath = levelEditor.getSplinePaths().stream().filter(path -> pathId.equals(path.id)).findFirst().orElse(null);
        nextPath = levelEditor.buildSplinePathConvertedToBezier(pathId);
        changedState = nextPath != null && levelEditor.replaceSplinePath(nextPath);
        if (changedState) {
            levelEditor.selectSplinePath(pathId);
        }
    }

    @Override
    public void undo() {
        if (changedState && previousPath != null) {
            levelEditor.replaceSplinePath(previousPath);
            levelEditor.selectSplinePath(pathId);
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
