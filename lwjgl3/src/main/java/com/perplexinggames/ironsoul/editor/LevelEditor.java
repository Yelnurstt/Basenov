package com.perplexinggames.ironsoul.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.perplexinggames.ironsoul.editor.command.EditorCommand;
import com.perplexinggames.ironsoul.editor.command.EditorCommandHistory;
import com.perplexinggames.ironsoul.editor.event.BlockErasedEvent;
import com.perplexinggames.ironsoul.editor.event.BlockPlacedEvent;
import com.perplexinggames.ironsoul.editor.event.EditorEventBus;
import com.perplexinggames.ironsoul.editor.event.EditorModeChangedEvent;
import com.perplexinggames.ironsoul.editor.event.LevelLoadedEvent;
import com.perplexinggames.ironsoul.editor.event.LevelSavedEvent;
import com.perplexinggames.ironsoul.editor.event.ToolChangedEvent;
import com.perplexinggames.ironsoul.editor.tool.EditorToolStrategy;
import com.perplexinggames.ironsoul.editor.tool.PlaceBlockTool;
import com.perplexinggames.ironsoul.level.BlockData;
import com.perplexinggames.ironsoul.level.BlockType;
import com.perplexinggames.ironsoul.level.LevelData;
import com.perplexinggames.ironsoul.level.RuntimeLevel;
import com.perplexinggames.ironsoul.level.serialization.LevelSerializer;

public class LevelEditor {
    public static final String DEFAULT_LEVEL_PATH = "levels/test-level.json";
    private static final int DEFAULT_LEVEL_WIDTH = 40;
    private static final int DEFAULT_LEVEL_HEIGHT = 30;
    private static final int DEFAULT_TILE_SIZE = 32;

    private final RuntimeLevel runtimeLevel;
    private final LevelSerializer levelSerializer;
    private final EditorEventBus eventBus;
    private final EditorCommandHistory commandHistory;
    private final EditorToolContext toolContext;

    private EditorMode mode;
    private GridPoint2 hoveredCell;
    private GridPoint2 selectedCell;
    private String lastStatusMessage;
    private String lastLoadSourceDescription;
    private boolean lastLoadEmptyFallback;

    public LevelEditor(RuntimeLevel runtimeLevel, LevelSerializer levelSerializer) {
        this.runtimeLevel = runtimeLevel;
        this.levelSerializer = levelSerializer;
        this.eventBus = new EditorEventBus();
        this.commandHistory = new EditorCommandHistory();
        this.toolContext = new EditorToolContext(new PlaceBlockTool());
        this.mode = EditorMode.GAMEPLAY;
        this.lastStatusMessage = "Ready";
        registerDebugListeners();
        eventBus.post(new ToolChangedEvent(getCurrentToolName()));
    }

    public void executeCommand(EditorCommand command) {
        commandHistory.execute(command);
    }

    public void undo() {
        String commandName = commandHistory.undo();
        if (commandName == null) {
            updateStatus("Nothing to undo");
            return;
        }
        updateStatus("Undo: " + commandName);
    }

    public void redo() {
        String commandName = commandHistory.redo();
        if (commandName == null) {
            updateStatus("Nothing to redo");
            return;
        }
        updateStatus("Redo: " + commandName);
    }

    public void clearHistory() {
        commandHistory.clear();
    }

    public void setMode(EditorMode mode) {
        if (this.mode == mode) {
            return;
        }

        EditorMode previousMode = this.mode;
        this.mode = mode;
        eventBus.post(new EditorModeChangedEvent(previousMode, mode));
    }

    public EditorMode getMode() {
        return mode;
    }

    public boolean isEditorMode() {
        return mode == EditorMode.EDITOR;
    }

    public void setTool(EditorToolStrategy toolStrategy) {
        toolContext.setActiveTool(toolStrategy);
        eventBus.post(new ToolChangedEvent(toolStrategy.getName()));
    }

    public EditorToolContext getToolContext() {
        return toolContext;
    }

    public String getCurrentToolName() {
        return toolContext.getActiveTool().getName();
    }

    public void setHoveredCell(int gridX, int gridY) {
        hoveredCell = new GridPoint2(gridX, gridY);
    }

    public GridPoint2 getHoveredCell() {
        return hoveredCell == null ? null : new GridPoint2(hoveredCell);
    }

    public GridPoint2 getSelectedCell() {
        return selectedCell == null ? null : new GridPoint2(selectedCell);
    }

    public BlockData getSelectedBlock() {
        if (selectedCell == null) {
            return null;
        }
        return runtimeLevel.getBlock(selectedCell.x, selectedCell.y);
    }

    public boolean selectBlock(int gridX, int gridY) {
        GridPoint2 nextSelection = runtimeLevel.hasBlock(gridX, gridY) ? new GridPoint2(gridX, gridY) : null;
        if (sameCell(selectedCell, nextSelection)) {
            return false;
        }

        selectedCell = nextSelection;
        if (selectedCell == null) {
            updateStatus("Selection cleared");
        } else {
            BlockData blockData = runtimeLevel.getBlock(selectedCell.x, selectedCell.y);
            updateStatus("Selected " + blockData.type + " at (" + selectedCell.x + ", " + selectedCell.y + ")");
        }
        return true;
    }

    public void restoreSelection(GridPoint2 selection) {
        selectedCell = selection == null ? null : new GridPoint2(selection);
    }

    public boolean placeBlock(int gridX, int gridY, BlockType blockType) {
        boolean changed = runtimeLevel.setBlock(gridX, gridY, blockType);
        if (changed) {
            eventBus.post(new BlockPlacedEvent(gridX, gridY, blockType));
        }
        return changed;
    }

    public void restoreBlock(BlockData blockData) {
        runtimeLevel.restoreBlock(blockData);
        eventBus.post(new BlockPlacedEvent(blockData.x, blockData.y, blockData.type));
    }

    public boolean eraseBlock(int gridX, int gridY) {
        BlockData removedBlock = runtimeLevel.removeBlock(gridX, gridY);
        if (removedBlock == null) {
            return false;
        }

        if (selectedCell != null && selectedCell.x == gridX && selectedCell.y == gridY) {
            selectedCell = null;
        }
        eventBus.post(new BlockErasedEvent(gridX, gridY, removedBlock.type));
        return true;
    }

    public boolean saveLevelToDefaultLocation() {
        FileHandle localFile = Gdx.files.local(DEFAULT_LEVEL_PATH);
        levelSerializer.save(snapshotLevelData(), localFile);
        eventBus.post(new LevelSavedEvent(localFile.path(), runtimeLevel.getBlockCount()));
        return true;
    }

    public LevelData readLevelDataFromDefaultLocation() {
        FileHandle localFile = Gdx.files.local(DEFAULT_LEVEL_PATH);
        FileHandle internalFile = Gdx.files.internal(DEFAULT_LEVEL_PATH);
        LevelData levelData = levelSerializer.load(localFile, internalFile);

        if (levelData != null) {
            lastLoadEmptyFallback = false;
            lastLoadSourceDescription = localFile.exists() ? "local:" + localFile.path() : "internal:" + internalFile.path();
            return levelData;
        }

        lastLoadEmptyFallback = true;
        lastLoadSourceDescription = "empty level";
        return createEmptyLevelData();
    }

    public void applyLoadedLevel(LevelData levelData, String sourceDescription, boolean emptyFallback) {
        runtimeLevel.apply(levelData);
        selectedCell = null;
        eventBus.post(new LevelLoadedEvent(sourceDescription, runtimeLevel.getBlockCount(), emptyFallback));
    }

    public LevelData snapshotLevelData() {
        return runtimeLevel.toLevelData();
    }

    public RuntimeLevel getRuntimeLevel() {
        return runtimeLevel;
    }

    public EditorEventBus getEventBus() {
        return eventBus;
    }

    public String getLastStatusMessage() {
        return lastStatusMessage;
    }

    public String getLastLoadSourceDescription() {
        return lastLoadSourceDescription;
    }

    public boolean wasLastLoadEmptyFallback() {
        return lastLoadEmptyFallback;
    }

    public static LevelData createEmptyLevelData() {
        return new LevelData("test-level", "Test Level", DEFAULT_LEVEL_WIDTH, DEFAULT_LEVEL_HEIGHT, DEFAULT_TILE_SIZE);
    }

    private void registerDebugListeners() {
        eventBus.subscribe(BlockPlacedEvent.class,
            event -> updateStatus("Placed " + event.blockType + " at (" + event.x + ", " + event.y + ")"));
        eventBus.subscribe(BlockErasedEvent.class,
            event -> updateStatus("Erased " + event.blockType + " at (" + event.x + ", " + event.y + ")"));
        eventBus.subscribe(ToolChangedEvent.class, event -> updateStatus("Tool: " + event.toolName));
        eventBus.subscribe(LevelSavedEvent.class,
            event -> updateStatus("Saved " + event.blockCount + " blocks to " + event.path));
        eventBus.subscribe(LevelLoadedEvent.class, event -> {
            String source = event.emptyFallback ? "empty level" : event.source;
            updateStatus("Loaded " + event.blockCount + " blocks from " + source);
        });
        eventBus.subscribe(EditorModeChangedEvent.class, event -> updateStatus("Mode: " + event.currentMode));
    }

    private void updateStatus(String statusMessage) {
        this.lastStatusMessage = statusMessage;
        if (Gdx.app != null) {
            Gdx.app.log("LevelEditor", statusMessage);
        }
    }

    private boolean sameCell(GridPoint2 first, GridPoint2 second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        return first.x == second.x && first.y == second.y;
    }
}
