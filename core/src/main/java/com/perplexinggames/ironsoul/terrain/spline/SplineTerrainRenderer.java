package com.perplexinggames.ironsoul.terrain.spline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.perplexinggames.ironsoul.level.RuntimeLevel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplineTerrainRenderer {
    private final Map<String, Texture> textureCache = new HashMap<>();
    private final Texture fallbackTexture;
    private final float sampleSpacing;

    public SplineTerrainRenderer(float sampleSpacing) {
        this.sampleSpacing = sampleSpacing;
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.fallbackTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void renderBackgroundAndMain(SpriteBatch batch, OrthographicCamera camera, RuntimeLevel runtimeLevel) {
        renderByBand(batch, camera, runtimeLevel, -1);
    }

    public void renderMain(SpriteBatch batch, OrthographicCamera camera, RuntimeLevel runtimeLevel) {
        renderByBand(batch, camera, runtimeLevel, 0);
    }

    public void renderForeground(SpriteBatch batch, OrthographicCamera camera, RuntimeLevel runtimeLevel) {
        renderByBand(batch, camera, runtimeLevel, 1);
    }

    public void renderDebug(ShapeRenderer shapeRenderer, OrthographicCamera camera, RuntimeLevel runtimeLevel,
                            String selectedPathId, String selectedPointId, BezierHandleType selectedHandleType) {
        List<SplinePath> paths = runtimeLevel.getSplinePaths();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (SplinePath splinePath : paths) {
            List<SplineSample> samples = SplineSampling.sample(splinePath, sampleSpacing);
            shapeRenderer.setColor(selectedPathId != null && selectedPathId.equals(splinePath.id)
                ? new Color(0.35f, 1f, 0.65f, 1f)
                : new Color(0.2f, 0.8f, 0.95f, 0.9f));
            for (int i = 1; i < samples.size(); i++) {
                Vector2 previous = samples.get(i - 1).getPosition();
                Vector2 current = samples.get(i).getPosition();
                shapeRenderer.line(previous.x, previous.y, current.x, current.y);
            }
            if (selectedPathId != null && selectedPathId.equals(splinePath.id) && splinePath.curveType == SplineCurveType.BEZIER) {
                for (SplineControlPoint point : splinePath.getPoints()) {
                    Vector2 anchor = point.getAnchorPosition();
                    Vector2 inHandle = point.getInHandleWorldPosition();
                    Vector2 outHandle = point.getOutHandleWorldPosition();
                    shapeRenderer.setColor(new Color(1f, 0.6f, 0.2f, 0.75f));
                    shapeRenderer.line(anchor.x, anchor.y, inHandle.x, inHandle.y);
                    shapeRenderer.line(anchor.x, anchor.y, outHandle.x, outHandle.y);
                }
            }
        }
        shapeRenderer.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (SplinePath splinePath : paths) {
            for (SplineControlPoint point : splinePath.getPoints()) {
                boolean selected = selectedPointId != null && selectedPointId.equals(point.id);
                shapeRenderer.setColor(selected ? new Color(1f, 0.45f, 0.2f, 1f) : new Color(1f, 0.9f, 0.25f, 1f));
                shapeRenderer.circle(point.x, point.y, selected ? 7f : 5f, 16);
                if (selectedPathId != null && selectedPathId.equals(splinePath.id) && splinePath.curveType == SplineCurveType.BEZIER) {
                    boolean inSelected = selected && selectedHandleType == BezierHandleType.IN;
                    boolean outSelected = selected && selectedHandleType == BezierHandleType.OUT;
                    Vector2 inHandle = point.getInHandleWorldPosition();
                    Vector2 outHandle = point.getOutHandleWorldPosition();
                    shapeRenderer.setColor(inSelected ? new Color(1f, 0.2f, 0.2f, 1f) : new Color(0.95f, 0.65f, 0.3f, 1f));
                    shapeRenderer.circle(inHandle.x, inHandle.y, inSelected ? 5f : 4f, 12);
                    shapeRenderer.setColor(outSelected ? new Color(1f, 0.2f, 0.2f, 1f) : new Color(0.95f, 0.65f, 0.3f, 1f));
                    shapeRenderer.circle(outHandle.x, outHandle.y, outSelected ? 5f : 4f, 12);
                }
            }
        }
        shapeRenderer.end();
    }

    public void dispose() {
        for (Texture texture : textureCache.values()) {
            texture.dispose();
        }
        textureCache.clear();
        fallbackTexture.dispose();
    }

    private void renderByBand(SpriteBatch batch, OrthographicCamera camera, RuntimeLevel runtimeLevel, int band) {
        if (runtimeLevel == null || runtimeLevel.getSplinePaths().isEmpty()) {
            return;
        }
        Map<String, SplinePath> pathById = new HashMap<>();
        for (SplinePath splinePath : runtimeLevel.getSplinePaths()) {
            pathById.put(splinePath.id, splinePath);
        }

        List<SplineLayer> sortedLayers = new ArrayList<>(runtimeLevel.getSplineLayers());
        sortedLayers.sort(Comparator.comparingInt(layer -> layer.renderDepth));

        Color originalColor = batch.getColor().cpy();
        for (SplineLayer layer : sortedLayers) {
            if (layer == null || !layer.visible) {
                continue;
            }
            if (band < 0 && layer.renderDepth >= 0) {
                continue;
            }
            if (band == 0 && layer.renderDepth != 0) {
                continue;
            }
            if (band > 0 && layer.renderDepth <= 0) {
                continue;
            }

            SplinePath splinePath = pathById.get(layer.parentSplinePathId);
            if (splinePath == null) {
                continue;
            }
            List<SplineSample> samples = SplineSampling.sample(splinePath, sampleSpacing);
            if (samples.size() < 2) {
                continue;
            }
            batch.setColor(layer.getTint());
            renderLayerSegments(batch, camera, layer, samples);
        }
        batch.setColor(originalColor);
    }

    private void renderLayerSegments(SpriteBatch batch, OrthographicCamera camera, SplineLayer layer, List<SplineSample> samples) {
        Texture texture = resolveTexture(layer.spritePath);
        for (int i = 1; i < samples.size(); i++) {
            SplineSample previousSample = samples.get(i - 1);
            SplineSample currentSample = samples.get(i);
            Vector2 start = previousSample.getPosition();
            Vector2 end = currentSample.getPosition();
            Vector2 segment = new Vector2(end).sub(start);
            float length = segment.len();
            if (length <= 0.001f) {
                continue;
            }

            Vector2 tangent = previousSample.getTangent();
            Vector2 normal = previousSample.getNormal();
            Vector2 offset = new Vector2(normal).scl(layer.verticalOffset);
            Vector2 drawStart = applyParallax(new Vector2(start).add(offset), camera, layer.parallaxFactor);
            float rotation = tangent.angleDeg();
            float width = Math.max(4f, layer.visualWidth);

            if (layer.tileMode == SplineTileMode.REPEAT) {
                renderRepeatedSegment(batch, texture, drawStart, tangent, length, width, rotation);
            } else {
                drawSegment(batch, texture, drawStart, length, width, rotation);
            }
        }
    }

    private void renderRepeatedSegment(SpriteBatch batch, Texture texture, Vector2 drawStart, Vector2 tangent,
                                       float length, float width, float rotation) {
        float tileLength = Math.max(8f, texture.getWidth());
        float remaining = length;
        Vector2 cursor = new Vector2(drawStart);
        while (remaining > 0.001f) {
            float drawLength = Math.min(tileLength, remaining);
            drawSegment(batch, texture, cursor, drawLength, width, rotation);
            cursor.mulAdd(tangent, drawLength);
            remaining -= drawLength;
        }
    }

    private void drawSegment(SpriteBatch batch, Texture texture, Vector2 drawStart, float length, float width, float rotation) {
        batch.draw(texture, drawStart.x, drawStart.y - width * 0.5f, 0f, width * 0.5f,
            length, width, 1f, 1f, rotation, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
    }

    private Vector2 applyParallax(Vector2 worldPosition, OrthographicCamera camera, float parallaxFactor) {
        return worldPosition.add(camera.position.x * (1f - parallaxFactor), camera.position.y * (1f - parallaxFactor));
    }

    private Texture resolveTexture(String spritePath) {
        if (spritePath == null || spritePath.isBlank()) {
            return fallbackTexture;
        }
        Texture cached = textureCache.get(spritePath);
        if (cached != null) {
            return cached;
        }
        if (!Gdx.files.internal(spritePath).exists()) {
            return fallbackTexture;
        }
        Texture texture = new Texture(Gdx.files.internal(spritePath));
        textureCache.put(spritePath, texture);
        return texture;
    }
}
