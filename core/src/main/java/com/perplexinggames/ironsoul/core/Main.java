// com.perplexinggames.ironsoul/core/Main.java
package com.perplexinggames.ironsoul.core;

import com.badlogic.gdx.Game;
import com.perplexinggames.ironsoul.screens.LevelEditorDemoScreen;

public class Main extends Game {

    @Override
    public void create() {
        // Запускаем демо для тестирования тайлов
        setScreen(new LevelEditorDemoScreen());
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}
