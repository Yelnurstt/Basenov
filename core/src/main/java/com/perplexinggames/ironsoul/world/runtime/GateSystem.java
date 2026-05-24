package com.perplexinggames.ironsoul.world.runtime;

import com.badlogic.gdx.math.Rectangle;
import com.perplexinggames.ironsoul.world.GateData;
import com.perplexinggames.ironsoul.world.GateState;
import com.perplexinggames.ironsoul.world.WorldBlockData;

public class GateSystem {
    public GateData getOverlappingGate(Rectangle tankBounds, WorldBlockData block) {
        if (tankBounds == null || block == null) {
            return null;
        }

        for (GateData gate : block.gates) {
            if (gate != null && gate.bounds != null && gate.bounds.overlaps(tankBounds)) {
                return gate;
            }
        }
        return null;
    }

    public boolean canTransition(GateData gate) {
        return gate != null && gate.state == GateState.OPEN && gate.targetBlockId != null;
    }

    public String buildPrompt(GateData gate) {
        if (gate == null || gate.state == GateState.DISABLED) {
            return null;
        }
        if (gate.state == GateState.LOCKED) {
            return gate.interactionText == null || gate.interactionText.isBlank() ? "Locked" : gate.interactionText;
        }
        return gate.interactionText;
    }
}
