package com.perplexinggames.ironsoul.tank.controller;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.perplexinggames.ironsoul.tank.state.AirborneState;
import com.perplexinggames.ironsoul.tank.state.DashState;
import com.perplexinggames.ironsoul.tank.state.GroundedState;
import com.perplexinggames.ironsoul.tank.state.StunnedState;
import com.perplexinggames.ironsoul.tank.state.TankControlState;
import com.perplexinggames.ironsoul.tank.state.TankControlStateId;
import com.perplexinggames.ironsoul.tank.state.WallSlideState;
import com.perplexinggames.ironsoul.terrain.TerrainCollisionProvider;
import com.perplexinggames.ironsoul.terrain.TerrainContactInfo;

import java.util.EnumMap;
import java.util.Map;

public class TankController {
    private final TankModel model;
    private final TankPhysicsConfig config;
    private final TerrainCollisionProvider terrainCollisionProvider;
    private final Map<TankControlStateId, TankControlState> states = new EnumMap<>(TankControlStateId.class);
    private final Vector2 leftTrackProbe = new Vector2();
    private final Vector2 rightTrackProbe = new Vector2();
    private final Vector2 averageContactPoint = new Vector2();
    private final Vector2 averageSurfaceNormal = new Vector2(0f, 1f);
    private final Vector2 averageSurfaceTangent = new Vector2(1f, 0f);

    private TankControlState currentState;
    private TerrainContactInfo leftTrackContact = TerrainContactInfo.noGround(new Vector2());
    private TerrainContactInfo rightTrackContact = TerrainContactInfo.noGround(new Vector2());
    private boolean moveLeftHeld;
    private boolean moveRightHeld;
    private boolean crouching;
    private boolean grounded;
    private float dashCooldownRemaining;
    private float detachFromGroundRemaining;
    private float surfaceAngle;
    private float targetTankRotation;
    private float activeGroundFriction = 1f;
    private String lastAction = "System initialized";

    public TankController(TankModel model, TankPhysicsConfig config, TerrainCollisionProvider terrainCollisionProvider) {
        this.model = model;
        this.config = config;
        this.terrainCollisionProvider = terrainCollisionProvider;
        registerStates();
        refreshGroundContacts();
        currentState = states.get(grounded ? TankControlStateId.GROUNDED : TankControlStateId.AIRBORNE);
        currentState.enter(this);
    }

    public void update(float delta) {
        dashCooldownRemaining = Math.max(0f, dashCooldownRemaining - delta);
        detachFromGroundRemaining = Math.max(0f, detachFromGroundRemaining - delta);
        currentState.update(this, delta);
    }

    public void beginMove(HorizontalDirection direction) {
        if (direction == HorizontalDirection.LEFT) {
            moveLeftHeld = true;
            model.setFacingDirection(FacingDirection.LEFT);
        } else {
            moveRightHeld = true;
            model.setFacingDirection(FacingDirection.RIGHT);
        }
    }

    public void endMove(HorizontalDirection direction) {
        if (direction == HorizontalDirection.LEFT) {
            moveLeftHeld = false;
        } else {
            moveRightHeld = false;
        }
    }

    public void requestJump() {
        currentState.onJumpRequested(this);
    }

    public void requestDash() {
        currentState.onDashRequested(this);
    }

    public void requestMeleeRam() {
        currentState.onMeleeRamRequested(this);
    }

    public void setCrouching(boolean crouching) {
        this.crouching = crouching;
        currentState.onCrouchChanged(this, crouching);
        lastAction = crouching ? "Suspension lowered" : "Suspension reset";
    }

    public void applyGroundMovement(float delta) {
        refreshGroundContacts();
        if (!grounded) {
            smoothAirborneRotation(delta);
            return;
        }

        Vector2 tangent = resolveGroundMovementTangent();
        float targetAxis = getMoveAxis();
        float speedLimit = crouching ? config.getMaxMoveSpeed() * config.getCrouchSpeedMultiplier() : config.getMaxMoveSpeed();
        float tangentSpeed = model.getVelocityX() * tangent.x + model.getVelocityY() * tangent.y;

        if (targetAxis != 0f) {
            boolean movingUphill = (targetAxis > 0f && tangent.y > 0f) || (targetAxis < 0f && tangent.y < 0f);
            float uphillPenalty = movingUphill ? speedLimit * config.getSlopeResistance() * Math.abs(tangent.y) : 0f;
            float targetSpeed = targetAxis * Math.max(0f, speedLimit - uphillPenalty);
            tangentSpeed = approach(tangentSpeed, targetSpeed, config.getMoveAcceleration() * delta);
            model.setFacingDirection(targetAxis < 0f ? FacingDirection.LEFT : FacingDirection.RIGHT);
        } else {
            float effectiveDeceleration = config.getGroundDeceleration() * activeGroundFriction;
            tangentSpeed = approach(tangentSpeed, 0f, effectiveDeceleration * delta);
        }

        model.setVelocityX(tangent.x * tangentSpeed);
        model.setVelocityY(tangent.y * tangentSpeed);

        // ФИКС СТЕН: Двигаемся с учетом вертикальных преград (Радар)
        moveWithWallCollision(delta);

        refreshGroundContacts();
        if (grounded) {
            alignToGround(delta, false);
            return;
        }

        smoothAirborneRotation(delta);
    }

    public void applyHorizontalMovement(float delta, float acceleration, float deceleration) {
        float targetAxis = getMoveAxis();
        float speedLimit = crouching ? config.getMaxMoveSpeed() * config.getCrouchSpeedMultiplier() : config.getMaxMoveSpeed();

        if (targetAxis != 0f) {
            float targetVelocity = targetAxis * speedLimit;
            model.setVelocityX(approach(model.getVelocityX(), targetVelocity, acceleration * delta));
            model.setFacingDirection(targetAxis < 0f ? FacingDirection.LEFT : FacingDirection.RIGHT);
            return;
        }

        applyHorizontalDecay(delta, deceleration);
    }

    public void applyHorizontalDecay(float delta, float deceleration) {
        model.setVelocityX(approach(model.getVelocityX(), 0f, deceleration * delta));
    }

    public void applyGravity(float delta) {
        float nextVelocity = model.getVelocityY() + config.getGravity() * delta;
        model.setVelocityY(Math.max(-config.getMaxFallSpeed(), nextVelocity));
    }

    public void applyWallSlide(float delta) {
        applyHorizontalDecay(delta, config.getGroundDeceleration());
        float nextVelocity = model.getVelocityY() + config.getGravity() * delta;
        model.setVelocityY(Math.max(-config.getWallSlideMaxFallSpeed(), nextVelocity));
    }

    public void integrate(float delta) {
        // ФИКС СТЕН: Двигаемся с учетом вертикальных преград (Радар)
        moveWithWallCollision(delta);
        refreshGroundContacts();

        if (grounded) {
            alignToGround(delta, false);
            return;
        }

        smoothAirborneRotation(delta);
    }

    // ===============================================
    // НОВЫЙ МЕТОД: РАДАР СТЕН (Защита от прохождения сквозь 90 градусов)
    // ===============================================
    private void moveWithWallCollision(float delta) {
        float vx = model.getVelocityX();
        float oldX = model.getX();
        float nextX = oldX + vx * delta;

        if (vx != 0f) {
            float bodyHalfWidth = model.getWidth() * 0.5f;
            // Радар "смотрит" немного вперед по ходу движения
            float checkOffset = Math.signum(vx) * (bodyHalfWidth + 5f);
            float centerX = oldX + bodyHalfWidth;
            float nextCenterX = nextX + bodyHalfWidth;

            // 15f - максимальная высота препятствия
            if (terrainCollisionProvider.hasBlockingWall(centerX, nextCenterX + checkOffset, model.getY(), 15f)) {
                model.setVelocityX(0f); // Гасим скорость
                nextX = oldX;           // Останавливаемся ровно ПЕРЕД стеной
            }
        }

        model.setX(nextX);
        model.setY(model.getY() + model.getVelocityY() * delta);
        clampToArena();
    }

    public void snapToGround() {
        refreshGroundContacts();
        if (!grounded) {
            return;
        }
        alignToGround(1f, true);
    }

    public void launchJump() {
        detachFromGroundRemaining = 0.12f;
        model.setVelocityY(config.getJumpVelocity());
        lastAction = "Jump ignited";
    }

    public void launchWallJump() {
        float horizontalSign = isTouchingLeftWall() ? 1f : -1f;
        detachFromGroundRemaining = 0.12f;
        model.setVelocityX(horizontalSign * config.getWallJumpHorizontalVelocity());
        model.setVelocityY(config.getJumpVelocity() * 0.92f);
        model.setFacingDirection(horizontalSign > 0f ? FacingDirection.RIGHT : FacingDirection.LEFT);
        lastAction = "Wall jump executed";
    }

    public void startDash() {
        if (dashCooldownRemaining > 0f) {
            lastAction = "Dash cooling";
            return;
        }

        dashCooldownRemaining = config.getDashCooldown();
        changeState(TankControlStateId.DASH);
        lastAction = "Dash thrusters engaged";
    }

    public void beginDashMotion() {
        float axis = getMoveAxis();
        float dashDirection = axis != 0f ? axis : model.getFacingDirection().sign();
        Vector2 impulse = buildGroundAwareImpulse(dashDirection * config.getDashSpeed());
        model.setVelocityX(impulse.x);
        model.setVelocityY(impulse.y);
    }

    public void endDashMotion() {
        model.setVelocityX(model.getVelocityX() * 0.35f);
        model.setVelocityY(model.getVelocityY() * 0.35f);
    }

    public void performRamImpulse() {
        float ramDirection = model.getFacingDirection().sign();
        Vector2 impulse = buildGroundAwareImpulse(ramDirection * config.getRamImpulse());
        model.setVelocityX(impulse.x);
        model.setVelocityY(impulse.y);
        lastAction = "Ram burst committed";
    }

    public void enterStunnedState() {
        changeState(TankControlStateId.STUNNED);
        lastAction = "Stabilizers offline";
    }

    public void changeState(TankControlStateId nextStateId) {
        TankControlState nextState = states.get(nextStateId);
        if (nextState == null || nextState == currentState) {
            return;
        }

        currentState.exit(this);
        currentState = nextState;
        currentState.enter(this);
    }

    public boolean isGrounded() {
        return grounded;
    }

    public boolean isTouchingLeftWall() {
        return model.getX() <= 0.001f;
    }

    public boolean isTouchingRightWall() {
        return model.getX() + model.getWidth() >= config.getWorldWidth() - 0.001f;
    }

    public boolean isEligibleForWallSlide() {
        return !grounded
            && model.getVelocityY() < 0f
            && ((isTouchingLeftWall() && getMoveAxis() < 0f) || (isTouchingRightWall() && getMoveAxis() > 0f));
    }

    public boolean canUseCombatActions() {
        return currentState.allowsCombat();
    }

    public boolean canUseShield() {
        return currentState.allowsShield();
    }

    public TankControlState getCurrentState() {
        return currentState;
    }

    public TankModel getModel() {
        return model;
    }

    public TankPhysicsConfig getConfig() {
        return config;
    }

    public boolean isCrouching() {
        return crouching;
    }

    public float getDashCooldownRemaining() {
        return dashCooldownRemaining;
    }

    public String getLastAction() {
        return lastAction;
    }

    public FacingDirection getFacingDirection() {
        return model.getFacingDirection();
    }

    public Vector2 getLeftTrackProbe() {
        return new Vector2(leftTrackProbe);
    }

    public Vector2 getRightTrackProbe() {
        return new Vector2(rightTrackProbe);
    }

    public TerrainContactInfo getLeftTrackContact() {
        return leftTrackContact;
    }

    public TerrainContactInfo getRightTrackContact() {
        return rightTrackContact;
    }

    public Vector2 getAverageContactPoint() {
        return new Vector2(averageContactPoint);
    }

    public Vector2 getAverageSurfaceNormal() {
        return new Vector2(averageSurfaceNormal);
    }

    public Vector2 getAverageSurfaceTangent() {
        return new Vector2(averageSurfaceTangent);
    }

    public float getSurfaceAngle() {
        return surfaceAngle;
    }

    public float getTargetTankRotation() {
        return targetTankRotation;
    }

    private void registerStates() {
        states.put(TankControlStateId.GROUNDED, new GroundedState());
        states.put(TankControlStateId.AIRBORNE, new AirborneState());
        states.put(TankControlStateId.WALL_SLIDE, new WallSlideState());
        states.put(TankControlStateId.DASH, new DashState());
        states.put(TankControlStateId.STUNNED, new StunnedState());
    }

    private void refreshGroundContacts() {
        float extraHeight = 30f;
        float probeY = model.getY() + config.getProbeStartHeight() + extraHeight;

        leftTrackProbe.set(model.getX() + config.getProbeInset(), probeY);
        rightTrackProbe.set(model.getX() + model.getWidth() - config.getProbeInset(), probeY);
        Vector2 centerTrackProbe = new Vector2(model.getX() + model.getWidth() * 0.5f, probeY);

        float speedRatio = Math.abs(model.getVelocityX()) / config.getMaxMoveSpeed();
        speedRatio = MathUtils.clamp(speedRatio, 0f, 1f);
        float speedFactor = speedRatio * speedRatio * speedRatio;

        float restingDist = config.getProbeStartHeight() + config.getTrackToBodyOffset();
        float minProbe = restingDist + 15f;
        float maxProbe = config.getProbeDistance();

        float currentProbeDist = MathUtils.lerp(maxProbe, minProbe, speedFactor) + extraHeight;

        leftTrackContact = terrainCollisionProvider.findGroundBelow(leftTrackProbe, currentProbeDist);
        rightTrackContact = terrainCollisionProvider.findGroundBelow(rightTrackProbe, currentProbeDist);
        TerrainContactInfo centerContact = terrainCollisionProvider.findGroundBelow(centerTrackProbe, currentProbeDist);

        boolean leftGrounded = leftTrackContact.isGrounded();
        boolean rightGrounded = rightTrackContact.isGrounded();
        boolean centerGrounded = centerContact.isGrounded();

        if (grounded && speedRatio > 0.3f && (leftGrounded != rightGrounded)) {
            boolean forcedDetachment = false;
            if (model.getVelocityX() > 0f && !rightGrounded && leftGrounded) {
                leftGrounded = false;
                forcedDetachment = true;
            } else if (model.getVelocityX() < 0f && !leftGrounded && rightGrounded) {
                rightGrounded = false;
                forcedDetachment = true;
            }

            if (forcedDetachment) {
                model.setVelocityY(model.getVelocityY() + 180f * speedRatio);
                model.setVelocityX(model.getVelocityX() * 1.05f);
            }
        }

        boolean computedGrounded = leftGrounded || rightGrounded || centerGrounded;

        if (detachFromGroundRemaining > 0f) {
            computedGrounded = false;
        }
        if (currentState != null && currentState.getId() != TankControlStateId.GROUNDED && model.getVelocityY() > 0f) {
            computedGrounded = false;
        }

        grounded = computedGrounded;
        if (!grounded) {
            averageContactPoint.set(centerTrackProbe.x, model.getY() - config.getTrackToBodyOffset());
            averageSurfaceNormal.set(0f, 1f);
            averageSurfaceTangent.set(1f, 0f);
            surfaceAngle = 0f;
            targetTankRotation = 0f;
            activeGroundFriction = 1f;
            return;
        }

        if (leftGrounded && rightGrounded) {
            averageSurfaceNormal.set(leftTrackContact.getSurfaceNormal()).add(rightTrackContact.getSurfaceNormal()).nor();
            averageSurfaceTangent.set(leftTrackContact.getSurfaceTangent()).add(rightTrackContact.getSurfaceTangent());
            if (averageSurfaceTangent.isZero(0.001f)) {
                averageSurfaceTangent.set(rightTrackContact.getContactPoint()).sub(leftTrackContact.getContactPoint()).nor();
            } else {
                averageSurfaceTangent.nor();
            }
            if (averageSurfaceTangent.x < 0f) {
                averageSurfaceTangent.scl(-1f);
            }
            surfaceAngle = MathUtils.atan2(
                rightTrackContact.getContactPoint().y - leftTrackContact.getContactPoint().y,
                rightTrackContact.getContactPoint().x - leftTrackContact.getContactPoint().x
            ) * MathUtils.radiansToDegrees;
            targetTankRotation = surfaceAngle;
            activeGroundFriction = (leftTrackContact.getFriction() + rightTrackContact.getFriction()) * 0.5f;

            float midY = (leftTrackContact.getContactPoint().y + rightTrackContact.getContactPoint().y) * 0.5f;
            if (centerGrounded && centerContact.getContactPoint().y > midY) {
                averageContactPoint.set(centerContact.getContactPoint());
            } else {
                averageContactPoint.set(
                    (leftTrackContact.getContactPoint().x + rightTrackContact.getContactPoint().x) * 0.5f,
                    midY
                );
            }
            return;
        }

        TerrainContactInfo contact = centerGrounded ? centerContact : (leftGrounded ? leftTrackContact : rightTrackContact);
        averageContactPoint.set(contact.getContactPoint());
        averageSurfaceNormal.set(contact.getSurfaceNormal());
        averageSurfaceTangent.set(contact.getSurfaceTangent());
        if (averageSurfaceTangent.x < 0f) {
            averageSurfaceTangent.scl(-1f);
        }
        surfaceAngle = contact.getSurfaceAngle();
        targetTankRotation = surfaceAngle;
        activeGroundFriction = contact.getFriction();
    }

    private void updateProbePositions() {
        // Оставили пустым (логика теперь в refreshGroundContacts)
    }

    private void alignToGround(float delta, boolean immediate) {
        float snapAlpha = immediate ? 1f : Math.min(1f, config.getSnapToGroundSmoothing() * delta);
        float rotationAlpha = immediate ? 1f : Math.min(1f, config.getRotationSmoothing() * delta);
        float targetBodyY = averageContactPoint.y + config.getTrackToBodyOffset();

        model.setY(MathUtils.lerp(model.getY(), targetBodyY, snapAlpha));
        model.setRotationDegrees(MathUtils.lerpAngleDeg(model.getRotationDegrees(), targetTankRotation, rotationAlpha));

        if (currentState == null || currentState.getId() != TankControlStateId.GROUNDED) {
            model.setVelocityY(0f);
        }
    }

    private void smoothAirborneRotation(float delta) {
        float rotationAlpha = Math.min(1f, config.getRotationSmoothing() * 0.35f * delta);
        model.setRotationDegrees(MathUtils.lerpAngleDeg(model.getRotationDegrees(), 0f, rotationAlpha));
    }

    private Vector2 resolveGroundMovementTangent() {
        Vector2 tangent = new Vector2(averageSurfaceTangent);
        if (tangent.isZero(0.001f)) {
            tangent.set(1f, 0f);
        }
        if (tangent.x < 0f) {
            tangent.scl(-1f);
        }
        return tangent.nor();
    }

    private Vector2 buildGroundAwareImpulse(float signedMagnitude) {
        if (!grounded) {
            return new Vector2(signedMagnitude, 0f);
        }
        return resolveGroundMovementTangent().scl(signedMagnitude);
    }

    private float getMoveAxis() {
        if (moveLeftHeld == moveRightHeld) {
            return 0f;
        }
        return moveLeftHeld ? -1f : 1f;
    }

    private void clampToArena() {
        if (model.getX() < 0f) {
            model.setX(0f);
            model.setVelocityX(Math.max(0f, model.getVelocityX()));
        } else if (model.getX() + model.getWidth() > config.getWorldWidth()) {
            model.setX(config.getWorldWidth() - model.getWidth());
            model.setVelocityX(Math.min(0f, model.getVelocityX()));
        }
    }

    private float approach(float current, float target, float delta) {
        if (current < target) {
            return Math.min(target, current + delta);
        }
        return Math.max(target, current - delta);
    }
}
