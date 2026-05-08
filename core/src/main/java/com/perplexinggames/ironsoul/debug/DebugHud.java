package com.perplexinggames.ironsoul.debug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perplexinggames.ironsoul.core.gameplay.InteractionSystem;
import com.perplexinggames.ironsoul.core.gameplay.OverlayController;
import com.perplexinggames.ironsoul.core.gamestate.GameStateManager;
import com.perplexinggames.ironsoul.input.adapter.KeyboardControl;
import com.perplexinggames.ironsoul.input.binding.ActionBindingProfile;
import com.perplexinggames.ironsoul.input.binding.InputAction;
import com.perplexinggames.ironsoul.input.strategy.GameInputCoordinator;
import com.perplexinggames.ironsoul.projectile.ProjectileSystem;
import com.perplexinggames.ironsoul.tank.combat.WeaponSystem;
import com.perplexinggames.ironsoul.tank.controller.TankController;
import com.perplexinggames.ironsoul.tank.controller.TankModel;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DebugHud {
    private final BitmapFont font;
    private final ActionBindingProfile<KeyboardControl> bindings;

    public DebugHud(BitmapFont font, ActionBindingProfile<KeyboardControl> bindings) {
        this.font = font;
        this.bindings = bindings;
    }

    public void render(
        SpriteBatch batch,
        float worldWidth,
        float worldHeight,
        GameInputCoordinator inputCoordinator,
        GameStateManager gameStateManager,
        TankController tankController,
        WeaponSystem weaponSystem,
        ProjectileSystem projectileSystem,
        InteractionSystem interactionSystem,
        OverlayController overlayController
    ) {
        batch.begin();
        font.setColor(Color.WHITE);

        float x = 0.7f;
        float y = worldHeight - 0.7f;
        float line = 0.95f;
        TankModel model = tankController.getModel();

        draw(batch, "Control Demo Screen", x, y);
        draw(batch, "Input strategy: " + inputCoordinator.getActiveStrategyName(), x, y - line);
        draw(batch, "Global state: " + gameStateManager.getCurrentState().getName(), x, y - line * 2f);
        draw(batch, "Tank state: " + formatTankState(tankController), x, y - line * 3f);
        draw(batch, "Velocity: " + String.format(Locale.US, "x=%.2f y=%.2f", model.getVelocityX(), model.getVelocityY()), x, y - line * 4f);
        draw(batch, "Grounded: " + tankController.isGrounded() + " | Facing: " + tankController.getFacingDirection(), x, y - line * 5f);
        draw(batch, "Last command: " + inputCoordinator.getLastExecutedCommand(), x, y - line * 6f);
        draw(batch, "Last tank action: " + tankController.getLastAction(), x, y - line * 7f);
        draw(batch, "Weapon: " + weaponSystem.getLastCombatAction(), x, y - line * 8f);
        draw(batch, "Charging: " + weaponSystem.isCharging() + " (" + String.format(Locale.US, "%.2fs", weaponSystem.getChargeTime()) + ")", x, y - line * 9f);
        draw(batch, "Projectiles: " + projectileSystem.getActiveProjectileCount(), x, y - line * 10f);
        draw(batch, "Interact: " + interactionSystem.getLastInteraction(), x, y - line * 11f);
        draw(batch, "Overlay: " + overlayController.getLastOverlayAction(), x, y - line * 12f);

        draw(batch, formatBindingLine("Move Left", InputAction.MOVE_LEFT), x, y - line * 14f);
        draw(batch, formatBindingLine("Move Right", InputAction.MOVE_RIGHT), x, y - line * 15f);
        draw(batch, formatBindingLine("Jump", InputAction.JUMP), x, y - line * 16f);
        draw(batch, formatBindingLine("Dash", InputAction.DASH), x, y - line * 17f);
        draw(batch, formatBindingLine("Primary Fire", InputAction.PRIMARY_FIRE), x, y - line * 18f);
        draw(batch, formatBindingLine("Secondary", InputAction.SECONDARY_FIRE), x, y - line * 19f);
        draw(batch, formatBindingLine("Charge", InputAction.CHARGE_ATTACK), x, y - line * 20f);
        draw(batch, formatBindingLine("Interact", InputAction.INTERACT), x, y - line * 21f);
        draw(batch, formatBindingLine("Map", InputAction.MAP), x, y - line * 22f);
        draw(batch, formatBindingLine("Inventory", InputAction.INVENTORY), x, y - line * 23f);
        draw(batch, formatBindingLine("Pause", InputAction.PAUSE), x, y - line * 24f);

        if (gameStateManager.isPaused()) {
            font.setColor(new Color(1f, 0.86f, 0.55f, 1f));
            draw(batch, "PAUSED: movement and combat commands are blocked", worldWidth * 0.38f, 2.8f);
        } else if (overlayController.isMapOpen()) {
            font.setColor(new Color(0.72f, 0.89f, 1f, 1f));
            draw(batch, "MAP OVERLAY ACTIVE", worldWidth * 0.42f, 2.8f);
        } else if (overlayController.isInventoryOpen()) {
            font.setColor(new Color(0.76f, 0.96f, 0.78f, 1f));
            draw(batch, "INVENTORY OVERLAY ACTIVE", worldWidth * 0.38f, 2.8f);
        }

        batch.end();
    }

    private void draw(SpriteBatch batch, String text, float x, float y) {
        font.draw(batch, text, x, y);
    }

    private String formatTankState(TankController tankController) {
        return switch (tankController.getCurrentState().getId()) {
            case GROUNDED -> "GROUNDED";
            case AIRBORNE -> "AIRBORNE";
            case DASH -> "DASHING";
            case WALL_SLIDE -> "WALL_SLIDE";
            case STUNNED -> "STUNNED";
        };
    }

    private String formatBindingLine(String label, InputAction action) {
        return label + ": " + formatControls(bindings.snapshotBindings().get(action));
    }

    private String formatControls(Set<KeyboardControl> controls) {
        if (controls == null || controls.isEmpty()) {
            return "Unbound";
        }
        return controls.stream()
            .map(KeyboardControl::getLabel)
            .sorted()
            .collect(Collectors.joining(" / "));
    }
}
