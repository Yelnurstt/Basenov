package com.perplexinggames.ironsoul.input.command;

import com.perplexinggames.ironsoul.input.binding.InputAction;
import com.perplexinggames.ironsoul.input.binding.InputPhase;
import com.perplexinggames.ironsoul.input.command.commands.AimDownCommand;
import com.perplexinggames.ironsoul.input.command.commands.AimUpCommand;
import com.perplexinggames.ironsoul.input.command.commands.CrouchCommand;
import com.perplexinggames.ironsoul.input.command.commands.DashCommand;
import com.perplexinggames.ironsoul.input.command.commands.InteractCommand;
import com.perplexinggames.ironsoul.input.command.commands.JumpCommand;
import com.perplexinggames.ironsoul.input.command.commands.MeleeRamCommand;
import com.perplexinggames.ironsoul.input.command.commands.MoveLeftCommand;
import com.perplexinggames.ironsoul.input.command.commands.MoveRightCommand;
import com.perplexinggames.ironsoul.input.command.commands.PrimaryFireCommand;
import com.perplexinggames.ironsoul.input.command.commands.ReleaseChargeCommand;
import com.perplexinggames.ironsoul.input.command.commands.ResetAimCommand;
import com.perplexinggames.ironsoul.input.command.commands.SecondaryFireCommand;
import com.perplexinggames.ironsoul.input.command.commands.ShieldCommand;
import com.perplexinggames.ironsoul.input.command.commands.StartChargeCommand;
import com.perplexinggames.ironsoul.input.command.commands.StopMoveCommand;
import com.perplexinggames.ironsoul.input.command.commands.ToggleInventoryCommand;
import com.perplexinggames.ironsoul.input.command.commands.ToggleMapCommand;
import com.perplexinggames.ironsoul.input.command.commands.TogglePauseCommand;
import com.perplexinggames.ironsoul.tank.controller.AimDirection;
import com.perplexinggames.ironsoul.tank.controller.HorizontalDirection;

public final class DefaultCommandMapFactory {
    private DefaultCommandMapFactory() {
    }

    public static ActionCommandMap createDefault() {
        return new ActionCommandMap()
            .register(InputAction.MOVE_LEFT, new ActionCommandBinding()
                .on(InputPhase.START, context -> new MoveLeftCommand(context.getTankController()))
                .on(InputPhase.END, context -> new StopMoveCommand(context.getTankController(), HorizontalDirection.LEFT)))
            .register(InputAction.MOVE_RIGHT, new ActionCommandBinding()
                .on(InputPhase.START, context -> new MoveRightCommand(context.getTankController()))
                .on(InputPhase.END, context -> new StopMoveCommand(context.getTankController(), HorizontalDirection.RIGHT)))
            .register(InputAction.JUMP, new ActionCommandBinding()
                .on(InputPhase.START, context -> new JumpCommand(context.getTankController())))
            .register(InputAction.DASH, new ActionCommandBinding()
                .on(InputPhase.START, context -> new DashCommand(context.getTankController())))
            .register(InputAction.CROUCH, new ActionCommandBinding()
                .on(InputPhase.START, context -> new CrouchCommand(context.getTankController(), true))
                .on(InputPhase.END, context -> new CrouchCommand(context.getTankController(), false)))
            .register(InputAction.AIM_UP, new ActionCommandBinding()
                .on(InputPhase.START, context -> new AimUpCommand(context.getWeaponSystem(), AimDirection.UP))
                .on(InputPhase.END, context -> new ResetAimCommand(context.getWeaponSystem())))
            .register(InputAction.AIM_DOWN, new ActionCommandBinding()
                .on(InputPhase.START, context -> new AimDownCommand(context.getWeaponSystem(), AimDirection.DOWN))
                .on(InputPhase.END, context -> new ResetAimCommand(context.getWeaponSystem())))
            .register(InputAction.PRIMARY_FIRE, new ActionCommandBinding()
                .on(InputPhase.START, context -> new PrimaryFireCommand(context.getWeaponSystem())))
            .register(InputAction.SECONDARY_FIRE, new ActionCommandBinding()
                .on(InputPhase.START, context -> new SecondaryFireCommand(context.getWeaponSystem())))
            .register(InputAction.CHARGE_ATTACK, new ActionCommandBinding()
                .on(InputPhase.START, context -> new StartChargeCommand(context.getWeaponSystem()))
                .on(InputPhase.END, context -> new ReleaseChargeCommand(context.getWeaponSystem())))
            .register(InputAction.MELEE_RAM, new ActionCommandBinding()
                .on(InputPhase.START, context -> new MeleeRamCommand(context.getTankController())))
            .register(InputAction.SHIELD, new ActionCommandBinding()
                .on(InputPhase.START, context -> new ShieldCommand(context.getShieldSystem(), true))
                .on(InputPhase.END, context -> new ShieldCommand(context.getShieldSystem(), false)))
            .register(InputAction.INTERACT, new ActionCommandBinding()
                .on(InputPhase.START, context -> new InteractCommand(context.getInteractionSystem())))
            .register(InputAction.MAP, new ActionCommandBinding()
                .on(InputPhase.START, context -> new ToggleMapCommand(context.getOverlayController())))
            .register(InputAction.PAUSE, new ActionCommandBinding()
                .on(InputPhase.START, context -> new TogglePauseCommand(context.getGameStateManager())))
            .register(InputAction.INVENTORY, new ActionCommandBinding()
                .on(InputPhase.START, context -> new ToggleInventoryCommand(context.getOverlayController())));
    }
}
