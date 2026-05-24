package com.perplexinggames.ironsoul.tank;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.perplexinggames.ironsoul.tank.combat.WeaponSystem;
import com.perplexinggames.ironsoul.tank.controller.TankController;
import com.perplexinggames.ironsoul.tank.controller.TankModel;
import com.perplexinggames.ironsoul.tank.state.TankControlStateId;
import com.perplexinggames.ironsoul.terrain.TerrainContactInfo;

public class TankDebugRenderer {
    public void render(ShapeRenderer shapeRenderer, TankController tankController, WeaponSystem weaponSystem) {
        TankModel model = tankController.getModel();
        float bodyHeight = tankController.isCrouching() ? model.getHeight() * 0.75f : model.getHeight();
        float rotation = model.getRotationDegrees();
        float bodyOriginX = model.getWidth() * 0.5f;
        float bodyOriginY = bodyHeight * 0.5f;
        float trackWidth = model.getWidth() - model.getWidth() * 0.15f;
        float trackHeight = Math.max(model.getHeight() * 0.16f, 0.22f);
        float trackX = model.getX() + (model.getWidth() - trackWidth) * 0.5f;
        float trackY = model.getY() - trackHeight * 0.55f;
        float trackOriginX = trackWidth * 0.5f + (trackX - model.getX());
        float trackOriginY = trackHeight * 0.5f + (trackY - model.getY());

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(resolveBodyColor(tankController.getCurrentState().getId()));
        shapeRenderer.rect(
            model.getX(),
            model.getY(),
            bodyOriginX,
            bodyOriginY,
            model.getWidth(),
            bodyHeight,
            1f,
            1f,
            rotation
        );
        shapeRenderer.setColor(new Color(0.17f, 0.18f, 0.2f, 1f));
        shapeRenderer.rect(
            trackX,
            trackY,
            trackOriginX,
            trackOriginY,
            trackWidth,
            trackHeight,
            1f,
            1f,
            rotation
        );

        Vector2 turretBase = rotatedLocalPoint(model, bodyHeight, model.getWidth() * 0.52f, bodyHeight * 0.68f);
        float cannonAngle = resolveCannonAngle(rotation, tankController, weaponSystem);
        float cannonLength = Math.max(model.getWidth() * 0.55f, 1.55f);
        Vector2 muzzle = new Vector2(
            turretBase.x + MathUtils.cosDeg(cannonAngle) * cannonLength,
            turretBase.y + MathUtils.sinDeg(cannonAngle) * cannonLength
        );

        shapeRenderer.setColor(new Color(0.88f, 0.91f, 0.95f, 1f));
        shapeRenderer.rectLine(turretBase.x, turretBase.y, muzzle.x, muzzle.y, Math.max(model.getHeight() * 0.09f, 0.18f));
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(new Color(0.08f, 0.1f, 0.12f, 1f));
        shapeRenderer.rect(
            model.getX(),
            model.getY(),
            bodyOriginX,
            bodyOriginY,
            model.getWidth(),
            bodyHeight,
            1f,
            1f,
            rotation
        );

        Vector2 facingStart = rotatedLocalPoint(model, bodyHeight, model.getWidth() * 0.5f, bodyHeight + Math.max(bodyHeight * 0.35f, 0.55f));
        Vector2 facingDirection = new Vector2(MathUtils.cosDeg(rotation), MathUtils.sinDeg(rotation)).scl(tankController.getFacingDirection().sign());
        float arrowLength = Math.max(model.getWidth() * 0.6f, 1.6f);
        Vector2 facingEnd = new Vector2(facingStart).mulAdd(facingDirection, arrowLength);
        shapeRenderer.setColor(new Color(0.97f, 0.8f, 0.25f, 1f));
        shapeRenderer.line(facingStart, facingEnd);

        float probeRadius = Math.max(model.getHeight() * 0.08f, 0.1f);
        renderProbe(shapeRenderer, tankController.getLeftTrackProbe(), tankController.getLeftTrackContact(), probeRadius);
        renderProbe(shapeRenderer, tankController.getRightTrackProbe(), tankController.getRightTrackContact(), probeRadius);

        if (tankController.isGrounded()) {
            Vector2 averagePoint = tankController.getAverageContactPoint();
            Vector2 normalEnd = new Vector2(averagePoint).mulAdd(tankController.getAverageSurfaceNormal(), Math.max(model.getHeight() * 0.65f, 0.9f));
            shapeRenderer.setColor(new Color(0.32f, 0.86f, 0.95f, 1f));
            shapeRenderer.circle(averagePoint.x, averagePoint.y, probeRadius * 1.2f, 14);
            shapeRenderer.line(averagePoint, normalEnd);
        }
        shapeRenderer.end();
    }

    private void renderProbe(ShapeRenderer shapeRenderer, Vector2 probe, TerrainContactInfo contact, float probeRadius) {
        shapeRenderer.setColor(new Color(0.91f, 0.35f, 0.3f, 1f));
        shapeRenderer.circle(probe.x, probe.y, probeRadius, 12);

        if (!contact.isGrounded()) {
            return;
        }

        Vector2 contactPoint = contact.getContactPoint();
        Vector2 normalEnd = new Vector2(contactPoint).mulAdd(contact.getSurfaceNormal(), probeRadius * 10f);
        shapeRenderer.setColor(new Color(0.94f, 0.82f, 0.27f, 1f));
        shapeRenderer.line(probe, contactPoint);
        shapeRenderer.circle(contactPoint.x, contactPoint.y, probeRadius * 1.1f, 12);
        shapeRenderer.setColor(new Color(0.38f, 0.92f, 0.83f, 1f));
        shapeRenderer.line(contactPoint, normalEnd);
    }

    private Vector2 rotatedLocalPoint(TankModel model, float bodyHeight, float localX, float localY) {
        float rotation = model.getRotationDegrees();
        float originX = model.getWidth() * 0.5f;
        float originY = bodyHeight * 0.5f;
        return new Vector2(localX, localY)
            .sub(originX, originY)
            .rotateDeg(rotation)
            .add(model.getX() + originX, model.getY() + originY);
    }

    private float resolveCannonAngle(float bodyRotation, TankController tankController, WeaponSystem weaponSystem) {
        float facingAngle = tankController.getFacingDirection().sign() > 0f ? 0f : 180f;
        return switch (weaponSystem.getAimDirection()) {
            case UP -> bodyRotation + facingAngle + 52f;
            case DOWN -> bodyRotation + facingAngle - 36f;
            case FORWARD -> bodyRotation + facingAngle;
        };
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
}
