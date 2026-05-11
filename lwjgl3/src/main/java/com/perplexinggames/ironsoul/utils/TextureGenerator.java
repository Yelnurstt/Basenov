// core/src/com/perplexinggames/ironsoul/utils/TextureGenerator.java
package com.perplexinggames.ironsoul.utils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class TextureGenerator {

    // Создание простой текстуры с цветом
    public static Texture createColoredTexture(int width, int height, int color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    // Создание текстуры для тайлов с границей
    public static Texture createTileTexture(int size, int color, int borderColor) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        // Заливка основным цветом
        pixmap.setColor(color);
        pixmap.fill();

        // Рисуем границу
        pixmap.setColor(borderColor);
        for (int i = 0; i < 2; i++) {
            pixmap.drawRectangle(i, i, size - i * 2, size - i * 2);
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
