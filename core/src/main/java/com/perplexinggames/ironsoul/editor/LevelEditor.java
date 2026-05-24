package com.perplexinggames.ironsoul.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
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
import com.perplexinggames.ironsoul.terrain.TerrainPath;
import com.perplexinggames.ironsoul.terrain.TerrainPoint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LevelEditor {
    public static final String DEFAULT_LEVEL_PATH = "levels/test-level.json";
    public static final String DEFAULT_TERRAIN_PATH_ID = "main-terrain";
    private static final int DEFAULT_LEVEL_WIDTH = 40;
    private static final int DEFAULT_LEVEL_HEIGHT = 30;
    private static final int DEFAULT_TILE_SIZE = 32;
    private static final float TERRAIN_POINT_PICK_RADIUS_CELLS = 0.9f;

    private final RuntimeLevel runtimeLevel;
    private final LevelSerializer levelSerializer;
    private final EditorEventBus eventBus;
    private final EditorCommandHistory commandHistory;
    private final EditorToolContext toolContext;

    private EditorMode mode;
    private GridPoint2 hoveredCell;
    private GridPoint2 selectedCell;
    private String selectedTerrainPointId;
    private String lastStatusMessage;
    private String lastLoadSourceDescription;
    private boolean lastLoadEmptyFallback;
    private boolean terrainSnapToGrid;
    private int terrainPointSequence;

    public LevelEditor(RuntimeLevel runtimeLevel, LevelSerializer levelSerializer) {
        this.runtimeLevel = runtimeLevel;
        this.levelSerializer = levelSerializer;
        this.eventBus = new EditorEventBus();
        this.commandHistory = new EditorCommandHistory();
        this.toolContext = new EditorToolContext(new PlaceBlockTool());
        this.mode = EditorMode.GAMEPLAY;
        this.lastStatusMessage = "Ready";
        this.terrainSnapToGrid = false;
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

    public TerrainPoint getSelectedTerrainPoint() {
        return findTerrainPointById(selectedTerrainPointId);
    }

    public boolean isTerrainSnapToGrid() {
        return terrainSnapToGrid;
    }

    public void toggleTerrainSnapToGrid() {
        terrainSnapToGrid = !terrainSnapToGrid;
        updateStatus("Terrain snap to grid: " + (terrainSnapToGrid ? "ON" : "OFF"));
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

    public void selectTerrainPoint(String pointId) {
        selectedTerrainPointId = pointId;
        if (pointId == null) {
            updateStatus("Terrain point selection cleared");
            return;
        }

        TerrainPoint point = findTerrainPointById(pointId);
        if (point != null) {
            updateStatus("Selected terrain point " + point.getId());
        }
    }

    public TerrainPoint findTerrainPointNear(float worldX, float worldY) {
        Vector2 target = new Vector2(worldX, worldY);
        float maxDistance = runtimeLevel.getTileSize() * TERRAIN_POINT_PICK_RADIUS_CELLS;
        TerrainPoint closestPoint = null;
        float closestDistance = Float.MAX_VALUE;

        for (TerrainPath terrainPath : runtimeLevel.getTerrainPaths()) {
            for (TerrainPoint point : terrainPath.getPoints()) {
                float distance = target.dst(point.getX(), point.getY());
                if (distance <= maxDistance && distance < closestDistance) {
                    closestDistance = distance;
                    closestPoint = point.copy();
                }
            }
        }
        return closestPoint;
    }

    public String createTerrainPointId() {
        terrainPointSequence++;
        return "terrain-point-" + terrainPointSequence;
    }

    public TerrainPath getPrimaryTerrainPath() {
        return runtimeLevel.getTerrainPath(DEFAULT_TERRAIN_PATH_ID);
    }

    public TerrainPath buildPrimaryTerrainPathWithAddedPoint(String pointId, float worldX, float worldY) {
        TerrainPath currentPath = getPrimaryTerrainPath();
        List<TerrainPoint> points = currentPath == null ? new ArrayList<>() : copyPoints(currentPath.getPoints());
        Vector2 position = resolveTerrainPointPosition(worldX, worldY);
        points.add(new TerrainPoint(pointId, position.x, position.y));
        sortPoints(points);
        return buildTerrainPath(points, currentPath);
    }

    public TerrainPath buildPrimaryTerrainPathWithMovedPoint(String pointId, float worldX, float worldY) {
        TerrainPath currentPath = getPrimaryTerrainPath();
        if (currentPath == null) {
            return null;
        }

        List<TerrainPoint> points = new ArrayList<>();
        Vector2 position = resolveTerrainPointPosition(worldX, worldY);
        boolean changed = false;
        for (TerrainPoint point : currentPath.getPoints()) {
            if (point.getId().equals(pointId)) {
                points.add(new TerrainPoint(pointId, position.x, position.y));
                changed = true;
            } else {
                points.add(point.copy());
            }
        }
        if (!changed) {
            return null;
        }
        sortPoints(points);
        return buildTerrainPath(points, currentPath);
    }

    public TerrainPath buildPrimaryTerrainPathWithRemovedPoint(String pointId) {
        TerrainPath currentPath = getPrimaryTerrainPath();
        if (currentPath == null) {
            return null;
        }

        List<TerrainPoint> points = new ArrayList<>();
        boolean removed = false;
        for (TerrainPoint point : currentPath.getPoints()) {
            if (point.getId().equals(pointId)) {
                removed = true;
                continue;
            }
            points.add(point.copy());
        }
        if (!removed) {
            return null;
        }
        if (points.isEmpty()) {
            return new TerrainPath(DEFAULT_TERRAIN_PATH_ID, points, currentPath.getCurveType(),
                currentPath.getMaterial(), currentPath.getDebugWidth(), currentPath.getFriction());
        }
        sortPoints(points);
        return buildTerrainPath(points, currentPath);
    }

    public boolean replacePrimaryTerrainPath(TerrainPath terrainPath) {
        if (terrainPath == null || terrainPath.getPoints().isEmpty()) {
            boolean removed = runtimeLevel.removeTerrainPath(DEFAULT_TERRAIN_PATH_ID);
            if (removed) {
                updateStatus("Removed terrain path");
            }
            return removed;
        }

        boolean changed = runtimeLevel.setTerrainPath(terrainPath);
        if (changed) {
            updateStatus("Terrain points: " + runtimeLevel.getTerrainPointCount());
        }
        return changed;
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
        selectedTerrainPointId = null;
        terrainPointSequence = runtimeLevel.getTerrainPointCount();
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

    private TerrainPoint findTerrainPointById(String pointId) {
        if (pointId == null) {
            return null;
        }
        for (TerrainPath terrainPath : runtimeLevel.getTerrainPaths()) {
            for (TerrainPoint point : terrainPath.getPoints()) {
                if (pointId.equals(point.getId())) {
                    return point.copy();
                }
            }
        }
        return null;
    }

    private Vector2 resolveTerrainPointPosition(float worldX, float worldY) {
        if (!terrainSnapToGrid) {
            return new Vector2(worldX, worldY);
        }

        float tileSize = runtimeLevel.getTileSize();
        float snappedGridX = Math.round((worldX / tileSize) - 0.5f);
        float snappedGridY = Math.round((worldY / tileSize) - 0.5f);
        return new Vector2((snappedGridX + 0.5f) * tileSize, (snappedGridY + 0.5f) * tileSize);
    }

    private TerrainPath buildTerrainPath(List<TerrainPoint> points, TerrainPath sourcePath) {
        TerrainPath template = sourcePath == null
            ? new TerrainPath(DEFAULT_TERRAIN_PATH_ID, new ArrayList<>(), TerrainPath.CurveType.LINEAR, "editor-dirt", 4f, 1f)
            : sourcePath;
        return new TerrainPath(
            DEFAULT_TERRAIN_PATH_ID,
            points,
            template.getCurveType(),
            template.getMaterial(),
            template.getDebugWidth(),
            template.getFriction()
        );
    }

    private List<TerrainPoint> copyPoints(List<TerrainPoint> sourcePoints) {
        List<TerrainPoint> copies = new ArrayList<>(sourcePoints.size());
        for (TerrainPoint sourcePoint : sourcePoints) {
            copies.add(sourcePoint.copy());
        }
        return copies;
    }

    private void sortPoints(List<TerrainPoint> points) {
        points.sort(Comparator.comparingDouble(TerrainPoint::getX).thenComparingDouble(TerrainPoint::getY));
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
