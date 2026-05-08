package com.perplexinggames.ironsoul.tank.controller;

public enum HorizontalDirection {
    LEFT(-1f),
    RIGHT(1f);

    private final float axisValue;

    HorizontalDirection(float axisValue) {
        this.axisValue = axisValue;
    }

    public float axisValue() {
        return axisValue;
    }
}
