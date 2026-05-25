package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.terrain.spline.SplineLayer;

public class RemoveSplineLayerCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final String layerId;
    private SplineLayer previousLayer;
    private boolean changedState;

    public RemoveSplineLayerCommand(LevelEditor levelEditor, String layerId) {
        this.levelEditor = levelEditor;
        this.layerId = layerId;
    }

    @Override
    public String getName() {
        return "REMOVE_SPLINE_LAYER";
    }

    @Override
    public void execute() {
        previousLayer = levelEditor.getSelectedSplineLayer();
        if (previousLayer == null || !layerId.equals(previousLayer.id)) {
            previousLayer = levelEditor.getSplineLayersForPath(levelEditor.getSelectedSplinePathId()).stream()
                .filter(layer -> layerId.equals(layer.id))
                .findFirst()
                .orElse(null);
        }
        changedState = previousLayer != null && levelEditor.deleteSplineLayer(layerId);
    }

    @Override
    public void undo() {
        if (changedState && previousLayer != null) {
            levelEditor.replaceSplineLayer(previousLayer);
            levelEditor.selectSplineLayer(previousLayer.id);
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
