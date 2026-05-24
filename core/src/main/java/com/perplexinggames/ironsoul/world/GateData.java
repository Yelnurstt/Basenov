package com.perplexinggames.ironsoul.world;

import java.util.LinkedHashMap;
import java.util.Map;

public class GateData {
    public String id;
    public String sourceBlockId;
    public String targetBlockId;
    public String targetSpawnPointId;
    public WorldBounds bounds;
    public GateDirection direction;
    public GateState state;
    public TransitionType transitionType;
    public String interactionText;
    public String requiredAbility;
    public String debugName;
    public Map<String, String> metadata;

    public GateData() {
        bounds = new WorldBounds();
        direction = GateDirection.ANY;
        state = GateState.OPEN;
        transitionType = TransitionType.SEAMLESS;
        metadata = new LinkedHashMap<>();
    }

    public GateData copy() {
        GateData copy = new GateData();
        copy.id = id;
        copy.sourceBlockId = sourceBlockId;
        copy.targetBlockId = targetBlockId;
        copy.targetSpawnPointId = targetSpawnPointId;
        copy.bounds = bounds == null ? null : bounds.copy();
        copy.direction = direction;
        copy.state = state;
        copy.transitionType = transitionType;
        copy.interactionText = interactionText;
        copy.requiredAbility = requiredAbility;
        copy.debugName = debugName;
        copy.metadata.putAll(metadata);
        return copy;
    }
}
