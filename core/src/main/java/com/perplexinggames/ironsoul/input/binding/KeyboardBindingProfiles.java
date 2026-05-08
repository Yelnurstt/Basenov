package com.perplexinggames.ironsoul.input.binding;

import com.perplexinggames.ironsoul.input.adapter.KeyboardControl;

public final class KeyboardBindingProfiles {
    private KeyboardBindingProfiles() {
    }

    public static ActionBindingProfile<KeyboardControl> createDefault() {
        return new ActionBindingProfile<>(KeyboardControl.class)
            .bind(InputAction.MOVE_LEFT, KeyboardControl.A, KeyboardControl.LEFT)
            .bind(InputAction.MOVE_RIGHT, KeyboardControl.D, KeyboardControl.RIGHT)
            .bind(InputAction.JUMP, KeyboardControl.SPACE)
            .bind(InputAction.DASH, KeyboardControl.SHIFT_LEFT, KeyboardControl.SHIFT_RIGHT)
            .bind(InputAction.CROUCH, KeyboardControl.CONTROL_LEFT)
            .bind(InputAction.AIM_UP, KeyboardControl.W)
            .bind(InputAction.AIM_DOWN, KeyboardControl.S)
            .bind(InputAction.PRIMARY_FIRE, KeyboardControl.J, KeyboardControl.MOUSE_LEFT)
            .bind(InputAction.SECONDARY_FIRE, KeyboardControl.K)
            .bind(InputAction.CHARGE_ATTACK, KeyboardControl.L)
            .bind(InputAction.MELEE_RAM, KeyboardControl.Q)
            .bind(InputAction.INTERACT, KeyboardControl.E)
            .bind(InputAction.MAP, KeyboardControl.M)
            .bind(InputAction.PAUSE, KeyboardControl.ESCAPE)
            .bind(InputAction.INVENTORY, KeyboardControl.I);
    }
}
