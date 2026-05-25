package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.terrain.spline.SplinePath;

public class UpdateSplinePathPropertiesCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final SplinePath nextPath;
    private SplinePath previousPath;
    private boolean changedState;

    public UpdateSplinePathPropertiesCommand(LevelEditor levelEditor, SplinePath nextPath) {
        this.levelEditor = levelEditor;
        this.nextPath = nextPath;
    }

    @Override
    public String getName() {
        return "UPDATE_SPLINE_PATH_PROPERTIES";
    }

    @Override
    public void execute() {
        previousPath = levelEditor.getSelectedSplinePath();
        if (previousPath == null || !nextPath.id.equals(previousPath.id)) {
            previousPath = levelEditor.getSplinePaths().stream().filter(path -> nextPath.id.equals(path.id)).findFirst().orElse(null);
        }
        changedState = levelEditor.replaceSplinePath(nextPath);
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
