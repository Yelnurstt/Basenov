package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.terrain.spline.SplineLayer;

public class AddSplineLayerCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final SplineLayer splineLayer;
    private boolean changedState;

    public AddSplineLayerCommand(LevelEditor levelEditor, SplineLayer splineLayer) {
        this.levelEditor = levelEditor;
        this.splineLayer = splineLayer;
    }

    @Override
    public String getName() {
        return "ADD_SPLINE_LAYER";
    }

    @Override
    public void execute() {
        changedState = levelEditor.replaceSplineLayer(splineLayer);
        if (changedState) {
            levelEditor.selectSplineLayer(splineLayer.id);
        }
    }

    @Override
    public void undo() {
        if (changedState) {
            levelEditor.deleteSplineLayer(splineLayer.id);
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
