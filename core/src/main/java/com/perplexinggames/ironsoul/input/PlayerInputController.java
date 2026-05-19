package com.perplexinggames.ironsoul.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.perplexinggames.ironsoul.entities.Player;

public class PlayerInputController {
    private final Player player;

    public PlayerInputController(Player player) {
        this.player = player;
    }

    public void update() {
        boolean moveLeft = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean moveRight = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean jumpPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

        if (!player.isPhysicsStarted()) {
            if (!moveLeft && !moveRight && !jumpPressed) {
                player.stopHorizontalMovement();
                player.setInteracting(false);
                return;
            }
            player.startPhysics();
        }

        player.stopHorizontalMovement();

        if (moveLeft) {
            player.moveLeft();
        }
        if (moveRight) {
            player.moveRight();
        }
        if (jumpPressed) {
            player.jump();
        }

        player.setInteracting(Gdx.input.isKeyJustPressed(Input.Keys.E));
    }
}
