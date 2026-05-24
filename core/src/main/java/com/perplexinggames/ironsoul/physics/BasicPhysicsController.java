package com.perplexinggames.ironsoul.physics;

import com.badlogic.gdx.math.Vector2;
import com.perplexinggames.ironsoul.entities.Player;
import com.perplexinggames.ironsoul.level.LevelCollisionProvider;
import com.perplexinggames.ironsoul.level.RuntimeLevel;
import com.perplexinggames.ironsoul.terrain.TerrainCollisionProvider;
import com.perplexinggames.ironsoul.terrain.TerrainContactInfo;

public class BasicPhysicsController {
    public static final float GRAVITY = -900f;
    private static final float TERRAIN_FOOT_INSET = 14f;
    private static final float TERRAIN_PROBE_SKIN = 6f;
    private static final float TERRAIN_SNAP_EXTRA = 18f;

    private final Player player;
    private final RuntimeLevel runtimeLevel;
    private final LevelCollisionProvider levelCollisionProvider;
    private final TerrainCollisionProvider terrainCollisionProvider;

    public BasicPhysicsController(Player player, RuntimeLevel runtimeLevel, LevelCollisionProvider levelCollisionProvider) {
        this(player, runtimeLevel, levelCollisionProvider, null);
    }

    public BasicPhysicsController(
        Player player,
        RuntimeLevel runtimeLevel,
        LevelCollisionProvider levelCollisionProvider,
        TerrainCollisionProvider terrainCollisionProvider
    ) {
        this.player = player;
        this.runtimeLevel = runtimeLevel;
        this.levelCollisionProvider = levelCollisionProvider;
        this.terrainCollisionProvider = terrainCollisionProvider;
    }

    public void update(float delta) {
        if (!player.isPhysicsStarted()) {
            return;
        }

        player.applyGravity(GRAVITY, delta);

        float previousX = player.getX();
        player.setX(player.getX() + player.getVelocity().x * delta);
        if (levelCollisionProvider.collides(player.getBoundingBox())) {
            player.setX(previousX);
            player.getVelocity().x = 0f;
        }

        float previousY = player.getY();
        player.setY(player.getY() + player.getVelocity().y * delta);
        if (levelCollisionProvider.collides(player.getBoundingBox())) {
            boolean wasFalling = player.getVelocity().y < 0f;
            player.setY(previousY);
            player.getVelocity().y = 0f;
            player.setGrounded(wasFalling);
        } else {
            player.setGrounded(false);
            resolveTerrainGround(previousY);
        }

        clampPlayerToLevelBounds();
    }

    private void resolveTerrainGround(float previousY) {
        if (terrainCollisionProvider == null || player.getVelocity().y > 0f) {
            return;
        }

        float previousBottomY = previousY;
        float currentBottomY = player.getY();
        float probeStartY = Math.max(previousBottomY, currentBottomY) + TERRAIN_PROBE_SKIN;
        float probeDistance = Math.max(TERRAIN_SNAP_EXTRA, (probeStartY - currentBottomY) + TERRAIN_SNAP_EXTRA);

        Vector2 leftProbe = new Vector2(player.getX() + TERRAIN_FOOT_INSET, probeStartY);
        Vector2 rightProbe = new Vector2(player.getX() + player.getWidth() - TERRAIN_FOOT_INSET, probeStartY);
        TerrainContactInfo leftContact = terrainCollisionProvider.findGroundBelow(leftProbe, probeDistance);
        TerrainContactInfo rightContact = terrainCollisionProvider.findGroundBelow(rightProbe, probeDistance);

        if (!leftContact.isGrounded() && !rightContact.isGrounded()) {
            return;
        }

        float targetBottomY = Float.NEGATIVE_INFINITY;
        if (leftContact.isGrounded()) {
            targetBottomY = Math.max(targetBottomY, leftContact.getContactPoint().y);
        }
        if (rightContact.isGrounded()) {
            targetBottomY = Math.max(targetBottomY, rightContact.getContactPoint().y);
        }

        if (targetBottomY == Float.NEGATIVE_INFINITY || targetBottomY > probeStartY) {
            return;
        }

        player.setY(targetBottomY);
        player.getVelocity().y = 0f;
        player.setGrounded(true);
    }

    private void clampPlayerToLevelBounds() {
        float maxX = Math.max(0f, runtimeLevel.getPixelWidth() - player.getWidth());
        float maxY = Math.max(0f, runtimeLevel.getPixelHeight() - player.getHeight());

        player.setX(Math.max(0f, Math.min(player.getX(), maxX)));
        player.setY(Math.max(0f, Math.min(player.getY(), maxY)));
    }
}
