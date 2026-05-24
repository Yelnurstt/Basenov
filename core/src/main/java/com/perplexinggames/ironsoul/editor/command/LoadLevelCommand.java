package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.world.WorldData;

public class LoadLevelCommand implements EditorCommand {
    private final LevelEditor levelEditor;
    private WorldData previousWorldData;
    private WorldData loadedWorldData;
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
        previousWorldData = levelEditor.snapshotWorldData();

        if (loadedWorldData == null) {
            loadedWorldData = levelEditor.readWorldDataFromDefaultLocation();
            sourceDescription = levelEditor.getLastLoadSourceDescription();
            emptyFallback = levelEditor.wasLastLoadEmptyFallback();
        }

        levelEditor.applyLoadedWorld(loadedWorldData.copy(), sourceDescription, emptyFallback);
        changedState = true;
    }

    @Override
    public void undo() {
        if (!changedState || previousWorldData == null) {
            return;
        }
        levelEditor.applyLoadedWorld(previousWorldData.copy(), "undo snapshot", false);
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
