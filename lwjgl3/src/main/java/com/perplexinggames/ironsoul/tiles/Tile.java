package com.perplexinggames.ironsoul.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Tile {
    private int id;
    private TextureRegion texture;
    private TileType type;
    private boolean solid;
    private boolean interactive;
    private float damage; // для опасных тайлов (лава, шипы)
    private String interactionMessage;

    public enum TileType {
        GROUND(0, false, false),
        WALL(1, true, false),
        WATER(2, false, true),
        LAVA(3, false, true),
        SPIKE(4, false, true),
        DOOR(5, true, true),
        CHEST(6, false, true),
        LADDER(7, false, false),
        ICE(8, false, false),
        CONVEYOR(9, false, false);

        private int code;
        private boolean solid;
        private boolean interactive;

        TileType(int code, boolean solid, boolean interactive) {
            this.code = code;
            this.solid = solid;
            this.interactive = interactive;
        }

        public int getCode() { return code; }
        public boolean isSolid() { return solid; }
        public boolean isInteractive() { return interactive; }
    }

    public Tile(int id, TextureRegion texture, TileType type) {
        this.id = id;
        this.texture = texture;
        this.type = type;
        this.solid = type.isSolid();
        this.interactive = type.isInteractive();
        this.damage = 0;
        this.interactionMessage = "";

        // Настройка специфических свойств
        switch (type) {
            case LAVA:
                damage = 10;
                break;
            case SPIKE:
                damage = 25;
                break;
            case WATER:
                damage = 0;
                break;
        }
    }

    // Геттеры
    public int getId() { return id; }
    public TextureRegion getTexture() { return texture; }
    public TileType getType() { return type; }
    public boolean isSolid() { return solid; }
    public boolean isInteractive() { return interactive; }
    public float getDamage() { return damage; }
    public String getInteractionMessage() { return interactionMessage; }


    public void setSolid(boolean solid) {
        this.solid = solid;
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }
    // Сеттеры для кастомных свойств
    public void setDamage(float damage) { this.damage = damage; }
    public void setInteractionMessage(String message) { this.interactionMessage = message; }
}
