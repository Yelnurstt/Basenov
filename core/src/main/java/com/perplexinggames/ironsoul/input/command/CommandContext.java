package com.perplexinggames.ironsoul.input.command;

import com.perplexinggames.ironsoul.core.gameplay.DialogueSystem;
import com.perplexinggames.ironsoul.core.gameplay.InteractionSystem;
import com.perplexinggames.ironsoul.core.gameplay.OverlayController;
import com.perplexinggames.ironsoul.core.gamestate.GameStateManager;
import com.perplexinggames.ironsoul.tank.combat.ShieldSystem;
import com.perplexinggames.ironsoul.tank.combat.WeaponSystem;
import com.perplexinggames.ironsoul.tank.controller.TankController;

public class CommandContext {
    private final TankController tankController;
    private final WeaponSystem weaponSystem;
    private final ShieldSystem shieldSystem;
    private final InteractionSystem interactionSystem;
    private final OverlayController overlayController;
    private final DialogueSystem dialogueSystem;
    private final GameStateManager gameStateManager;

    public CommandContext(
        TankController tankController,
        WeaponSystem weaponSystem,
        ShieldSystem shieldSystem,
        InteractionSystem interactionSystem,
        OverlayController overlayController,
        DialogueSystem dialogueSystem,
        GameStateManager gameStateManager
    ) {
        this.tankController = tankController;
        this.weaponSystem = weaponSystem;
        this.shieldSystem = shieldSystem;
        this.interactionSystem = interactionSystem;
        this.overlayController = overlayController;
        this.dialogueSystem = dialogueSystem;
        this.gameStateManager = gameStateManager;
    }

    public TankController getTankController() {
        return tankController;
    }

    public WeaponSystem getWeaponSystem() {
        return weaponSystem;
    }

    public ShieldSystem getShieldSystem() {
        return shieldSystem;
    }

    public InteractionSystem getInteractionSystem() {
        return interactionSystem;
    }

    public OverlayController getOverlayController() {
        return overlayController;
    }

    public DialogueSystem getDialogueSystem() {
        return dialogueSystem;
    }

    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }
}
