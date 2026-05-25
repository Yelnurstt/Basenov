package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.terrain.spline.SplineLayer;
import com.perplexinggames.ironsoul.terrain.spline.SplinePath;

import java.util.ArrayList;
import java.util.List;

public class DeleteSplinePathCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final String pathId;
    private SplinePath previousPath;
    private List<SplineLayer> previousLayers;
    private boolean changedState;

    public DeleteSplinePathCommand(LevelEditor levelEditor, String pathId) {
        this.levelEditor = levelEditor;
        this.pathId = pathId;
    }

    @Override
    public String getName() {
        return "DELETE_SPLINE_PATH";
    }

    @Override
    public void execute() {
        previousPath = levelEditor.getSelectedSplinePath();
        if (previousPath == null || !pathId.equals(previousPath.id)) {
            previousPath = levelEditor.getSplinePaths().stream().filter(path -> pathId.equals(path.id)).findFirst().orElse(null);
        }
        previousLayers = new ArrayList<>(levelEditor.getSplineLayersForPath(pathId));
        changedState = previousPath != null && levelEditor.deleteSplinePath(pathId);
    }

    @Override
    public void undo() {
        if (!changedState || previousPath == null) {
            return;
        }
        levelEditor.replaceSplinePath(previousPath);
        for (SplineLayer layer : previousLayers) {
            levelEditor.replaceSplineLayer(layer);
        }
        levelEditor.selectSplinePath(previousPath.id);
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
