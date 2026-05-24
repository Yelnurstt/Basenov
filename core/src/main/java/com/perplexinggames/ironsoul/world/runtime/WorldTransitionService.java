package com.perplexinggames.ironsoul.world.runtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.perplexinggames.ironsoul.world.GateData;
import com.perplexinggames.ironsoul.world.GateState;
import com.perplexinggames.ironsoul.world.SpawnPointData;
import com.perplexinggames.ironsoul.world.TransitionType;
import com.perplexinggames.ironsoul.world.WorldBlockData;
import com.perplexinggames.ironsoul.world.WorldData;

import java.util.Locale;
import java.util.function.Consumer;

public class WorldTransitionService {
    private final WorldData worldData;
    private final WorldStreamingService streamingService;
    private final GateSystem gateSystem;
    private final TankRuntimeAdapter tankAdapter;
    private final Consumer<String> blockChangeListener;

    private GateData activeGate;
    private String activeInteractionText;
    private String lastTransitionLog = "Idle";

    public WorldTransitionService(WorldData worldData, WorldStreamingService streamingService,
                                  TankRuntimeAdapter tankAdapter, Consumer<String> blockChangeListener) {
        this.worldData = worldData;
        this.streamingService = streamingService;
        this.gateSystem = new GateSystem();
        this.tankAdapter = tankAdapter;
        this.blockChangeListener = blockChangeListener;
    }

    public GateData checkGateOverlap(Rectangle tankBounds, WorldBlockData currentBlock) {
        GateData previousGate = activeGate;
        activeGate = gateSystem.getOverlappingGate(tankBounds, currentBlock);
        activeInteractionText = gateSystem.buildPrompt(activeGate);
        if (activeGate != null && (previousGate == null || !activeGate.id.equals(previousGate.id))) {
            log("Tank entered gate: " + activeGate.id);
        }
        return activeGate;
    }

    public GateData getAvailableGate(Rectangle tankBounds, WorldBlockData currentBlock) {
        return gateSystem.getOverlappingGate(tankBounds, currentBlock);
    }

    public boolean requestTransition(String gateId) {
        if (activeGate == null || gateId == null || !gateId.equals(activeGate.id)) {
            return false;
        }
        if (activeGate.state != GateState.OPEN || activeGate.transitionType != TransitionType.INTERACTION) {
            return false;
        }
        executeTransition(activeGate);
        return true;
    }

    public void update(WorldBlockData currentBlock, boolean interactPressed) {
        GateData gate = checkGateOverlap(tankAdapter.getBounds(), currentBlock);
        if (gate == null) {
            activeInteractionText = null;
            return;
        }
        if (gate.transitionType == TransitionType.SEAMLESS && gateSystem.canTransition(gate)) {
            executeTransition(gate);
            return;
        }
        if (interactPressed && gate.transitionType == TransitionType.INTERACTION && gate.state == GateState.OPEN) {
            executeTransition(gate);
        }
    }

    public void executeTransition(GateData gate) {
        if (!gateSystem.canTransition(gate)) {
            if (gate != null && gate.state == GateState.LOCKED) {
                activeInteractionText = gateSystem.buildPrompt(gate);
            }
            return;
        }

        WorldBlockData targetBlock = worldData.findBlock(gate.targetBlockId);
        if (targetBlock == null) {
            return;
        }

        streamingService.loadBlock(gate.targetBlockId);
        changeCurrentBlock(gate.targetBlockId);
        SpawnPointData spawnPoint = resolveTargetSpawnPoint(targetBlock, gate.targetSpawnPointId);
        moveTankToSpawnPoint(WorldSpawnResolver.resolveSafeSpawnPoint(targetBlock, spawnPoint, worldData.tileSize));
        streamingService.loadConnectedBlocks(gate.targetBlockId);
        streamingService.unloadFarBlocks();
        activeGate = null;
        activeInteractionText = null;
    }

    public void moveTankToSpawnPoint(SpawnPointData spawnPoint) {
        if (spawnPoint == null) {
            return;
        }
        float x = spawnPoint.x;
        float y = spawnPoint.y;
        tankAdapter.setPosition(x, y);
        tankAdapter.setFacingDirection(spawnPoint.facingDirection);
        lastTransitionLog = String.format(Locale.US, "Spawning tank at: %s position=(%.1f, %.1f)", spawnPoint.id, x, y);
        log(lastTransitionLog);
    }

    public void changeCurrentBlock(String targetBlockId) {
        String previousBlockId = streamingService.getCurrentBlock();
        streamingService.setCurrentBlock(targetBlockId);
        if (blockChangeListener != null) {
            blockChangeListener.accept(targetBlockId);
        }
        lastTransitionLog = "Changing block: " + previousBlockId + " -> " + targetBlockId;
        log(lastTransitionLog);
    }

    public String getActiveInteractionText() {
        return activeInteractionText;
    }

    public String getLastTransitionLog() {
        return lastTransitionLog;
    }

    public GateData getActiveGate() {
        return activeGate;
    }

    private SpawnPointData resolveTargetSpawnPoint(WorldBlockData targetBlock, String spawnPointId) {
        if (spawnPointId != null) {
            for (SpawnPointData spawnPoint : targetBlock.spawnPoints) {
                if (spawnPoint != null && spawnPointId.equals(spawnPoint.id)) {
                    return spawnPoint;
                }
            }
        }
        return targetBlock.spawnPoints.isEmpty() ? null : targetBlock.spawnPoints.get(0);
    }

    private void log(String message) {
        if (Gdx.app != null) {
            Gdx.app.log("WorldTransitionService", message);
        }
    }
}
