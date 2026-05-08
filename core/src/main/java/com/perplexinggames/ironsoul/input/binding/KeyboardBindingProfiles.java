package com.perplexinggames.ironsoul.input.binding;

import com.perplexinggames.ironsoul.input.adapter.KeyboardControl;

public final class KeyboardBindingProfiles {
    private KeyboardBindingProfiles() {
    }

    public static ActionBindingProfile<KeyboardControl> createDefault() {
        return new ActionBindingProfile<>(KeyboardControl.class)
            .bind(InputAction.MOVE_LEFT, KeyboardControl.A)
            .bind(InputAction.MOVE_RIGHT, KeyboardControl.D)
            .bind(InputAction.JUMP, KeyboardControl.SPACE)
            .bind(InputAction.DASH, KeyboardControl.SHIFT_LEFT)
            .bind(InputAction.CROUCH, KeyboardControl.CONTROL_LEFT)
            .bind(InputAction.AIM_UP, KeyboardControl.W)
            .bind(InputAction.AIM_DOWN, KeyboardControl.S)
            .bind(InputAction.PRIMARY_FIRE, KeyboardControl.J)
            .bind(InputAction.SECONDARY_FIRE, KeyboardControl.K)
            .bind(InputAction.CHARGE_ATTACK, KeyboardControl.L)
            .bind(InputAction.MELEE_RAM, KeyboardControl.Q)
            .bind(InputAction.SHIELD, KeyboardControl.E)
            .bind(InputAction.INTERACT, KeyboardControl.F)
            .bind(InputAction.MAP, KeyboardControl.M)
            .bind(InputAction.PAUSE, KeyboardControl.ESCAPE)
            .bind(InputAction.INVENTORY, KeyboardControl.I);
    }
}
