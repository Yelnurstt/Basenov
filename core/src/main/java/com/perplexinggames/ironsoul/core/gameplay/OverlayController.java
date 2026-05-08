package com.perplexinggames.ironsoul.core.gameplay;

import com.perplexinggames.ironsoul.core.gamestate.GameStateManager;
import com.perplexinggames.ironsoul.core.gamestate.GameStateType;

public class OverlayController {
    private boolean mapOpen;
    private boolean inventoryOpen;
    private String lastOverlayAction = "None";

    public void toggleMap() {
        mapOpen = !mapOpen;
        if (mapOpen) {
            inventoryOpen = false;
        }
        lastOverlayAction = mapOpen ? "Map opened" : "Map closed";
    }

    public void toggleInventory() {
        inventoryOpen = !inventoryOpen;
        if (inventoryOpen) {
            mapOpen = false;
        }
        lastOverlayAction = inventoryOpen ? "Inventory opened" : "Inventory closed";
    }

    public void toggleMap(GameStateManager gameStateManager) {
        toggleMap();
        gameStateManager.toggleState(
            GameStateType.MAP_OPEN,
            GameStateType.PLAYING
        );
    }

    public void toggleInventory(GameStateManager gameStateManager) {
        toggleInventory();
        gameStateManager.toggleState(
            GameStateType.INVENTORY_OPEN,
            GameStateType.PLAYING
        );
    }

    public boolean isMapOpen() {
        return mapOpen;
    }

    public boolean isInventoryOpen() {
        return inventoryOpen;
    }

    public String getLastOverlayAction() {
        return lastOverlayAction;
    }
}
