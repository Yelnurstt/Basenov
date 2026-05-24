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
    private String selectedGateId;
    private String selectedSpawnPointId;
    private String lastStatusMessage;
    private String lastLoadSourceDescription;
    private boolean lastLoadEmptyFallback;
    private boolean terrainSnapToGrid;
    private int terrainPointSequence;
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
        selectedGateId = null;
        selectedSpawnPointId = null;
        terrainPointSequence = runtimeLevel.getTerrainPointCount();
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
        for (WorldBlockData block : worldData.worldBlocks) {
            gateSequence += block.gates.size();
            spawnSequence += block.spawnPoints.size();
            markerSequence += block.objects.size() + block.enemies.size() + block.rewards.size() + block.triggers.size();
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
