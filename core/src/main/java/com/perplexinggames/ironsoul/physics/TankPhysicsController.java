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

    // === ПАРАМЕТРЫ КРУТЫХ СКЛОНОВ ===
    public float maxClimbAngle = 55f; // Макс угол, на который танк может заехать
    public float maxStableGroundAngle = 35f; // Макс угол, на котором танк не скатывается без газа
    public float slopeSlideForce = 800f; // Сила, стаскивающая танк вниз на крутых склонах

    private final TerrainCollisionProvider terrain;
    public final TerrainContactInfo leftContact = new TerrainContactInfo();
    public final TerrainContactInfo rightContact = new TerrainContactInfo();
    public final TerrainContactInfo centerContact = new TerrainContactInfo(); // Центральный луч

    public TankPhysicsController(TerrainCollisionProvider terrain, float startX, float startY) {
        this.terrain = terrain;
        this.x = startX;
        this.y = startY;
    }

    public void update(float delta, float inputAxis) {
        float speedRatio = Math.abs(velocity.x) / maxSpeed;
        speedRatio = MathUtils.clamp(speedRatio, 0f, 1f);
        float speedFactor = speedRatio * speedRatio * speedRatio;

        float restingDist = probeHeightOffset;
        float minProbe = restingDist + 15f;
        float maxProbe = probeLength;

        float currentProbeDist = MathUtils.lerp(maxProbe, minProbe, speedFactor);
        float probeY = y + probeHeightOffset;

        // Три щупа
        terrain.getContactInfo(x - trackWidth / 2f, probeY, currentProbeDist, leftContact);
        terrain.getContactInfo(x + trackWidth / 2f, probeY, currentProbeDist, rightContact);
        terrain.getContactInfo(x, probeY, currentProbeDist, centerContact);

        boolean leftGrounded = leftContact.hasContact;
        boolean rightGrounded = rightContact.hasContact;
        boolean centerGrounded = centerContact.hasContact;

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
        if (centerGrounded) contacts++;

        if (contacts > 0) {
            state = TankPhysicsState.GROUNDED;

            float targetRot = 0f;
            float targetY = y;

            if (leftGrounded && rightGrounded) {
                targetRot = new Vector2(rightContact.point.x - leftContact.point.x,
                    rightContact.point.y - leftContact.point.y).angleDeg();
                targetY = (leftContact.point.y + rightContact.point.y) / 2f;

                // FIX Проверка на острые пики
                if (centerGrounded && targetY < centerContact.point.y) {
                    targetY = centerContact.point.y;
                }
            } else if (centerGrounded) {
                targetRot = centerContact.angle;
                targetY = centerContact.point.y;
            } else if (leftGrounded) {
                targetRot = leftContact.angle;
                targetY = leftContact.point.y + (trackWidth / 2f) * MathUtils.sinDeg(rotation);
            } else if (rightGrounded) {
                targetRot = rightContact.angle;
                targetY = rightContact.point.y - (trackWidth / 2f) * MathUtils.sinDeg(rotation);
            }

            if (targetRot > 180) targetRot -= 360;
            if (targetRot < -180) targetRot += 360;

            float currentSurfaceAngle = Math.abs(targetRot);

            // FIX Жестко выталкиваем танк на поверхность,
            if (y < targetY - 5f && velocity.y <= 0) {
                y = targetY;
            }

            rotation = MathUtils.lerpAngleDeg(rotation, targetRot, rotationSmoothing * delta);
            y = MathUtils.lerp(y, targetY, groundSnapStrength * delta);
            velocity.y = 0;

            Vector2 tangent = new Vector2(MathUtils.cosDeg(rotation), MathUtils.sinDeg(rotation));

            // FIX 3: Slope Limit Logic
            if (currentSurfaceAngle > maxClimbAngle) {
                float slideDirection = Math.signum(targetRot);
                velocity.add(-slideDirection * tangent.x * slopeSlideForce * delta, -slideDirection * tangent.y * slopeSlideForce * delta);
                velocity.scl(friction * 0.95f);
            } else {
                if (inputAxis != 0) {
                    velocity.add(tangent.x * inputAxis * acceleration * delta, tangent.y * inputAxis * acceleration * delta);
                } else {
                    velocity.scl(friction);
                }

                // Скатывание на средних склонах без нажатия кнопок движения
                if (currentSurfaceAngle > maxStableGroundAngle && inputAxis == 0) {
                    float slideDirection = Math.signum(targetRot);
                    velocity.add(-slideDirection * tangent.x * (slopeSlideForce * 0.25f) * delta, -slideDirection * tangent.y * (slopeSlideForce * 0.25f) * delta);
                }

                float slopePull = -MathUtils.sinDeg(rotation) * slopeResistance * delta;
                velocity.add(tangent.x * slopePull, tangent.y * slopePull);
            }

            if (velocity.len() > maxSpeed) velocity.nor().scl(maxSpeed);

        } else {
            state = TankPhysicsState.AIRBORNE;
            velocity.y += gravity * delta;
            velocity.x += inputAxis * (acceleration * 0.3f) * delta;
            velocity.x *= 0.98f;
            rotation = MathUtils.lerpAngleDeg(rotation, 0, (rotationSmoothing / 2f) * delta);
            y += velocity.y * delta;
        }

        // Радар стен 
        float nextX = x + velocity.x * delta;
        float bodyHalfWidth = trackWidth / 2f + 5f;
        float checkOffset = Math.signum(velocity.x) * bodyHalfWidth;
        float maxStepHeight = 15f;

        if (velocity.x != 0 && terrain.hasBlockingWall(x, nextX + checkOffset, y, maxStepHeight)) {
            velocity.x = 0f;
        } else {
            x = nextX;
        }
    }
}
