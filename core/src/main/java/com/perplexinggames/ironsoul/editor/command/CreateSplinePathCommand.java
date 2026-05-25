package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.terrain.spline.SplineLayer;
import com.perplexinggames.ironsoul.terrain.spline.SplinePath;

public class CreateSplinePathCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final SplinePath splinePath;
    private final SplineLayer defaultLayer;
    private boolean changedState;

    public CreateSplinePathCommand(LevelEditor levelEditor, SplinePath splinePath, SplineLayer defaultLayer) {
        this.levelEditor = levelEditor;
        this.splinePath = splinePath;
        this.defaultLayer = defaultLayer;
    }

    @Override
    public String getName() {
        return "CREATE_SPLINE_PATH";
    }

    @Override
    public void execute() {
        changedState = levelEditor.replaceSplinePath(splinePath);
        if (defaultLayer != null) {
            changedState = levelEditor.replaceSplineLayer(defaultLayer) || changedState;
        }
        if (changedState) {
            levelEditor.selectSplinePath(splinePath.id);
            if (defaultLayer != null) {
                levelEditor.selectSplineLayer(defaultLayer.id);
            }
        }
    }

    @Override
    public void undo() {
        if (!changedState) {
            return;
        }
        if (defaultLayer != null) {
            levelEditor.deleteSplineLayer(defaultLayer.id);
        }
        levelEditor.deleteSplinePath(splinePath.id);
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
