package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.terrain.spline.SplineLayer;

public class UpdateSplineLayerPropertiesCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final SplineLayer nextLayer;
    private SplineLayer previousLayer;
    private boolean changedState;

    public UpdateSplineLayerPropertiesCommand(LevelEditor levelEditor, SplineLayer nextLayer) {
        this.levelEditor = levelEditor;
        this.nextLayer = nextLayer;
    }

    @Override
    public String getName() {
        return "UPDATE_SPLINE_LAYER_PROPERTIES";
    }

    @Override
    public void execute() {
        previousLayer = levelEditor.getSelectedSplineLayer();
        if (previousLayer == null || !nextLayer.id.equals(previousLayer.id)) {
            previousLayer = levelEditor.getSplineLayersForPath(nextLayer.parentSplinePathId).stream()
                .filter(layer -> nextLayer.id.equals(layer.id))
                .findFirst()
                .orElse(null);
        }
        changedState = levelEditor.replaceSplineLayer(nextLayer);
    }

    @Override
    public void undo() {
        if (changedState && previousLayer != null) {
            levelEditor.replaceSplineLayer(previousLayer);
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
