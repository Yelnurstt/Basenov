package com.perplexinggames.ironsoul.core.gameplay;

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
