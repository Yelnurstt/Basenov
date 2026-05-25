package com.perplexinggames.ironsoul.terrain.spline;

import com.badlogic.gdx.graphics.Color;

public class SplineLayer {
    public String id;
    public String parentSplinePathId;
    public String name;
    public String spritePath;
    public String textureRegionName;
    public int renderDepth;
    public float parallaxFactor;
    public float verticalOffset;
    public float visualWidth;
    public SplineTileMode tileMode;
    public float tintR;
    public float tintG;
    public float tintB;
    public float tintA;
    public boolean visible;
    public boolean collisionEnabled;

    public SplineLayer() {
        this("", "", "Layer", null, null, 0, 1f, 0f, 32f, SplineTileMode.STRETCH, Color.WHITE, true, false);
    }

    public SplineLayer(String id, String parentSplinePathId, String name, String spritePath, String textureRegionName,
                       int renderDepth, float parallaxFactor, float verticalOffset, float visualWidth,
                       SplineTileMode tileMode, Color tint, boolean visible, boolean collisionEnabled) {
        this.id = id;
        this.parentSplinePathId = parentSplinePathId;
        this.name = name;
        this.spritePath = spritePath;
        this.textureRegionName = textureRegionName;
        this.renderDepth = renderDepth;
        this.parallaxFactor = parallaxFactor;
        this.verticalOffset = verticalOffset;
        this.visualWidth = visualWidth;
        this.tileMode = tileMode;
        setTint(tint == null ? Color.WHITE : tint);
        this.visible = visible;
        this.collisionEnabled = collisionEnabled;
    }

    public Color getTint() {
        return new Color(tintR, tintG, tintB, tintA);
    }

    public void setTint(Color tint) {
        Color safeTint = tint == null ? Color.WHITE : tint;
        this.tintR = safeTint.r;
        this.tintG = safeTint.g;
        this.tintB = safeTint.b;
        this.tintA = safeTint.a;
    }

    public SplineLayer copy() {
        return new SplineLayer(id, parentSplinePathId, name, spritePath, textureRegionName, renderDepth,
            parallaxFactor, verticalOffset, visualWidth, tileMode, getTint(), visible, collisionEnabled);
    }
}
