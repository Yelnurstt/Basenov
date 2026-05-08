package com.perplexinggames.ironsoul.tank;

import com.perplexinggames.ironsoul.tank.controller.TankPhysicsConfig;

public class TankMovementConfig extends TankPhysicsConfig {
    public TankMovementConfig(
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
        super(
            worldWidth,
            groundY,
            moveAcceleration,
            airAcceleration,
            groundDeceleration,
            airDeceleration,
            maxMoveSpeed,
            crouchSpeedMultiplier,
            jumpVelocity,
            wallJumpHorizontalVelocity,
            gravity,
            maxFallSpeed,
            wallSlideMaxFallSpeed,
            dashSpeed,
            dashDuration,
            dashCooldown,
            ramImpulse,
            stunDuration
        );
    }

    public static TankMovementConfig demoDefault(float worldWidth) {
        return new TankMovementConfig(
            worldWidth,
            2f,
            40f,
            24f,
            30f,
            12f,
            8.5f,
            0.5f,
            13.5f,
            9f,
            -32f,
            21f,
            4.5f,
            16f,
            0.16f,
            0.8f,
            7f,
            0.4f
        );
    }
}
