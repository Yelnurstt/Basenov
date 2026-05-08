package com.perplexinggames.ironsoul.tank;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.perplexinggames.ironsoul.tank.combat.WeaponSystem;
import com.perplexinggames.ironsoul.tank.controller.AimDirection;
import com.perplexinggames.ironsoul.tank.controller.TankController;
import com.perplexinggames.ironsoul.tank.controller.TankModel;
import com.perplexinggames.ironsoul.tank.state.TankControlStateId;

public class TankDebugRenderer {
    public void render(ShapeRenderer shapeRenderer, TankController tankController, WeaponSystem weaponSystem) {
        TankModel model = tankController.getModel();
        float bodyHeight = tankController.isCrouching() ? model.getHeight() * 0.75f : model.getHeight();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(resolveBodyColor(tankController.getCurrentState().getId()));
        shapeRenderer.rect(model.getX(), model.getY(), model.getWidth(), bodyHeight);
        shapeRenderer.setColor(new Color(0.17f, 0.18f, 0.2f, 1f));
        shapeRenderer.rect(model.getX() + 0.2f, model.getY() - 0.18f, model.getWidth() - 0.4f, 0.22f);

        float turretBaseX = model.getX() + model.getWidth() * 0.52f;
        float turretBaseY = model.getY() + bodyHeight * 0.68f;
        float muzzleX = turretBaseX + weaponDirectionX(tankController, weaponSystem) * 1.55f;
        float muzzleY = turretBaseY + weaponDirectionY(weaponSystem) * 1.1f;

        shapeRenderer.setColor(new Color(0.88f, 0.91f, 0.95f, 1f));
        shapeRenderer.rectLine(turretBaseX, turretBaseY, muzzleX, muzzleY, 0.18f);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(new Color(0.08f, 0.1f, 0.12f, 1f));
        shapeRenderer.rect(model.getX(), model.getY(), model.getWidth(), bodyHeight);

        shapeRenderer.setColor(new Color(0.97f, 0.8f, 0.25f, 1f));
        float arrowStartX = model.getX() + model.getWidth() * 0.5f;
        float arrowStartY = model.getY() + bodyHeight + 0.55f;
        float arrowEndX = arrowStartX + tankController.getFacingDirection().sign() * 1.6f;
        shapeRenderer.line(arrowStartX, arrowStartY, arrowEndX, arrowStartY);
        shapeRenderer.line(arrowEndX, arrowStartY, arrowEndX - tankController.getFacingDirection().sign() * 0.35f, arrowStartY + 0.25f);
        shapeRenderer.line(arrowEndX, arrowStartY, arrowEndX - tankController.getFacingDirection().sign() * 0.35f, arrowStartY - 0.25f);
        shapeRenderer.end();
    }

    private Color resolveBodyColor(TankControlStateId stateId) {
        return switch (stateId) {
            case GROUNDED -> new Color(0.72f, 0.74f, 0.76f, 1f);
            case AIRBORNE -> new Color(0.84f, 0.7f, 0.36f, 1f);
            case WALL_SLIDE -> new Color(0.88f, 0.54f, 0.3f, 1f);
            case DASH -> new Color(0.48f, 0.78f, 0.97f, 1f);
            case STUNNED -> new Color(0.66f, 0.38f, 0.38f, 1f);
        };
    }

    private float weaponDirectionX(TankController tankController, WeaponSystem weaponSystem) {
        return switch (weaponSystem.getAimDirection()) {
            case FORWARD, UP, DOWN -> tankController.getFacingDirection().sign();
        };
    }

    private float weaponDirectionY(WeaponSystem weaponSystem) {
        return switch (weaponSystem.getAimDirection()) {
            case UP -> 0.85f;
            case DOWN -> -0.65f;
            case FORWARD -> 0f;
        };
    }
}
