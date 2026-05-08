package com.perplexinggames.ironsoul.projectile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.perplexinggames.ironsoul.tank.controller.AimDirection;
import com.perplexinggames.ironsoul.tank.controller.FacingDirection;
import com.perplexinggames.ironsoul.tank.controller.TankModel;

public class ProjectileSystem {
    private final Array<Projectile> activeProjectiles = new Array<>();
    private final float worldWidth;
    private final float worldHeight;

    public ProjectileSystem(float worldWidth, float worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public void spawnPrimaryShot(TankModel tankModel, FacingDirection facingDirection, AimDirection aimDirection) {
        float baseX = tankModel.getX() + tankModel.getWidth() * 0.5f + facingDirection.sign() * 1.5f;
        float baseY = tankModel.getY() + tankModel.getHeight() * 0.7f;

        float directionX = facingDirection.sign();
        float directionY = switch (aimDirection) {
            case UP -> 0.45f;
            case DOWN -> -0.35f;
            case FORWARD -> 0f;
        };

        float speed = 18f;
        activeProjectiles.add(new Projectile(
            baseX,
            baseY,
            directionX * speed,
            directionY * speed,
            0.18f,
            2.4f
        ));
    }

    public void update(float delta) {
        for (int i = activeProjectiles.size - 1; i >= 0; i--) {
            Projectile projectile = activeProjectiles.get(i);
            projectile.update(delta);
            if (projectile.isExpired(worldWidth, worldHeight)) {
                activeProjectiles.removeIndex(i);
            }
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.95f, 0.65f, 0.18f, 1f));
        for (Projectile projectile : activeProjectiles) {
            shapeRenderer.circle(projectile.getX(), projectile.getY(), projectile.getRadius(), 12);
        }
        shapeRenderer.end();
    }

    public int getActiveProjectileCount() {
        return activeProjectiles.size;
    }
}
