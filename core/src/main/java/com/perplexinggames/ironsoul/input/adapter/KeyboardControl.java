package com.perplexinggames.ironsoul.input.adapter;

public enum KeyboardControl {
    A,
    D,
    LEFT,
    RIGHT,
    W,
    S,
    SPACE,
    SHIFT_LEFT,
    SHIFT_RIGHT,
    CONTROL_LEFT,
    Q,
    E,
    F,
    I,
    J,
    K,
    L,
    M,
    MOUSE_LEFT,
    ESCAPE,
    ENTER;

    public String getLabel() {
        return switch (this) {
            case A -> "A";
            case D -> "D";
            case LEFT -> "Left";
            case RIGHT -> "Right";
            case W -> "W";
            case S -> "S";
            case SPACE -> "Space";
            case SHIFT_LEFT, SHIFT_RIGHT -> "Shift";
            case CONTROL_LEFT -> "Left Ctrl";
            case Q -> "Q";
            case E -> "E";
            case F -> "F";
            case I -> "I";
            case J -> "J";
            case K -> "K";
            case L -> "L";
            case M -> "M";
            case MOUSE_LEFT -> "Mouse Left";
            case ESCAPE -> "Esc";
            case ENTER -> "Enter";
        };
    }
}
