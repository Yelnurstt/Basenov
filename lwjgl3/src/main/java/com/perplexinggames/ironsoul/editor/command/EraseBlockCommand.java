package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.level.BlockData;

public class EraseBlockCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final int gridX;
    private final int gridY;
    private BlockData removedBlock;
    private boolean changedState;

    public EraseBlockCommand(LevelEditor levelEditor, int gridX, int gridY) {
        this.levelEditor = levelEditor;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    @Override
    public String getName() {
        return "ERASE_BLOCK";
    }

    @Override
    public void execute() {
        removedBlock = levelEditor.getRuntimeLevel().getBlockCopy(gridX, gridY);
        changedState = removedBlock != null && levelEditor.eraseBlock(gridX, gridY);
    }

    @Override
    public void undo() {
        if (!changedState || removedBlock == null) {
            return;
        }
        levelEditor.restoreBlock(removedBlock);
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
