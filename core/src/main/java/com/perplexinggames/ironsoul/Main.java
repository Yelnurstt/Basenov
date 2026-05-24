package com.perplexinggames.ironsoul;

import com.badlogic.gdx.Game;
import com.perplexinggames.ironsoul.screens.TerrainDemoScreen; // Убедись, что название экрана совпадает с твоим

/** * Основной класс игры, общий для всех платформ.
 * Управляет жизненным циклом экранов.
 */
public class Main extends Game {

    @Override
    public void create() {
        // Устанавливаем наш новый экран для тестирования физики танка
        setScreen(new TerrainDemoScreen());
    }

    @Override
    public void dispose() {
        // Вызов super.dispose() важен, так как внутри Game он вызывает screen.hide()
        super.dispose();

        // Дополнительно страхуемся и очищаем ресурсы текущего экрана
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}
