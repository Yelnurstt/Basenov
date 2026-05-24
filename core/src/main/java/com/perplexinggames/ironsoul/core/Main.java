// com.perplexinggames.ironsoul/core/Main.java
package com.perplexinggames.ironsoul.core;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.perplexinggames.ironsoul.screens.LevelEditorDemoScreen;
import com.perplexinggames.ironsoul.screens.GameScreen;
import com.perplexinggames.ironsoul.screens.MainMenuScreen;

public class Main extends Game {

    @Override
    public void create() {
        showMainMenu();
    }

    public void showMainMenu() {
        setScreen(new MainMenuScreen(this));
    }

    public void showGame() {
        setScreen(new GameScreen(this));
    }

    public void showWorldEditor() {
        setScreen(new LevelEditorDemoScreen(this));
    }

    public void exitGame() {
        Gdx.app.exit();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}
