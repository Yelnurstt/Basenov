package com.perplexinggames.ironsoul.terrain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class TerrainDebugRenderer {
    public void render(ShapeRenderer shapeRenderer, TerrainPath terrainPath, TerrainCollisionData collisionData) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.2f, 0.22f, 0.26f, 1f));
        shapeRenderer.rect(0f, 0f, 2000f, 24f);

        shapeRenderer.setColor(new Color(0.41f, 0.82f, 0.54f, 1f));
        for (TerrainSegment segment : collisionData.getSegments()) {
            shapeRenderer.rectLine(
                segment.getStartPoint().getX(),
                segment.getStartPoint().getY(),
                segment.getEndPoint().getX(),
                segment.getEndPoint().getY(),
                collisionData.getDebugWidth()
            );
        }

        shapeRenderer.setColor(new Color(0.98f, 0.75f, 0.2f, 1f));
        for (TerrainPoint point : terrainPath.getPoints()) {
            shapeRenderer.circle(point.getX(), point.getY(), 6f, 16);
        }
        shapeRenderer.end();
    }
}
