package com.perplexinggames.ironsoul.physics;

import com.perplexinggames.ironsoul.entities.Player;
import com.perplexinggames.ironsoul.level.LevelCollisionProvider;
import com.perplexinggames.ironsoul.level.RuntimeLevel;

public class BasicPhysicsController {
    public static final float GRAVITY = -900f;

    private final Player player;
    private final RuntimeLevel runtimeLevel;
    private final LevelCollisionProvider levelCollisionProvider;

    public BasicPhysicsController(Player player, RuntimeLevel runtimeLevel, LevelCollisionProvider levelCollisionProvider) {
        this.player = player;
        this.runtimeLevel = runtimeLevel;
        this.levelCollisionProvider = levelCollisionProvider;
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
        }

        clampPlayerToLevelBounds();
    }

    private void clampPlayerToLevelBounds() {
        float maxX = Math.max(0f, runtimeLevel.getPixelWidth() - player.getWidth());
        float maxY = Math.max(0f, runtimeLevel.getPixelHeight() - player.getHeight());

        player.setX(Math.max(0f, Math.min(player.getX(), maxX)));
        player.setY(Math.max(0f, Math.min(player.getY(), maxY)));
    }
}
