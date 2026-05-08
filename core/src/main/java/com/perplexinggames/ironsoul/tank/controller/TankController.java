package com.perplexinggames.ironsoul.tank.controller;

import com.perplexinggames.ironsoul.tank.state.AirborneState;
import com.perplexinggames.ironsoul.tank.state.DashState;
import com.perplexinggames.ironsoul.tank.state.GroundedState;
import com.perplexinggames.ironsoul.tank.state.StunnedState;
import com.perplexinggames.ironsoul.tank.state.TankControlState;
import com.perplexinggames.ironsoul.tank.state.TankControlStateId;
import com.perplexinggames.ironsoul.tank.state.WallSlideState;

import java.util.EnumMap;
import java.util.Map;

public class TankController {
    private final TankModel model;
    private final TankPhysicsConfig config;
    private final Map<TankControlStateId, TankControlState> states = new EnumMap<>(TankControlStateId.class);

    private TankControlState currentState;
    private boolean moveLeftHeld;
    private boolean moveRightHeld;
    private boolean crouching;
    private float dashCooldownRemaining;
    private String lastAction = "System initialized";

    public TankController(TankModel model, TankPhysicsConfig config) {
        this.model = model;
        this.config = config;
        registerStates();
        currentState = states.get(TankControlStateId.GROUNDED);
        currentState.enter(this);
    }

    public void update(float delta) {
        dashCooldownRemaining = Math.max(0f, dashCooldownRemaining - delta);
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
        model.setX(model.getX() + model.getVelocityX() * delta);
        model.setY(model.getY() + model.getVelocityY() * delta);
        clampToArena();
    }

    public void integrateHorizontal(float delta) {
        model.setX(model.getX() + model.getVelocityX() * delta);
        clampToArena();
    }

    public void snapToGround() {
        model.setY(config.getGroundY());
        model.setVelocityY(0f);
    }

    public void launchJump() {
        model.setVelocityY(config.getJumpVelocity());
        lastAction = "Jump ignited";
    }

    public void launchWallJump() {
        float horizontalSign = isTouchingLeftWall() ? 1f : -1f;
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
        model.setVelocityX(dashDirection * config.getDashSpeed());
        model.setVelocityY(0f);
    }

    public void endDashMotion() {
        model.setVelocityX(model.getVelocityX() * 0.35f);
    }

    public void performRamImpulse() {
        float ramDirection = model.getFacingDirection().sign();
        model.setVelocityX(ramDirection * config.getRamImpulse());
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
        return model.getY() <= config.getGroundY() + 0.001f;
    }

    public boolean isTouchingLeftWall() {
        return model.getX() <= 0.001f;
    }

    public boolean isTouchingRightWall() {
        return model.getX() + model.getWidth() >= config.getWorldWidth() - 0.001f;
    }

    public boolean isEligibleForWallSlide() {
        return !isGrounded()
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

    private void registerStates() {
        states.put(TankControlStateId.GROUNDED, new GroundedState());
        states.put(TankControlStateId.AIRBORNE, new AirborneState());
        states.put(TankControlStateId.WALL_SLIDE, new WallSlideState());
        states.put(TankControlStateId.DASH, new DashState());
        states.put(TankControlStateId.STUNNED, new StunnedState());
    }

    private float getMoveAxis() {
        if (moveLeftHeld == moveRightHeld) {
            return 0f;
        }
        return moveLeftHeld ? -1f : 1f;
    }

    private void clampToArena() {
        float clampedX = Math.max(0f, Math.min(model.getX(), config.getWorldWidth() - model.getWidth()));
        model.setX(clampedX);

        if (model.getY() <= config.getGroundY()) {
            model.setY(config.getGroundY());
            model.setVelocityY(0f);
        }
    }

    private float approach(float current, float target, float delta) {
        if (current < target) {
            return Math.min(target, current + delta);
        }
        return Math.max(target, current - delta);
    }
}
