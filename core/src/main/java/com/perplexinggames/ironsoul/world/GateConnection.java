package com.perplexinggames.ironsoul.world;

public class GateConnection {
    public final String sourceBlockId;
    public final String sourceGateId;
    public final String targetBlockId;
    public final String targetSpawnPointId;
    public final TransitionType transitionType;

    public GateConnection(String sourceBlockId, String sourceGateId, String targetBlockId,
                          String targetSpawnPointId, TransitionType transitionType) {
        this.sourceBlockId = sourceBlockId;
        this.sourceGateId = sourceGateId;
        this.targetBlockId = targetBlockId;
        this.targetSpawnPointId = targetSpawnPointId;
        this.transitionType = transitionType;
    }
}
