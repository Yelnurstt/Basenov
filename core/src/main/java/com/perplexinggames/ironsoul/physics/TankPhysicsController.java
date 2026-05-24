package com.perplexinggames.ironsoul.physics;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.perplexinggames.ironsoul.terrain.TerrainCollisionProvider;
import com.perplexinggames.ironsoul.terrain.TerrainContactInfo;

public class TankPhysicsController {
    public float x, y, rotation;
    public final Vector2 velocity = new Vector2();
    public TankPhysicsState state = TankPhysicsState.AIRBORNE;

    public float acceleration = 600f;
    public float maxSpeed = 250f;
    public float friction = 0.85f;
    public float slopeResistance = 300f;
    public float gravity = -900f;
    public float groundSnapStrength = 40f; // жерге жабысып тұру
    public float rotationSmoothing = 10f;

    public float trackWidth = 56f;
    // ФИКС: Поднимаем старт луча выше и делаем его длиннее, чтобы не промахивался на склонах
    public float probeHeightOffset = 40f;
    public float probeLength = 120f; // лучи длинее чтоб танк не летал

    private final TerrainCollisionProvider terrain;
    public final TerrainContactInfo leftContact = new TerrainContactInfo();
    public final TerrainContactInfo rightContact = new TerrainContactInfo();

    public TankPhysicsController(TerrainCollisionProvider terrain, float startX, float startY) {
        this.terrain = terrain;
        this.x = startX;
        this.y = startY;
    }

    public void update(float delta, float inputAxis) {
        float probeY = y + probeHeightOffset;

        terrain.getContactInfo(x - trackWidth / 2f, probeY, probeLength, leftContact);
        terrain.getContactInfo(x + trackWidth / 2f, probeY, probeLength, rightContact);

        int contacts = 0;
        if (leftContact.hasContact) contacts++;
        if (rightContact.hasContact) contacts++;

        if (contacts > 0) {
            state = TankPhysicsState.GROUNDED;

            float targetRot = 0f;
            float targetY = y;

            // ФИКС: Правильно считаем Y центра танка в зависимости от того, сколько гусениц касаются земли
            if (contacts == 2) {
                targetRot = new Vector2(rightContact.point.x - leftContact.point.x,
                    rightContact.point.y - leftContact.point.y).angleDeg();
                targetY = (leftContact.point.y + rightContact.point.y) / 2f;
            } else if (leftContact.hasContact) {
                targetRot = leftContact.angle;
                // Если касается только зад, центр танка ВЫШЕ гусеницы с учетом угла
                targetY = leftContact.point.y + (trackWidth / 2f) * MathUtils.sinDeg(rotation);
            } else if (rightContact.hasContact) {
                targetRot = rightContact.angle;
                // Если касается только перед, центр танка НИЖЕ гусеницы с учетом угла
                targetY = rightContact.point.y - (trackWidth / 2f) * MathUtils.sinDeg(rotation);
            }

            rotation = MathUtils.lerpAngleDeg(rotation, targetRot, rotationSmoothing * delta);
            y = MathUtils.lerp(y, targetY, groundSnapStrength * delta);
            velocity.y = 0;

            Vector2 tangent = new Vector2(MathUtils.cosDeg(rotation), MathUtils.sinDeg(rotation));

            if (inputAxis != 0) {
                velocity.add(tangent.x * inputAxis * acceleration * delta, tangent.y * inputAxis * acceleration * delta);
            } else {
                velocity.scl(friction);
            }

            float slopePull = -MathUtils.sinDeg(rotation) * slopeResistance * delta;
            velocity.add(tangent.x * slopePull, tangent.y * slopePull);

            if (velocity.len() > maxSpeed) velocity.nor().scl(maxSpeed);

            x += velocity.x * delta;

        } else {
            state = TankPhysicsState.AIRBORNE;
            velocity.y += gravity * delta;
            velocity.x += inputAxis * (acceleration * 0.3f) * delta;
            velocity.x *= 0.98f;

            rotation = MathUtils.lerpAngleDeg(rotation, 0, (rotationSmoothing / 2f) * delta);

            x += velocity.x * delta;
            y += velocity.y * delta;
        }
    }
}
