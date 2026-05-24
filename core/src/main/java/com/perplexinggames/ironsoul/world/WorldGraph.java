package com.perplexinggames.ironsoul.world;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class WorldGraph {
    private final WorldData worldData;

    public WorldGraph(WorldData worldData) {
        this.worldData = worldData;
    }

    public List<String> findConnectedBlocks(String blockId) {
        Set<String> connected = new LinkedHashSet<>();

        for (GateData gate : findOutgoingGates(blockId)) {
            if (gate.targetBlockId != null) {
                connected.add(gate.targetBlockId);
            }
        }
        for (GateData gate : findIncomingGates(blockId)) {
            if (gate.sourceBlockId != null) {
                connected.add(gate.sourceBlockId);
            }
        }

        return new ArrayList<>(connected);
    }

    public List<GateData> findOutgoingGates(String blockId) {
        List<GateData> gates = new ArrayList<>();
        WorldBlockData block = worldData.findBlock(blockId);
        if (block == null) {
            return gates;
        }
        for (GateData gate : block.gates) {
            if (gate != null && blockId.equals(gate.sourceBlockId)) {
                gates.add(gate);
            }
        }
        return gates;
    }

    public List<GateData> findIncomingGates(String blockId) {
        List<GateData> gates = new ArrayList<>();
        for (WorldBlockData block : worldData.worldBlocks) {
            if (block == null) {
                continue;
            }
            for (GateData gate : block.gates) {
                if (gate != null && blockId.equals(gate.targetBlockId)) {
                    gates.add(gate);
                }
            }
        }
        return gates;
    }

    public List<GateConnection> getConnections() {
        List<GateConnection> connections = new ArrayList<>();
        for (WorldBlockData block : worldData.worldBlocks) {
            if (block == null) {
                continue;
            }
            for (GateData gate : block.gates) {
                if (gate == null) {
                    continue;
                }
                connections.add(new GateConnection(
                    gate.sourceBlockId,
                    gate.id,
                    gate.targetBlockId,
                    gate.targetSpawnPointId,
                    gate.transitionType
                ));
            }
        }
        return connections;
    }

    public WorldValidationResult validateConnections() {
        return WorldValidator.validate(worldData);
    }
}
