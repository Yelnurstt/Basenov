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
        float stunDuration,
        float probeInset,
        float probeStartHeight,
        float probeDistance,
        float trackToBodyOffset,
        float rotationSmoothing,
        float snapToGroundSmoothing,
        float slopeResistance
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
            stunDuration,
            probeInset,
            probeStartHeight,
            probeDistance,
            trackToBodyOffset,
            rotationSmoothing,
            snapToGroundSmoothing,
            slopeResistance
        );
    }

    public static TankMovementConfig demoDefault(float worldWidth) {
        return new TankMovementConfig(
            worldWidth,
            100f,
            420f,
            240f,
            360f,
            120f,
            170f,
            0.55f,
            220f,
            180f,
            -520f,
            340f,
            90f,
            280f,
            0.16f,
            0.8f,
            190f,
            0.4f,
            10f,
            34f,
            60f,
            12f,
            10f,
            12f,
            0.35f
        );
    }
}
