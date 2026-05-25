package com.perplexinggames.ironsoul.terrain.spline;

public enum BezierHandleMode {
    FREE,
    MIRRORED,
    ALIGNED,
    AUTO;

    public static BezierHandleMode fromString(String rawValue, BezierHandleMode fallback) {
        if (rawValue == null || rawValue.isBlank()) {
            return fallback;
        }
        try {
            return BezierHandleMode.valueOf(rawValue.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }
}
