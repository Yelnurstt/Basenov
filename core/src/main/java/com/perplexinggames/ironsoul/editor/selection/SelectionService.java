package com.perplexinggames.ironsoul.editor.selection;

import com.badlogic.gdx.utils.Array;

public class SelectionService {
    private final Array<String> selectedIds = new Array<>();

    public void select(String id) {
        selectedIds.clear();
        if (id != null) {
            selectedIds.add(id);
        }
    }

    public void addToSelection(String id) {
        if (id != null && !selectedIds.contains(id, false)) {
            selectedIds.add(id);
        }
    }

    public void removeFromSelection(String id) {
        if (id != null) {
            selectedIds.removeValue(id, false);
        }
    }

    public void clearSelection() {
        selectedIds.clear();
    }

    public Array<String> getSelectedIds() {
        return selectedIds;
    }

    public String getPrimarySelected() {
        return selectedIds.isEmpty() ? null : selectedIds.first();
    }

    public boolean isSelected(String id) {
        return id != null && selectedIds.contains(id, false);
    }
}
