package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.level.BlockData;
import com.perplexinggames.ironsoul.level.BlockType;

public class PlaceBlockCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private final int gridX;
    private final int gridY;
    private final BlockType blockType;
    private BlockData previousBlock;
    private boolean changedState;

    public PlaceBlockCommand(LevelEditor levelEditor, int gridX, int gridY, BlockType blockType) {
        this.levelEditor = levelEditor;
        this.gridX = gridX;
        this.gridY = gridY;
        this.blockType = blockType;
    }

    @Override
    public String getName() {
        return "PLACE_BLOCK";
    }

    @Override
    public void execute() {
        previousBlock = levelEditor.getRuntimeLevel().getBlockCopy(gridX, gridY);
        changedState = levelEditor.placeBlock(gridX, gridY, blockType);
    }

    @Override
    public void undo() {
        if (!changedState) {
            return;
        }

        if (previousBlock == null) {
            levelEditor.eraseBlock(gridX, gridY);
        } else {
            levelEditor.restoreBlock(previousBlock);
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
