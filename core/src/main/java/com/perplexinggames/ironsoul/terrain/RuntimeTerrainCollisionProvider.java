package com.perplexinggames.ironsoul.terrain;

import com.badlogic.gdx.math.Vector2;
import com.perplexinggames.ironsoul.level.RuntimeLevel;
import com.perplexinggames.ironsoul.terrain.spline.SplineTerrainCollisionAdapter;

import java.util.ArrayList;
import java.util.Collection;

public class RuntimeTerrainCollisionProvider implements TerrainCollisionProvider {
    private final RuntimeLevel runtimeLevel;
    private final TerrainCollisionBuilder collisionBuilder;
    private final SplineTerrainCollisionAdapter splineCollisionAdapter;

    public RuntimeTerrainCollisionProvider(RuntimeLevel runtimeLevel) {
        this.runtimeLevel = runtimeLevel;
        this.collisionBuilder = new TerrainCollisionBuilder();
        this.splineCollisionAdapter = new SplineTerrainCollisionAdapter(24f);
    }

    @Override
    public TerrainContactInfo findGroundBelow(Vector2 position, float probeDistance) {
        TerrainContactInfo bestContact = TerrainContactInfo.noGround(position);
        float bestSurfaceY = Float.NEGATIVE_INFINITY;

        for (TerrainCollisionData collisionData : collectCollisionData()) {
            TerrainContactInfo contact = new SegmentTerrainCollisionProvider(java.util.Collections.singletonList(collisionData))
                .findGroundBelow(position, probeDistance);
            if (contact.isGrounded() && contact.getContactPoint().y > bestSurfaceY) {
                bestSurfaceY = contact.getContactPoint().y;
                bestContact = contact;
            }
        }

        return bestContact;
    }

    @Override
    public boolean hasBlockingWall(float startX, float endX, float currentY, float maxStepHeight) {
        for (TerrainCollisionData collisionData : collectCollisionData()) {
            SegmentTerrainCollisionProvider provider =
                new SegmentTerrainCollisionProvider(java.util.Collections.singletonList(collisionData));
            if (provider.hasBlockingWall(startX, endX, currentY, maxStepHeight)) {
                return true;
            }
        }
        return false;
    }

    private Collection<TerrainCollisionData> collectCollisionData() {
        Collection<TerrainCollisionData> splineCollision = splineCollisionAdapter.build(runtimeLevel);
        if (!splineCollision.isEmpty()) {
            return splineCollision;
        }

        ArrayList<TerrainCollisionData> legacyCollision = new ArrayList<>();
        for (TerrainPath terrainPath : runtimeLevel.getTerrainPaths()) {
            legacyCollision.add(collisionBuilder.build(terrainPath));
        }
        return legacyCollision;
    }
}
