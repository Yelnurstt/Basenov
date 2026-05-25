package com.perplexinggames.ironsoul.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
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
import com.perplexinggames.ironsoul.editor.ui.EditorToolController;
import com.perplexinggames.ironsoul.level.BlockData;
import com.perplexinggames.ironsoul.level.BlockType;
import com.perplexinggames.ironsoul.level.RuntimeLevel;
import com.perplexinggames.ironsoul.terrain.TerrainPath;
import com.perplexinggames.ironsoul.terrain.TerrainPoint;
import com.perplexinggames.ironsoul.terrain.spline.BezierHandleMode;
import com.perplexinggames.ironsoul.terrain.spline.BezierHandleService;
import com.perplexinggames.ironsoul.terrain.spline.BezierHandleType;
import com.perplexinggames.ironsoul.terrain.spline.SplineControlPoint;
import com.perplexinggames.ironsoul.terrain.spline.SplineCurveType;
import com.perplexinggames.ironsoul.terrain.spline.SplineHandleHit;
import com.perplexinggames.ironsoul.terrain.spline.SplineLayer;
import com.perplexinggames.ironsoul.terrain.spline.SplinePath;
import com.perplexinggames.ironsoul.terrain.spline.SplineTileMode;
import com.perplexinggames.ironsoul.tank.controller.FacingDirection;
import com.perplexinggames.ironsoul.world.GateData;
import com.perplexinggames.ironsoul.world.GateDirection;
import com.perplexinggames.ironsoul.world.GateState;
import com.perplexinggames.ironsoul.world.SpawnPointData;
import com.perplexinggames.ironsoul.world.TransitionType;
import com.perplexinggames.ironsoul.world.WorldBlockData;
import com.perplexinggames.ironsoul.world.WorldBounds;
import com.perplexinggames.ironsoul.world.WorldData;
import com.perplexinggames.ironsoul.world.WorldElementData;
import com.perplexinggames.ironsoul.world.WorldGraph;
import com.perplexinggames.ironsoul.world.WorldValidationIssue;
import com.perplexinggames.ironsoul.world.WorldValidationResult;
import com.perplexinggames.ironsoul.world.WorldValidator;
import com.perplexinggames.ironsoul.world.runtime.WorldSpawnResolver;
import com.perplexinggames.ironsoul.world.serialization.WorldSerializer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class LevelEditor implements EditorToolController {
    public static final String DEFAULT_LEVEL_PATH = "levels/test-level.json";
    public static final String DEFAULT_TERRAIN_PATH_ID = "main-terrain";
    private static final int DEFAULT_BLOCK_WIDTH = 40;
    private static final int DEFAULT_BLOCK_HEIGHT = 30;
    private static final int DEFAULT_TILE_SIZE = 32;
    private static final float TERRAIN_POINT_PICK_RADIUS_CELLS = 0.9f;
    private static final float WORLD_ELEMENT_PICK_RADIUS = 24f;

    public enum MarkerLayer {
        OBJECT,
        ENEMY,
        REWARD,
        TRIGGER
    }

    private final RuntimeLevel runtimeLevel;
    private final WorldSerializer worldSerializer;
    private final EditorEventBus eventBus;
    private final EditorCommandHistory commandHistory;
    private final EditorToolContext toolContext;

    private WorldData worldData;
    private WorldGraph worldGraph;
    private WorldValidationResult lastValidationResult;

    private EditorMode mode;
    private GridPoint2 hoveredCell;
    private GridPoint2 selectedCell;
    private String selectedTerrainPointId;
    private String selectedSplinePathId;
    private String selectedSplinePointId;
    private String selectedSplineLayerId;
    private BezierHandleType selectedSplineHandleType;
    private String selectedGateId;
    private String selectedSpawnPointId;
    private String lastStatusMessage;
    private String lastLoadSourceDescription;
    private boolean lastLoadEmptyFallback;
    private boolean terrainSnapToGrid;
    private int terrainPointSequence;
    private int splinePathSequence;
    private int splinePointSequence;
    private int splineLayerSequence;
    private int blockSequence;
    private int gateSequence;
    private int spawnSequence;
    private int markerSequence;

    public LevelEditor(RuntimeLevel runtimeLevel, WorldSerializer worldSerializer) {
        this.runtimeLevel = runtimeLevel;
        this.worldSerializer = worldSerializer;
        this.eventBus = new EditorEventBus();
        this.commandHistory = new EditorCommandHistory();
        this.toolContext = new EditorToolContext(new PlaceBlockTool());
        this.mode = EditorMode.GAMEPLAY;
        this.lastStatusMessage = "Ready";
        this.terrainSnapToGrid = false;
        this.worldData = createEmptyWorldData();
        this.worldGraph = new WorldGraph(worldData);
        this.lastValidationResult = WorldValidator.validate(worldData);
        runtimeLevel.apply(getActiveBlock(), worldData.tileSize);
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
        syncLoadedBlockFromRuntimeLevel();
        validateWorld();
        updateStatus("Undo: " + commandName);
    }

    public void redo() {
        String commandName = commandHistory.redo();
        if (commandName == null) {
            updateStatus("Nothing to redo");
            return;
        }
        syncLoadedBlockFromRuntimeLevel();
        validateWorld();
        updateStatus("Redo: " + commandName);
    }

    public void clearHistory() {
        commandHistory.clear();
    }

    @Override
    public void setMode(EditorMode mode) {
        if (this.mode == mode) {
            return;
        }
        EditorMode previousMode = this.mode;
        this.mode = mode;
        eventBus.post(new EditorModeChangedEvent(previousMode, mode));
    }

    @Override
    public EditorMode getMode() {
        return mode;
    }

    public boolean isEditorMode() {
        return mode == EditorMode.EDITOR;
    }

    @Override
    public void setTool(EditorToolStrategy toolStrategy) {
        toolContext.setActiveTool(toolStrategy);
        eventBus.post(new ToolChangedEvent(toolStrategy.getName()));
    }

    public EditorToolContext getToolContext() {
        return toolContext;
    }

    @Override
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

    public SplinePath getSelectedSplinePath() {
        return runtimeLevel.getSplinePath(selectedSplinePathId);
    }

    public SplineLayer getSelectedSplineLayer() {
        return runtimeLevel.getSplineLayer(selectedSplineLayerId);
    }

    public SplineControlPoint getSelectedSplinePoint() {
        return findSplinePointById(selectedSplinePointId);
    }

    public String getSelectedSplinePathId() {
        return selectedSplinePathId;
    }

    public String getSelectedSplinePointId() {
        return selectedSplinePointId;
    }

    public String getSelectedSplineLayerId() {
        return selectedSplineLayerId;
    }

    public BezierHandleType getSelectedSplineHandleType() {
        return selectedSplineHandleType;
    }

    public String findSplinePathIdForPoint(String pointId) {
        return findSplinePathIdByPointId(pointId);
    }

    public GateData getSelectedGate() {
        WorldBlockData block = getActiveBlock();
        if (block == null || selectedGateId == null) {
            return null;
        }
        for (GateData gate : block.gates) {
            if (gate != null && selectedGateId.equals(gate.id)) {
                return gate;
            }
        }
        return null;
    }

    public SpawnPointData getSelectedSpawnPoint() {
        WorldBlockData block = getActiveBlock();
        if (block == null || selectedSpawnPointId == null) {
            return null;
        }
        for (SpawnPointData spawnPoint : block.spawnPoints) {
            if (spawnPoint != null && selectedSpawnPointId.equals(spawnPoint.id)) {
                return spawnPoint;
            }
        }
        return null;
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

    public void selectSplinePath(String pathId) {
        selectedSplinePathId = pathId;
        if (pathId == null) {
            selectedSplineHandleType = null;
            updateStatus("Spline selection cleared");
            return;
        }
        SplinePath splinePath = runtimeLevel.getSplinePath(pathId);
        if (splinePath != null) {
            if (selectedSplineLayerId == null || runtimeLevel.getSplineLayer(selectedSplineLayerId) == null
                || !pathId.equals(runtimeLevel.getSplineLayer(selectedSplineLayerId).parentSplinePathId)) {
                List<SplineLayer> pathLayers = getSplineLayersForPath(pathId);
                selectedSplineLayerId = pathLayers.isEmpty() ? null : pathLayers.get(0).id;
            }
            updateStatus("Selected spline " + splinePath.name);
        }
    }

    public void selectSplinePoint(String pointId) {
        selectedSplinePointId = pointId;
        selectedSplineHandleType = null;
        if (pointId == null) {
            updateStatus("Spline point selection cleared");
            return;
        }
        SplineControlPoint point = findSplinePointById(pointId);
        if (point != null) {
            updateStatus("Selected spline point " + point.id);
        }
    }

    public void selectSplineLayer(String layerId) {
        selectedSplineLayerId = layerId;
        selectedSplineHandleType = null;
        if (layerId == null) {
            updateStatus("Spline layer selection cleared");
            return;
        }
        SplineLayer layer = runtimeLevel.getSplineLayer(layerId);
        if (layer != null) {
            selectedSplinePathId = layer.parentSplinePathId;
            updateStatus("Selected spline layer " + layer.name);
        }
    }

    public void selectSplineHandle(String pointId, BezierHandleType handleType) {
        selectedSplinePointId = pointId;
        selectedSplineHandleType = handleType;
        if (pointId == null || handleType == null) {
            updateStatus("Spline handle selection cleared");
            return;
        }
        updateStatus("Selected " + handleType + " handle for " + pointId);
    }

    public void selectGate(String gateId) {
        selectedGateId = gateId;
        if (gateId != null) {
            updateStatus("Selected gate " + gateId);
        }
    }

    public void selectSpawnPoint(String spawnPointId) {
        selectedSpawnPointId = spawnPointId;
        if (spawnPointId != null) {
            updateStatus("Selected spawn point " + spawnPointId);
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

    public SplineControlPoint findSplinePointNear(float worldX, float worldY) {
        Vector2 target = new Vector2(worldX, worldY);
        float maxDistance = runtimeLevel.getTileSize() * TERRAIN_POINT_PICK_RADIUS_CELLS;
        SplineControlPoint closestPoint = null;
        float closestDistance = Float.MAX_VALUE;

        for (SplinePath splinePath : runtimeLevel.getSplinePaths()) {
            for (SplineControlPoint point : splinePath.getPoints()) {
                float distance = target.dst(point.x, point.y);
                if (distance <= maxDistance && distance < closestDistance) {
                    closestDistance = distance;
                    closestPoint = point.copy();
                }
            }
        }
        return closestPoint;
    }

    public SplineHandleHit findSplineHandleNear(float worldX, float worldY) {
        SplinePath selectedPath = getSelectedSplinePath();
        if (selectedPath == null || selectedPath.curveType != SplineCurveType.BEZIER) {
            return null;
        }
        Vector2 target = new Vector2(worldX, worldY);
        float maxDistance = runtimeLevel.getTileSize() * TERRAIN_POINT_PICK_RADIUS_CELLS;
        SplineHandleHit closestHandle = null;
        float closestDistance = Float.MAX_VALUE;

        for (SplineControlPoint point : selectedPath.getPoints()) {
            if (point.getInHandleOffset().len2() > 0.001f) {
                Vector2 inHandle = point.getInHandleWorldPosition();
                float inDistance = target.dst(inHandle);
                if (inDistance <= maxDistance && inDistance < closestDistance) {
                    closestDistance = inDistance;
                    closestHandle = new SplineHandleHit(selectedPath.id, point.id, BezierHandleType.IN, inHandle.x, inHandle.y);
                }
            }

            if (point.getOutHandleOffset().len2() > 0.001f) {
                Vector2 outHandle = point.getOutHandleWorldPosition();
                float outDistance = target.dst(outHandle);
                if (outDistance <= maxDistance && outDistance < closestDistance) {
                    closestDistance = outDistance;
                    closestHandle = new SplineHandleHit(selectedPath.id, point.id, BezierHandleType.OUT, outHandle.x, outHandle.y);
                }
            }
        }
        return closestHandle;
    }

    public String createTerrainPointId() {
        terrainPointSequence++;
        return "terrain-point-" + terrainPointSequence;
    }

    public String createSplinePathId() {
        splinePathSequence++;
        return "spline-path-" + splinePathSequence;
    }

    public String createSplinePointId() {
        splinePointSequence++;
        return "spline-point-" + splinePointSequence;
    }

    public String createSplineLayerId() {
        splineLayerSequence++;
        return "spline-layer-" + splineLayerSequence;
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
        boolean changed;
        if (terrainPath == null || terrainPath.getPoints().isEmpty()) {
            changed = runtimeLevel.removeTerrainPath(DEFAULT_TERRAIN_PATH_ID);
            if (changed) {
                updateStatus("Removed terrain path");
            }
        } else {
            changed = runtimeLevel.setTerrainPath(terrainPath);
            if (changed) {
                updateStatus("Terrain points: " + runtimeLevel.getTerrainPointCount());
            }
        }
        if (changed) {
            syncLoadedBlockFromRuntimeLevel();
            validateWorld();
        }
        return changed;
    }

    public List<SplinePath> getSplinePaths() {
        return runtimeLevel.copySplinePaths();
    }

    public List<SplineLayer> getSplineLayersForPath(String pathId) {
        List<SplineLayer> layers = new ArrayList<>();
        for (SplineLayer layer : runtimeLevel.getSplineLayers()) {
            if (layer != null && pathId != null && pathId.equals(layer.parentSplinePathId)) {
                layers.add(layer.copy());
            }
        }
        layers.sort(Comparator.comparingInt(layer -> layer.renderDepth));
        return layers;
    }

    public SplinePath buildSplinePathWithAddedPoint(String pathId, String pointId, float worldX, float worldY) {
        SplinePath currentPath = runtimeLevel.getSplinePath(pathId);
        if (currentPath == null) {
            return null;
        }
        List<SplineControlPoint> points = copySplinePoints(currentPath.getPoints());
        Vector2 position = resolveTerrainPointPosition(worldX, worldY);
        points.add(new SplineControlPoint(pointId, position.x, position.y));
        SplinePath updated = copySplinePathWithPoints(currentPath, points);
        if (updated.curveType == SplineCurveType.BEZIER) {
            BezierHandleService.ensureValidHandles(updated);
        }
        return updated;
    }

    public SplinePath buildSplinePathWithMovedPoint(String pathId, String pointId, float worldX, float worldY) {
        SplinePath currentPath = runtimeLevel.getSplinePath(pathId);
        if (currentPath == null) {
            return null;
        }
        List<SplineControlPoint> points = new ArrayList<>();
        Vector2 position = resolveTerrainPointPosition(worldX, worldY);
        boolean changed = false;
        for (SplineControlPoint point : currentPath.getPoints()) {
            if (point.id.equals(pointId)) {
                SplineControlPoint movedPoint = point.copy();
                movedPoint.x = position.x;
                movedPoint.y = position.y;
                points.add(movedPoint);
                changed = true;
            } else {
                points.add(point.copy());
            }
        }
        if (!changed) {
            return null;
        }
        SplinePath updated = copySplinePathWithPoints(currentPath, points);
        if (updated.curveType == SplineCurveType.BEZIER) {
            BezierHandleService.ensureValidHandles(updated);
        }
        return updated;
    }

    public SplinePath buildSplinePathWithRemovedPoint(String pathId, String pointId) {
        SplinePath currentPath = runtimeLevel.getSplinePath(pathId);
        if (currentPath == null) {
            return null;
        }
        List<SplineControlPoint> points = new ArrayList<>();
        boolean removed = false;
        for (SplineControlPoint point : currentPath.getPoints()) {
            if (point.id.equals(pointId)) {
                removed = true;
                continue;
            }
            points.add(point.copy());
        }
        if (!removed) {
            return null;
        }
        SplinePath updated = copySplinePathWithPoints(currentPath, points);
        if (updated.curveType == SplineCurveType.BEZIER) {
            BezierHandleService.ensureValidHandles(updated);
        }
        return updated;
    }

    public SplinePath buildSplinePathWithMovedHandle(String pathId, String pointId, BezierHandleType handleType, float worldX, float worldY) {
        SplinePath currentPath = runtimeLevel.getSplinePath(pathId);
        if (currentPath == null || handleType == null) {
            return null;
        }
        List<SplineControlPoint> points = copySplinePoints(currentPath.getPoints());
        boolean changed = false;
        for (SplineControlPoint point : points) {
            if (!point.id.equals(pointId)) {
                continue;
            }
            float offsetX = worldX - point.x;
            float offsetY = worldY - point.y;
            if (handleType == BezierHandleType.IN) {
                BezierHandleService.setInHandle(point, offsetX, offsetY);
            } else {
                BezierHandleService.setOutHandle(point, offsetX, offsetY);
            }
            changed = true;
            break;
        }
        return changed ? copySplinePathWithPoints(currentPath, points) : null;
    }

    public boolean replaceSplinePath(SplinePath splinePath) {
        if (splinePath == null) {
            return false;
        }
        boolean changed = runtimeLevel.setSplinePath(splinePath);
        if (changed) {
            syncLoadedBlockFromRuntimeLevel();
            validateWorld();
        }
        return changed;
    }

    public boolean deleteSplinePath(String pathId) {
        boolean changed = runtimeLevel.removeSplinePath(pathId);
        if (changed) {
            if (pathId != null && pathId.equals(selectedSplinePathId)) {
                selectedSplinePathId = null;
                selectedSplinePointId = null;
                selectedSplineLayerId = null;
                selectedSplineHandleType = null;
            }
            syncLoadedBlockFromRuntimeLevel();
            validateWorld();
        }
        return changed;
    }

    public boolean replaceSplineLayer(SplineLayer splineLayer) {
        boolean changed = runtimeLevel.setSplineLayer(splineLayer);
        if (changed) {
            syncLoadedBlockFromRuntimeLevel();
            validateWorld();
        }
        return changed;
    }

    public boolean deleteSplineLayer(String layerId) {
        boolean changed = runtimeLevel.removeSplineLayer(layerId);
        if (changed) {
            if (layerId != null && layerId.equals(selectedSplineLayerId)) {
                selectedSplineLayerId = null;
            }
            syncLoadedBlockFromRuntimeLevel();
            validateWorld();
        }
        return changed;
    }

    public SplinePath createDefaultSplinePath(String pathId, String name) {
        return new SplinePath(pathId, name, new ArrayList<>(), SplineCurveType.LINEAR, false, true, 4f,
            "editor-dirt", "editor-dirt");
    }

    public SplineLayer createDefaultSplineLayer(String layerId, String pathId, String name, int renderDepth) {
        return new SplineLayer(layerId, pathId, name, null, null, renderDepth, 1f, 0f, runtimeLevel.getTileSize(),
            SplineTileMode.STRETCH, com.badlogic.gdx.graphics.Color.WHITE, true, renderDepth == 0);
    }

    public SplinePath updateSplinePathProperties(String pathId, String name, SplineCurveType curveType, boolean closed,
                                                 boolean collisionEnabled, float collisionThickness, String material) {
        SplinePath existing = runtimeLevel.getSplinePath(pathId);
        if (existing == null) {
            return null;
        }
        SplinePath updated = new SplinePath(existing.id, name, copySplinePoints(existing.getPoints()), curveType, closed, collisionEnabled,
            collisionThickness, material, existing.physicsMaterial);
        if (curveType == SplineCurveType.BEZIER) {
            if (existing.curveType != SplineCurveType.BEZIER) {
                BezierHandleService.autoGenerateHandles(updated);
            } else {
                BezierHandleService.ensureValidHandles(updated);
            }
        }
        return updated;
    }

    public SplinePath updateSplinePointHandleMode(String pathId, String pointId, BezierHandleMode handleMode) {
        SplinePath existing = runtimeLevel.getSplinePath(pathId);
        if (existing == null || pointId == null || handleMode == null) {
            return null;
        }
        List<SplineControlPoint> points = copySplinePoints(existing.getPoints());
        boolean changed = false;
        for (int i = 0; i < points.size(); i++) {
            SplineControlPoint point = points.get(i);
            if (!pointId.equals(point.id)) {
                continue;
            }
            point.handleMode = handleMode;
            if (handleMode == BezierHandleMode.AUTO) {
                SplinePath working = copySplinePathWithPoints(existing, points);
                BezierHandleService.applyAutoHandles(working, i);
                points = copySplinePoints(working.getPoints());
            } else if (handleMode == BezierHandleMode.MIRRORED) {
                BezierHandleService.applyHandleModeAfterOutChanged(point);
            } else if (handleMode == BezierHandleMode.ALIGNED) {
                BezierHandleService.applyHandleModeAfterOutChanged(point);
            }
            changed = true;
            break;
        }
        return changed ? copySplinePathWithPoints(existing, points) : null;
    }

    public SplinePath buildSplinePathWithResetHandles(String pathId, String pointId) {
        SplinePath existing = runtimeLevel.getSplinePath(pathId);
        if (existing == null || pointId == null) {
            return null;
        }
        SplinePath updated = existing.copy();
        BezierHandleService.resetHandles(updated, pointId);
        return updated;
    }

    public SplinePath buildSplinePathWithAutoSmooth(String pathId) {
        SplinePath existing = runtimeLevel.getSplinePath(pathId);
        if (existing == null) {
            return null;
        }
        SplinePath updated = existing.copy();
        updated.curveType = SplineCurveType.BEZIER;
        BezierHandleService.autoGenerateHandles(updated);
        return updated;
    }

    public SplinePath buildSplinePathConvertedToBezier(String pathId) {
        SplinePath existing = runtimeLevel.getSplinePath(pathId);
        if (existing == null) {
            return null;
        }
        SplinePath updated = existing.copy();
        updated.curveType = SplineCurveType.BEZIER;
        BezierHandleService.autoGenerateHandles(updated);
        return updated;
    }

    public BezierHandleMode getSelectedSplinePointHandleMode() {
        SplineControlPoint point = getSelectedSplinePoint();
        return point == null || point.handleMode == null ? BezierHandleMode.AUTO : point.handleMode;
    }

    public SplineLayer updateSplineLayerProperties(String layerId, String spritePath, int renderDepth, float parallaxFactor,
                                                   float verticalOffset, float visualWidth, SplineTileMode tileMode,
                                                   boolean visible, boolean collisionEnabled) {
        SplineLayer existing = runtimeLevel.getSplineLayer(layerId);
        if (existing == null) {
            return null;
        }
        SplineLayer updated = existing.copy();
        updated.spritePath = spritePath;
        updated.renderDepth = renderDepth;
        updated.parallaxFactor = parallaxFactor;
        updated.verticalOffset = verticalOffset;
        updated.visualWidth = visualWidth;
        updated.tileMode = tileMode;
        updated.visible = visible;
        updated.collisionEnabled = collisionEnabled;
        return updated;
    }

    public boolean placeBlock(int gridX, int gridY, BlockType blockType) {
        boolean changed = runtimeLevel.setBlock(gridX, gridY, blockType);
        if (changed) {
            syncLoadedBlockFromRuntimeLevel();
            validateWorld();
            eventBus.post(new BlockPlacedEvent(gridX, gridY, blockType));
        }
        return changed;
    }

    public void restoreBlock(BlockData blockData) {
        runtimeLevel.restoreBlock(blockData);
        syncLoadedBlockFromRuntimeLevel();
        validateWorld();
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
        syncLoadedBlockFromRuntimeLevel();
        validateWorld();
        eventBus.post(new BlockErasedEvent(gridX, gridY, removedBlock.type));
        return true;
    }

    public boolean deleteNonBlockAt(float worldX, float worldY) {
        WorldBlockData block = getActiveBlock();
        if (block == null) {
            return false;
        }

        GateData gate = findGateAt(worldX, worldY);
        if (gate != null) {
            block.gates.remove(gate);
            if (gate.id != null && gate.id.equals(selectedGateId)) {
                selectedGateId = null;
            }
            validateWorld();
            updateStatus("Deleted gate " + gate.id);
            return true;
        }

        SpawnPointData spawnPoint = findSpawnPointNear(worldX, worldY);
        if (spawnPoint != null) {
            block.spawnPoints.remove(spawnPoint);
            if (spawnPoint.id != null && spawnPoint.id.equals(selectedSpawnPointId)) {
                selectedSpawnPointId = null;
            }
            validateWorld();
            updateStatus("Deleted spawn point " + spawnPoint.id);
            return true;
        }

        WorldElementData marker = findMarkerNear(worldX, worldY);
        if (marker != null) {
            removeMarker(block, marker);
            validateWorld();
            updateStatus("Deleted " + marker.type + " " + marker.id);
            return true;
        }

        SplineControlPoint splinePoint = findSplinePointNear(worldX, worldY);
        String splinePathId = splinePoint == null ? null : findSplinePathIdByPointId(splinePoint.id);
        if (splinePoint != null && splinePathId != null) {
            boolean removed = replaceSplinePath(buildSplinePathWithRemovedPoint(splinePathId, splinePoint.id));
            if (removed && splinePoint.id.equals(selectedSplinePointId)) {
                selectedSplinePointId = null;
                selectedSplineHandleType = null;
            }
            return removed;
        }

        TerrainPoint terrainPoint = findTerrainPointNear(worldX, worldY);
        if (terrainPoint != null) {
            boolean removed = replacePrimaryTerrainPath(buildPrimaryTerrainPathWithRemovedPoint(terrainPoint.getId()));
            if (removed && terrainPoint.getId().equals(selectedTerrainPointId)) {
                selectedTerrainPointId = null;
            }
            return removed;
        }

        return false;
    }

    public boolean saveLevelToDefaultLocation() {
        FileHandle localFile = Gdx.files.local(DEFAULT_LEVEL_PATH);
        worldSerializer.save(snapshotWorldData(), localFile);
        eventBus.post(new LevelSavedEvent(localFile.path(), runtimeLevel.getBlockCount()));
        return true;
    }

    public WorldData readWorldDataFromDefaultLocation() {
        FileHandle localFile = Gdx.files.local(DEFAULT_LEVEL_PATH);
        FileHandle internalFile = Gdx.files.internal(DEFAULT_LEVEL_PATH);
        WorldData loadedWorld = worldSerializer.load(localFile, internalFile);
        if (loadedWorld != null) {
            lastLoadEmptyFallback = false;
            lastLoadSourceDescription = localFile.exists() ? "local:" + localFile.path() : "internal:" + internalFile.path();
            return loadedWorld;
        }

        lastLoadEmptyFallback = true;
        lastLoadSourceDescription = "empty world";
        return createEmptyWorldData();
    }

    public void applyLoadedWorld(WorldData loadedWorld, String sourceDescription, boolean emptyFallback) {
        this.worldData = loadedWorld == null ? createEmptyWorldData() : loadedWorld.copy();
        if (worldData.activeBlockId == null && !worldData.worldBlocks.isEmpty()) {
            worldData.activeBlockId = worldData.worldBlocks.get(0).id;
        }
        runtimeLevel.apply(getActiveBlock(), worldData.tileSize);
        selectedCell = null;
        selectedTerrainPointId = null;
        selectedSplinePathId = null;
        selectedSplinePointId = null;
        selectedSplineLayerId = null;
        selectedSplineHandleType = null;
        selectedGateId = null;
        selectedSpawnPointId = null;
        terrainPointSequence = runtimeLevel.getTerrainPointCount();
        splinePointSequence = runtimeLevel.getSplinePointCount();
        rebuildSequences();
        validateWorld();
        eventBus.post(new LevelLoadedEvent(sourceDescription, runtimeLevel.getBlockCount(), emptyFallback));
    }

    public WorldData snapshotWorldData() {
        syncLoadedBlockFromRuntimeLevel();
        validateWorld();
        return worldData.copy();
    }

    public RuntimeLevel getRuntimeLevel() {
        return runtimeLevel;
    }

    public WorldData getWorldData() {
        syncLoadedBlockFromRuntimeLevel();
        return worldData;
    }

    public WorldGraph getWorldGraph() {
        return worldGraph;
    }

    public WorldValidationResult getLastValidationResult() {
        return lastValidationResult;
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

    public WorldBlockData getActiveBlock() {
        return worldData.findBlock(worldData.activeBlockId);
    }

    public String getActiveBlockId() {
        return worldData.activeBlockId;
    }

    public List<String> getBlockIds() {
        List<String> ids = new ArrayList<>();
        for (WorldBlockData block : worldData.worldBlocks) {
            ids.add(block.id);
        }
        return ids;
    }

    public boolean selectActiveBlock(String blockId) {
        if (blockId == null || worldData.findBlock(blockId) == null) {
            return false;
        }
        if (blockId.equals(worldData.activeBlockId) && blockId.equals(runtimeLevel.getId())) {
            return false;
        }
        syncLoadedBlockFromRuntimeLevel();
        worldData.activeBlockId = blockId;
        runtimeLevel.apply(getActiveBlock(), worldData.tileSize);
        selectedCell = null;
        selectedTerrainPointId = null;
        selectedSplinePathId = null;
        selectedSplinePointId = null;
        selectedSplineLayerId = null;
        selectedSplineHandleType = null;
        selectedGateId = null;
        selectedSpawnPointId = null;
        updateStatus("Active block: " + blockId);
        validateWorld();
        return true;
    }

    public WorldBlockData createBlock(String requestedId, String requestedName, int width, int height) {
        String blockId = sanitizeBlockId(requestedId, requestedName);
        if (blockId == null) {
            blockId = "block-" + (++blockSequence);
        }
        if (worldData.findBlock(blockId) != null) {
            blockId = blockId + "-" + (++blockSequence);
        }
        String blockName = requestedName == null || requestedName.isBlank() ? blockId : requestedName.trim();
        WorldBlockData block = new WorldBlockData(blockId, blockName, Math.max(1, width), Math.max(1, height));
        float tileSize = worldData.tileSize;
        block.spawnPoints.add(WorldSpawnResolver.createDefaultSpawnPoint(
            "START_" + blockId.toUpperCase(Locale.ROOT),
            blockId,
            block,
            worldData.tileSize,
            blockName + " start"
        ));
        syncLoadedBlockFromRuntimeLevel();
        worldData.worldBlocks.add(block);
        worldData.activeBlockId = blockId;
        runtimeLevel.apply(block, worldData.tileSize);
        validateWorld();
        updateStatus("Created block " + blockId);
        return block;
    }

    public boolean updateActiveBlockProperties(String requestedId, String requestedName, int width, int height) {
        WorldBlockData activeBlock = getActiveBlock();
        if (activeBlock == null) {
            return false;
        }
        syncLoadedBlockFromRuntimeLevel();

        String originalId = activeBlock.id;
        String nextId = sanitizeBlockId(requestedId, requestedName);
        if (nextId == null) {
            nextId = activeBlock.id;
        }
        if (!nextId.equals(originalId) && worldData.findBlock(nextId) != null) {
            updateStatus("Block id already exists: " + nextId);
            return false;
        }

        activeBlock.name = requestedName == null || requestedName.isBlank() ? activeBlock.name : requestedName.trim();
        if (!nextId.equals(originalId)) {
            renameBlockReferences(originalId, nextId);
            activeBlock.id = nextId;
            worldData.activeBlockId = nextId;
        }

        RuntimeLevel.ResizeResult resizeResult = runtimeLevel.resize(Math.max(1, width), Math.max(1, height));
        syncLoadedBlockFromRuntimeLevel();
        clampActiveBlockNonTerrainData(activeBlock);
        validateWorld();

        if (resizeResult.hasWarnings()) {
            updateStatus(String.join(" ", resizeResult.getWarnings()));
        } else {
            updateStatus("Updated block " + activeBlock.id);
        }
        return true;
    }

    public boolean placeOrSelectGate(float worldX, float worldY) {
        GateData gate = findGateAt(worldX, worldY);
        if (gate != null) {
            selectGate(gate.id);
            return true;
        }

        WorldBlockData block = getActiveBlock();
        if (block == null) {
            return false;
        }

        String targetBlockId = findNextTargetBlockId(block.id);
        String targetSpawnId = findFirstSpawnPointId(targetBlockId);
        float tileSize = worldData.tileSize;
        float gateWidth = tileSize * 2f;
        float gateHeight = tileSize * 2f;
        float clampedX = MathUtils.clamp(worldX - gateWidth * 0.5f, 0f, block.getPixelWidth(worldData.tileSize) - gateWidth);
        float clampedY = MathUtils.clamp(worldY - gateHeight * 0.5f, 0f, block.getPixelHeight(worldData.tileSize) - gateHeight);

        GateData createdGate = new GateData();
        createdGate.id = "gate-" + (++gateSequence);
        createdGate.debugName = createdGate.id;
        createdGate.sourceBlockId = block.id;
        createdGate.targetBlockId = targetBlockId;
        createdGate.targetSpawnPointId = targetSpawnId;
        createdGate.bounds = new WorldBounds(clampedX, clampedY, gateWidth, gateHeight);
        createdGate.direction = GateDirection.ANY;
        createdGate.transitionType = TransitionType.INTERACTION;
        createdGate.state = GateState.OPEN;
        createdGate.interactionText = buildGateInteractionText(targetBlockId);
        block.gates.add(createdGate);
        selectGate(createdGate.id);
        validateWorld();
        updateStatus("Created gate " + createdGate.id);
        return true;
    }

    public boolean placeSpawnPoint(float worldX, float worldY) {
        WorldBlockData block = getActiveBlock();
        if (block == null) {
            return false;
        }

        SpawnPointData existing = findSpawnPointNear(worldX, worldY);
        if (existing != null) {
            existing.x = clampToBlockX(worldX, block);
            existing.y = clampToBlockY(worldY, block);
            selectSpawnPoint(existing.id);
            validateWorld();
            updateStatus("Moved spawn point " + existing.id);
            return true;
        }

        SpawnPointData spawnPoint = new SpawnPointData(
            "spawn-" + (++spawnSequence),
            block.id,
            clampToBlockX(worldX, block),
            clampToBlockY(worldY, block),
            FacingDirection.RIGHT,
            "Spawn " + spawnSequence
        );
        block.spawnPoints.add(spawnPoint);
        selectSpawnPoint(spawnPoint.id);
        validateWorld();
        updateStatus("Created spawn point " + spawnPoint.id);
        return true;
    }

    public boolean placeMarker(MarkerLayer layer, float worldX, float worldY) {
        WorldBlockData block = getActiveBlock();
        if (block == null) {
            return false;
        }
        WorldElementData marker = new WorldElementData(
            layer.name().toLowerCase(Locale.ROOT) + "-" + (++markerSequence),
            layer.name().toLowerCase(Locale.ROOT),
            clampToBlockX(worldX, block),
            clampToBlockY(worldY, block),
            layer.name() + " " + markerSequence
        );
        getMarkerCollection(block, layer).add(marker);
        validateWorld();
        updateStatus("Placed " + marker.type + " " + marker.id);
        return true;
    }

    public void cycleSelectedGateTransitionType() {
        GateData gate = getSelectedGate();
        if (gate == null) {
            return;
        }
        gate.transitionType = gate.transitionType == TransitionType.SEAMLESS
            ? TransitionType.INTERACTION
            : TransitionType.SEAMLESS;
        if (gate.transitionType == TransitionType.INTERACTION && (gate.interactionText == null || gate.interactionText.isBlank())) {
            gate.interactionText = buildGateInteractionText(gate.targetBlockId);
        }
        validateWorld();
        updateStatus("Gate " + gate.id + " type: " + gate.transitionType);
    }

    public void cycleSelectedGateState() {
        GateData gate = getSelectedGate();
        if (gate == null) {
            return;
        }
        gate.state = switch (gate.state) {
            case OPEN -> GateState.LOCKED;
            case LOCKED -> GateState.DISABLED;
            case DISABLED -> GateState.OPEN;
        };
        validateWorld();
        updateStatus("Gate " + gate.id + " state: " + gate.state);
    }

    public void cycleSelectedGateTargetBlock() {
        GateData gate = getSelectedGate();
        if (gate == null || worldData.worldBlocks.isEmpty()) {
            return;
        }
        List<String> blockIds = getBlockIds();
        int currentIndex = Math.max(0, blockIds.indexOf(gate.targetBlockId));
        String nextTarget = gate.targetBlockId;
        for (int offset = 1; offset <= blockIds.size(); offset++) {
            String candidate = blockIds.get((currentIndex + offset) % blockIds.size());
            if (!candidate.equals(gate.sourceBlockId)) {
                nextTarget = candidate;
                break;
            }
        }
        gate.targetBlockId = nextTarget;
        gate.targetSpawnPointId = findFirstSpawnPointId(nextTarget);
        gate.interactionText = buildGateInteractionText(nextTarget);
        validateWorld();
        updateStatus("Gate " + gate.id + " target: " + nextTarget);
    }

    public void cycleSelectedGateTargetSpawnPoint() {
        GateData gate = getSelectedGate();
        if (gate == null || gate.targetBlockId == null) {
            return;
        }
        WorldBlockData targetBlock = worldData.findBlock(gate.targetBlockId);
        if (targetBlock == null || targetBlock.spawnPoints.isEmpty()) {
            return;
        }
        int index = 0;
        for (int i = 0; i < targetBlock.spawnPoints.size(); i++) {
            if (gate.targetSpawnPointId != null && gate.targetSpawnPointId.equals(targetBlock.spawnPoints.get(i).id)) {
                index = i;
                break;
            }
        }
        gate.targetSpawnPointId = targetBlock.spawnPoints.get((index + 1) % targetBlock.spawnPoints.size()).id;
        validateWorld();
        updateStatus("Gate " + gate.id + " spawn: " + gate.targetSpawnPointId);
    }

    public String getValidationSummary() {
        if (lastValidationResult == null || lastValidationResult.getIssues().isEmpty()) {
            return "Validation: OK";
        }
        WorldValidationIssue issue = lastValidationResult.getIssues().get(0);
        return "Validation: " + issue.getSeverity() + " - " + issue.getMessage();
    }

    public static WorldData createEmptyWorldData() {
        WorldData worldData = new WorldData("test-world", "Test World", DEFAULT_TILE_SIZE);
        WorldBlockData startBlock = new WorldBlockData("start-area", "Start Area", DEFAULT_BLOCK_WIDTH, DEFAULT_BLOCK_HEIGHT);
        startBlock.spawnPoints.add(WorldSpawnResolver.createDefaultSpawnPoint("START", "start-area", startBlock, DEFAULT_TILE_SIZE, "Start"));
        worldData.worldBlocks.add(startBlock);
        worldData.activeBlockId = startBlock.id;
        return worldData;
    }

    private void registerDebugListeners() {
        eventBus.subscribe(BlockPlacedEvent.class,
            event -> updateStatus("Placed " + event.blockType + " at (" + event.x + ", " + event.y + ")"));
        eventBus.subscribe(BlockErasedEvent.class,
            event -> updateStatus("Erased " + event.blockType + " at (" + event.x + ", " + event.y + ")"));
        eventBus.subscribe(ToolChangedEvent.class, event -> updateStatus("Tool: " + event.toolName));
        eventBus.subscribe(LevelSavedEvent.class,
            event -> updateStatus("Saved active block and world graph to " + event.path));
        eventBus.subscribe(LevelLoadedEvent.class, event -> {
            String source = event.emptyFallback ? "empty world" : event.source;
            updateStatus("Loaded world from " + source);
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

    private SplineControlPoint findSplinePointById(String pointId) {
        if (pointId == null) {
            return null;
        }
        for (SplinePath splinePath : runtimeLevel.getSplinePaths()) {
            for (SplineControlPoint point : splinePath.getPoints()) {
                if (pointId.equals(point.id)) {
                    return point.copy();
                }
            }
        }
        return null;
    }

    private String findSplinePathIdByPointId(String pointId) {
        if (pointId == null) {
            return null;
        }
        for (SplinePath splinePath : runtimeLevel.getSplinePaths()) {
            for (SplineControlPoint point : splinePath.getPoints()) {
                if (pointId.equals(point.id)) {
                    return splinePath.id;
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

    private SplinePath copySplinePathWithPoints(SplinePath sourcePath, List<SplineControlPoint> points) {
        SplinePath copied = new SplinePath(
            sourcePath.id,
            sourcePath.name,
            points,
            sourcePath.curveType,
            sourcePath.closed,
            sourcePath.collisionEnabled,
            sourcePath.collisionThickness,
            sourcePath.material,
            sourcePath.physicsMaterial
        );
        if (copied.curveType == SplineCurveType.BEZIER) {
            BezierHandleService.ensureValidHandles(copied);
        }
        return copied;
    }

    private List<TerrainPoint> copyPoints(List<TerrainPoint> sourcePoints) {
        List<TerrainPoint> copies = new ArrayList<>(sourcePoints.size());
        for (TerrainPoint sourcePoint : sourcePoints) {
            copies.add(sourcePoint.copy());
        }
        return copies;
    }

    private List<SplineControlPoint> copySplinePoints(List<SplineControlPoint> sourcePoints) {
        List<SplineControlPoint> copies = new ArrayList<>(sourcePoints.size());
        for (SplineControlPoint sourcePoint : sourcePoints) {
            copies.add(sourcePoint.copy());
        }
        return copies;
    }

    private void sortPoints(List<TerrainPoint> points) {
        points.sort(Comparator.comparingDouble(TerrainPoint::getX).thenComparingDouble(TerrainPoint::getY));
    }

    private void updateStatus(String statusMessage) {
        lastStatusMessage = statusMessage;
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

    private void syncLoadedBlockFromRuntimeLevel() {
        String loadedBlockId = runtimeLevel.getId();
        WorldBlockData loadedBlock = loadedBlockId == null ? getActiveBlock() : worldData.findBlock(loadedBlockId);
        if (loadedBlock == null) {
            return;
        }
        loadedBlock.width = runtimeLevel.getWidth();
        loadedBlock.height = runtimeLevel.getHeight();
        loadedBlock.tiles = runtimeLevel.copyBlocks();
        loadedBlock.terrain = runtimeLevel.copyTerrainPaths();
        loadedBlock.splinePaths = runtimeLevel.copySplinePaths();
        loadedBlock.splineLayers = runtimeLevel.copySplineLayers();
    }

    private void validateWorld() {
        worldGraph = new WorldGraph(worldData);
        lastValidationResult = WorldValidator.validate(worldData);
    }

    private void renameBlockReferences(String originalId, String nextId) {
        for (WorldBlockData block : worldData.worldBlocks) {
            for (SpawnPointData spawnPoint : block.spawnPoints) {
                if (originalId.equals(spawnPoint.blockId)) {
                    spawnPoint.blockId = nextId;
                }
            }
            for (GateData gate : block.gates) {
                if (originalId.equals(gate.sourceBlockId)) {
                    gate.sourceBlockId = nextId;
                }
                if (originalId.equals(gate.targetBlockId)) {
                    gate.targetBlockId = nextId;
                }
            }
        }
    }

    private void clampActiveBlockNonTerrainData(WorldBlockData block) {
        float maxWidth = block.getPixelWidth(worldData.tileSize);
        float maxHeight = block.getPixelHeight(worldData.tileSize);

        for (SpawnPointData spawnPoint : block.spawnPoints) {
            spawnPoint.x = MathUtils.clamp(spawnPoint.x, 0f, maxWidth);
            spawnPoint.y = MathUtils.clamp(spawnPoint.y, 0f, maxHeight);
        }
        for (GateData gate : block.gates) {
            if (gate.bounds != null) {
                gate.bounds.clampTo(maxWidth, maxHeight);
            }
            gate.sourceBlockId = block.id;
        }
        clampMarkers(block.objects, maxWidth, maxHeight);
        clampMarkers(block.enemies, maxWidth, maxHeight);
        clampMarkers(block.rewards, maxWidth, maxHeight);
        clampMarkers(block.triggers, maxWidth, maxHeight);
    }

    private void clampMarkers(List<WorldElementData> markers, float maxWidth, float maxHeight) {
        for (WorldElementData marker : markers) {
            marker.x = MathUtils.clamp(marker.x, 0f, maxWidth);
            marker.y = MathUtils.clamp(marker.y, 0f, maxHeight);
        }
    }

    private void rebuildSequences() {
        blockSequence = worldData.worldBlocks.size();
        gateSequence = 0;
        spawnSequence = 0;
        markerSequence = 0;
        splinePathSequence = 0;
        splineLayerSequence = 0;
        for (WorldBlockData block : worldData.worldBlocks) {
            gateSequence += block.gates.size();
            spawnSequence += block.spawnPoints.size();
            markerSequence += block.objects.size() + block.enemies.size() + block.rewards.size() + block.triggers.size();
            splinePathSequence += block.splinePaths.size();
            splineLayerSequence += block.splineLayers.size();
        }
    }

    private String sanitizeBlockId(String requestedId, String fallbackName) {
        String source = requestedId;
        if (source == null || source.isBlank()) {
            source = fallbackName;
        }
        if (source == null || source.isBlank()) {
            return null;
        }
        String normalized = source.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        return normalized.replaceAll("^-+|-+$", "");
    }

    private GateData findGateAt(float worldX, float worldY) {
        WorldBlockData block = getActiveBlock();
        if (block == null) {
            return null;
        }
        for (GateData gate : block.gates) {
            if (gate.bounds != null && gate.bounds.contains(worldX, worldY)) {
                return gate;
            }
        }
        return null;
    }

    private SpawnPointData findSpawnPointNear(float worldX, float worldY) {
        WorldBlockData block = getActiveBlock();
        if (block == null) {
            return null;
        }
        Vector2 target = new Vector2(worldX, worldY);
        for (SpawnPointData spawnPoint : block.spawnPoints) {
            if (target.dst(spawnPoint.x, spawnPoint.y) <= WORLD_ELEMENT_PICK_RADIUS) {
                return spawnPoint;
            }
        }
        return null;
    }

    private WorldElementData findMarkerNear(float worldX, float worldY) {
        WorldBlockData block = getActiveBlock();
        if (block == null) {
            return null;
        }
        Vector2 target = new Vector2(worldX, worldY);
        WorldElementData closest = null;
        float closestDistance = Float.MAX_VALUE;
        closest = findClosestMarker(target, block.objects, closest, closestDistance);
        closestDistance = closest == null ? Float.MAX_VALUE : target.dst(closest.x, closest.y);
        closest = findClosestMarker(target, block.enemies, closest, closestDistance);
        closestDistance = closest == null ? Float.MAX_VALUE : target.dst(closest.x, closest.y);
        closest = findClosestMarker(target, block.rewards, closest, closestDistance);
        closestDistance = closest == null ? Float.MAX_VALUE : target.dst(closest.x, closest.y);
        return findClosestMarker(target, block.triggers, closest, closestDistance);
    }

    private WorldElementData findClosestMarker(Vector2 target, List<WorldElementData> markers,
                                               WorldElementData currentClosest, float currentDistance) {
        WorldElementData closest = currentClosest;
        float closestDistance = currentDistance;
        for (WorldElementData marker : markers) {
            float distance = target.dst(marker.x, marker.y);
            if (distance <= WORLD_ELEMENT_PICK_RADIUS && distance < closestDistance) {
                closest = marker;
                closestDistance = distance;
            }
        }
        return closest;
    }

    private void removeMarker(WorldBlockData block, WorldElementData marker) {
        if (block.objects.remove(marker)) {
            return;
        }
        if (block.enemies.remove(marker)) {
            return;
        }
        if (block.rewards.remove(marker)) {
            return;
        }
        block.triggers.remove(marker);
    }

    private String findNextTargetBlockId(String sourceBlockId) {
        for (WorldBlockData block : worldData.worldBlocks) {
            if (!block.id.equals(sourceBlockId)) {
                return block.id;
            }
        }
        return sourceBlockId;
    }

    private String findFirstSpawnPointId(String blockId) {
        WorldBlockData block = worldData.findBlock(blockId);
        if (block == null || block.spawnPoints.isEmpty()) {
            return null;
        }
        return block.spawnPoints.get(0).id;
    }

    private String buildGateInteractionText(String targetBlockId) {
        WorldBlockData targetBlock = worldData.findBlock(targetBlockId);
        String targetName = targetBlock == null ? "area" : targetBlock.name;
        return "Press E to enter " + targetName;
    }

    private float clampToBlockX(float worldX, WorldBlockData block) {
        return MathUtils.clamp(worldX, 0f, block.getPixelWidth(worldData.tileSize));
    }

    private float clampToBlockY(float worldY, WorldBlockData block) {
        return MathUtils.clamp(worldY, 0f, block.getPixelHeight(worldData.tileSize));
    }

    private List<WorldElementData> getMarkerCollection(WorldBlockData block, MarkerLayer layer) {
        return switch (layer) {
            case OBJECT -> block.objects;
            case ENEMY -> block.enemies;
            case REWARD -> block.rewards;
            case TRIGGER -> block.triggers;
        };
    }
}
