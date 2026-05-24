package com.perplexinggames.ironsoul.world;

import com.perplexinggames.ironsoul.tank.controller.FacingDirection;

public class SpawnPointData {
    public String id;
    public String blockId;
    public float x;
    public float y;
    public FacingDirection facingDirection;
    public String debugLabel;

    public SpawnPointData() {
    }

    public SpawnPointData(String id, String blockId, float x, float y, FacingDirection facingDirection, String debugLabel) {
        this.id = id;
        this.blockId = blockId;
        this.x = x;
        this.y = y;
        this.facingDirection = facingDirection;
        this.debugLabel = debugLabel;
    }

    public SpawnPointData copy() {
        return new SpawnPointData(id, blockId, x, y, facingDirection, debugLabel);
    }
}
