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
    public float maxSpeed = 300f;
    public float friction = 0.85f;
    public float slopeResistance = 300f;
    public float gravity = -900f;
    public float groundSnapStrength = 40f;
    public float rotationSmoothing = 10f;

    public float trackWidth = 56f;
    public float probeHeightOffset = 40f;
    public float probeLength = 120f;

    private final TerrainCollisionProvider terrain;
    public final TerrainContactInfo leftContact = new TerrainContactInfo();
    public final TerrainContactInfo rightContact = new TerrainContactInfo();

    public TankPhysicsController(TerrainCollisionProvider terrain, float startX, float startY) {
        this.terrain = terrain;
        this.x = startX;
        this.y = startY;
    }

    public void update(float delta, float inputAxis) {
        // === 1. ДИНАМИЧЕСКИЕ ЩУПЫ (Инерция) ===
        float speedRatio = Math.abs(velocity.x) / maxSpeed;
        speedRatio = MathUtils.clamp(speedRatio, 0f, 1f);
        float speedFactor = speedRatio * speedRatio * speedRatio;

        float restingDist = probeHeightOffset;
        float minProbe = restingDist + 15f;
        float maxProbe = probeLength;

        float currentProbeDist = MathUtils.lerp(maxProbe, minProbe, speedFactor);

        float probeY = y + probeHeightOffset;

        terrain.getContactInfo(x - trackWidth / 2f, probeY, currentProbeDist, leftContact);
        terrain.getContactInfo(x + trackWidth / 2f, probeY, currentProbeDist, rightContact);

        boolean leftGrounded = leftContact.hasContact;
        boolean rightGrounded = rightContact.hasContact;

        // === 2. РАННИЙ ОТРЫВ (Трамплин для дальнего полета!) ===
        if (state == TankPhysicsState.GROUNDED && speedRatio > 0.4f && (leftGrounded != rightGrounded)) {
            boolean forcedDetachment = false;

            if (velocity.x > 0f && leftGrounded) {
                leftGrounded = false;
                forcedDetachment = true;
            } else if (velocity.x < 0f && rightGrounded) {
                rightGrounded = false;
                forcedDetachment = true;
            }

            if (forcedDetachment) {
                velocity.y += 150f * speedRatio;
                velocity.x *= 1f;
            }
        }

        int contacts = 0;
        if (leftGrounded) contacts++;
        if (rightGrounded) contacts++;

        if (contacts > 0) {
            state = TankPhysicsState.GROUNDED;

            float targetRot = 0f;
            float targetY = y;

            if (contacts == 2) {
                targetRot = new Vector2(rightContact.point.x - leftContact.point.x,
                    rightContact.point.y - leftContact.point.y).angleDeg();
                targetY = (leftContact.point.y + rightContact.point.y) / 2f;
            } else if (leftGrounded) {
                targetRot = leftContact.angle;
                targetY = leftContact.point.y + (trackWidth / 2f) * MathUtils.sinDeg(rotation);
            } else if (rightGrounded) {
                targetRot = rightContact.angle;
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

            // X БОЛЬШЕ НЕ ПРИБАВЛЯЕМ ЗДЕСЬ

        } else {
            state = TankPhysicsState.AIRBORNE;
            velocity.y += gravity * delta;
            velocity.x += inputAxis * (acceleration * 0.3f) * delta;
            velocity.x *= 0.98f;

            rotation = MathUtils.lerpAngleDeg(rotation, 0, (rotationSmoothing / 2f) * delta);

            // X БОЛЬШЕ НЕ ПРИБАВЛЯЕМ ЗДЕСЬ, только Y
            y += velocity.y * delta;
        }

        // ==========================================
        // 3. РАДАР СТЕН (Защита от прохождения насквозь)
        // ==========================================
        float nextX = x + velocity.x * delta;
        float bodyHalfWidth = trackWidth / 2f + 5f; // Выдвигаем радар за габариты гусениц
        float checkOffset = Math.signum(velocity.x) * bodyHalfWidth;
        float maxStepHeight = 15f; // Максимальная высота, которую танк может переехать

        // Проверяем стены по курсу движения
        if (velocity.x != 0 && terrain.hasBlockingWall(x, nextX + checkOffset, y, maxStepHeight)) {
            velocity.x = 0f; // Упираемся в стену, гасим скорость
        } else {
            x = nextX; // Путь свободен, едем!
        }
    }
}
