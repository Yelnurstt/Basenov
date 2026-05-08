package com.perplexinggames.ironsoul.tank.controller;

public enum FacingDirection {
    LEFT(-1f),
    RIGHT(1f);

    private final float sign;

    FacingDirection(float sign) {
        this.sign = sign;
    }

    public float sign() {
        return sign;
    }
}
