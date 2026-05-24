package com.perplexinggames.ironsoul.terrain;

import com.badlogic.gdx.math.Vector2;

public class TerrainSegment {
    public final TerrainPoint p1, p2;
    public final Vector2 direction, normal;
    public final float angle;

    public TerrainSegment(TerrainPoint p1, TerrainPoint p2) {
        this.p1 = p1;
        this.p2 = p2;

        // Вектор направления от первой точки ко второй
        this.direction = new Vector2(p2.x - p1.x, p2.y - p1.y).nor();

        // Нормаль - это перпендикуляр к направлению (повернутый на 90 градусов)
        this.normal = new Vector2(-direction.y, direction.x).nor();

        // Угол наклона в градусах
        this.angle = direction.angleDeg();
    }

    // Проверяем, находится ли координата X над этим отрезком
    public boolean containsX(float x) {
        return x >= Math.min(p1.x, p2.x) && x <= Math.max(p1.x, p2.x);
    }

    // Линейная интерполяция (Lerp): находим точную высоту Y на отрезке по координате X
    public float getY(float x) {
        if (p1.x == p2.x) return Math.max(p1.y, p2.y); // Защита от деления на ноль для вертикальных стен
        float t = (x - p1.x) / (p2.x - p1.x);
        return p1.y + t * (p2.y - p1.y);
    }
}
