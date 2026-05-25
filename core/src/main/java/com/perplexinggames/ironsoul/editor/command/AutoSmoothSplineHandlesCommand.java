package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.terrain.spline.SplinePath;

public class AutoSmoothSplineHandlesCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final String pathId;
    private SplinePath previousPath;
    private SplinePath nextPath;
    private boolean changedState;

    public AutoSmoothSplineHandlesCommand(LevelEditor levelEditor, String pathId) {
        this.levelEditor = levelEditor;
        this.pathId = pathId;
    }

    @Override
    public String getName() {
        return "AUTO_SMOOTH_SPLINE_HANDLES";
    }

    @Override
    public void execute() {
        previousPath = levelEditor.getSplinePaths().stream().filter(path -> pathId.equals(path.id)).findFirst().orElse(null);
        nextPath = levelEditor.buildSplinePathWithAutoSmooth(pathId);
        changedState = nextPath != null && levelEditor.replaceSplinePath(nextPath);
    }

    @Override
    public void undo() {
        if (changedState && previousPath != null) {
            levelEditor.replaceSplinePath(previousPath);
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
