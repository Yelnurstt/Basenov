package com.perplexinggames.ironsoul.gameplay;

import com.perplexinggames.ironsoul.entities.Player;
import com.perplexinggames.ironsoul.input.PlayerInputController;
import com.perplexinggames.ironsoul.level.RuntimeLevel;
import com.perplexinggames.ironsoul.physics.BasicPhysicsController;

public class GameplayController {
    private final RuntimeLevel runtimeLevel;
    private final Player player;
    private final PlayerInputController playerInputController;
    private final BasicPhysicsController basicPhysicsController;

    public GameplayController(RuntimeLevel runtimeLevel, Player player,
                              PlayerInputController playerInputController,
                              BasicPhysicsController basicPhysicsController) {
        this.runtimeLevel = runtimeLevel;
        this.player = player;
        this.playerInputController = playerInputController;
        this.basicPhysicsController = basicPhysicsController;
    }

    public void update(float delta) {
        playerInputController.update();
        basicPhysicsController.update(delta);
    }

    public RuntimeLevel getRuntimeLevel() {
        return runtimeLevel;
    }

    public Player getPlayer() {
        return player;
    }
}
