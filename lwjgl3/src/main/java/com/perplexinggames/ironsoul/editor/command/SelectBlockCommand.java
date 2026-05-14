package com.perplexinggames.ironsoul.editor.command;

import com.badlogic.gdx.math.GridPoint2;
import com.perplexinggames.ironsoul.editor.LevelEditor;

public class SelectBlockCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final int gridX;
    private final int gridY;
    private GridPoint2 previousSelection;
    private boolean changedState;

    public SelectBlockCommand(LevelEditor levelEditor, int gridX, int gridY) {
        this.levelEditor = levelEditor;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    @Override
    public String getName() {
        return "SELECT_BLOCK";
    }

    @Override
    public void execute() {
        previousSelection = levelEditor.getSelectedCell();
        changedState = levelEditor.selectBlock(gridX, gridY);
    }

    @Override
    public void undo() {
        levelEditor.restoreSelection(previousSelection);
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
