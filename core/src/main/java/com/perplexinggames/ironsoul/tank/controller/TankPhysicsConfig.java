package com.perplexinggames.ironsoul.tank.controller;

public class TankPhysicsConfig {
    private final float worldWidth;
    private final float groundY;
    private final float moveAcceleration;
    private final float airAcceleration;
    private final float groundDeceleration;
    private final float airDeceleration;
    private final float maxMoveSpeed;
    private final float crouchSpeedMultiplier;
    private final float jumpVelocity;
    private final float wallJumpHorizontalVelocity;
    private final float gravity;
    private final float maxFallSpeed;
    private final float wallSlideMaxFallSpeed;
    private final float dashSpeed;
    private final float dashDuration;
    private final float dashCooldown;
    private final float ramImpulse;
    private final float stunDuration;

    public TankPhysicsConfig(
        float worldWidth,
        float groundY,
        float moveAcceleration,
        float airAcceleration,
        float groundDeceleration,
        float airDeceleration,
        float maxMoveSpeed,
        float crouchSpeedMultiplier,
        float jumpVelocity,
        float wallJumpHorizontalVelocity,
        float gravity,
        float maxFallSpeed,
        float wallSlideMaxFallSpeed,
        float dashSpeed,
        float dashDuration,
        float dashCooldown,
        float ramImpulse,
        float stunDuration
    ) {
        this.worldWidth = worldWidth;
        this.groundY = groundY;
        this.moveAcceleration = moveAcceleration;
        this.airAcceleration = airAcceleration;
        this.groundDeceleration = groundDeceleration;
        this.airDeceleration = airDeceleration;
        this.maxMoveSpeed = maxMoveSpeed;
        this.crouchSpeedMultiplier = crouchSpeedMultiplier;
        this.jumpVelocity = jumpVelocity;
        this.wallJumpHorizontalVelocity = wallJumpHorizontalVelocity;
        this.gravity = gravity;
        this.maxFallSpeed = maxFallSpeed;
        this.wallSlideMaxFallSpeed = wallSlideMaxFallSpeed;
        this.dashSpeed = dashSpeed;
        this.dashDuration = dashDuration;
        this.dashCooldown = dashCooldown;
        this.ramImpulse = ramImpulse;
        this.stunDuration = stunDuration;
    }

    public static TankPhysicsConfig defaultConfig() {
        return new TankPhysicsConfig(
            40f,
            2f,
            44f,
            24f,
            36f,
            14f,
            9f,
            0.45f,
            14.5f,
            10f,
            -34f,
            22f,
            4.5f,
            18f,
            0.18f,
            0.75f,
            8f,
            0.45f
        );
    }

    public float getWorldWidth() {
        return worldWidth;
    }

    public float getGroundY() {
        return groundY;
    }

    public float getMoveAcceleration() {
        return moveAcceleration;
    }

    public float getAirAcceleration() {
        return airAcceleration;
    }

    public float getGroundDeceleration() {
        return groundDeceleration;
    }

    public float getAirDeceleration() {
        return airDeceleration;
    }

    public float getMaxMoveSpeed() {
        return maxMoveSpeed;
    }

    public float getCrouchSpeedMultiplier() {
        return crouchSpeedMultiplier;
    }

    public float getJumpVelocity() {
        return jumpVelocity;
    }

    public float getWallJumpHorizontalVelocity() {
        return wallJumpHorizontalVelocity;
    }

    public float getGravity() {
        return gravity;
    }

    public float getMaxFallSpeed() {
        return maxFallSpeed;
    }

    public float getWallSlideMaxFallSpeed() {
        return wallSlideMaxFallSpeed;
    }

    public float getDashSpeed() {
        return dashSpeed;
    }

    public float getDashDuration() {
        return dashDuration;
    }

    public float getDashCooldown() {
        return dashCooldown;
    }

    public float getRamImpulse() {
        return ramImpulse;
    }

    public float getStunDuration() {
        return stunDuration;
    }
}
