package com.perplexinggames.ironsoul.editor.snap;

import com.badlogic.gdx.math.Vector2;

public class EditorSnapService {
    private boolean enabled = false;
    private float snapSize = 32f;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public float getSnapSize() {
        return snapSize;
    }

    public void setSnapSize(float snapSize) {
        if (snapSize > 0) {
            this.snapSize = snapSize;
        }
    }

    public float snapValue(float value) {
        if (!enabled) {
            return value;
        }
        return Math.round(value / snapSize) * snapSize;
    }

    public Vector2 snapPosition(Vector2 position) {
        if (!enabled) {
            return position;
        }
        position.x = snapValue(position.x);
        position.y = snapValue(position.y);
        return position;
    }
}
