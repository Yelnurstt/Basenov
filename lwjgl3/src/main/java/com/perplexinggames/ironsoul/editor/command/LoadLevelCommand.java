package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.level.LevelData;

public class LoadLevelCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private LevelData previousLevelData;
    private LevelData loadedLevelData;
    private String sourceDescription;
    private boolean emptyFallback;
    private boolean changedState;

    public LoadLevelCommand(LevelEditor levelEditor) {
        this.levelEditor = levelEditor;
    }

    @Override
    public String getName() {
        return "LOAD_LEVEL";
    }

    @Override
    public void execute() {
        previousLevelData = levelEditor.snapshotLevelData();

        if (loadedLevelData == null) {
            loadedLevelData = levelEditor.readLevelDataFromDefaultLocation();
            sourceDescription = levelEditor.getLastLoadSourceDescription();
            emptyFallback = levelEditor.wasLastLoadEmptyFallback();
        }

        levelEditor.applyLoadedLevel(loadedLevelData.copy(), sourceDescription, emptyFallback);
        changedState = true;
    }

    @Override
    public void undo() {
        if (!changedState || previousLevelData == null) {
            return;
        }
        levelEditor.applyLoadedLevel(previousLevelData.copy(), "undo snapshot", false);
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
