package com.perplexinggames.ironsoul.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.perplexinggames.ironsoul.editor.command.LoadLevelCommand;
import com.perplexinggames.ironsoul.editor.command.SaveLevelCommand;
import com.perplexinggames.ironsoul.editor.tool.EraseBlockTool;
import com.perplexinggames.ironsoul.editor.tool.PlaceBlockTool;
import com.perplexinggames.ironsoul.editor.tool.SelectBlockTool;

public class EditorInputAdapter extends InputAdapter {
    private static final float CAMERA_PAN_SPEED = 500f;

    private final LevelEditor levelEditor;
    private final OrthographicCamera worldCamera;
    private final Vector3 tempScreenPosition;
    private boolean draggingLeftButton;
    private int lastDragGridX;
    private int lastDragGridY;

    public EditorInputAdapter(LevelEditor levelEditor, OrthographicCamera worldCamera) {
        this.levelEditor = levelEditor;
        this.worldCamera = worldCamera;
        this.tempScreenPosition = new Vector3();
        this.lastDragGridX = Integer.MIN_VALUE;
        this.lastDragGridY = Integer.MIN_VALUE;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.F1) {
            levelEditor.setMode(EditorMode.GAMEPLAY);
            return true;
        }
        if (keycode == Input.Keys.F2) {
            levelEditor.setMode(EditorMode.EDITOR);
            return true;
        }
        if (!levelEditor.isEditorMode()) {
            return false;
        }

        if (UIUtils.ctrl() && keycode == Input.Keys.Z) {
            levelEditor.undo();
            return true;
        }
        if (UIUtils.ctrl() && keycode == Input.Keys.Y) {
            levelEditor.redo();
            return true;
        }

        switch (keycode) {
            case Input.Keys.NUM_1:
                levelEditor.setTool(new PlaceBlockTool());
                return true;
            case Input.Keys.NUM_2:
                levelEditor.setTool(new EraseBlockTool());
                return true;
            case Input.Keys.NUM_3:
                levelEditor.setTool(new SelectBlockTool());
                return true;
            case Input.Keys.S:
                levelEditor.executeCommand(new SaveLevelCommand(levelEditor));
                return true;
            case Input.Keys.L:
                levelEditor.executeCommand(new LoadLevelCommand(levelEditor));
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!levelEditor.isEditorMode()) {
            return false;
        }

        GridPoint2 gridCell = screenToGridCell(screenX, screenY);
        levelEditor.setHoveredCell(gridCell.x, gridCell.y);
        levelEditor.getToolContext().onMouseDown(levelEditor, gridCell.x, gridCell.y, button);

        if (button == Input.Buttons.LEFT) {
            draggingLeftButton = true;
            lastDragGridX = gridCell.x;
            lastDragGridY = gridCell.y;
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!levelEditor.isEditorMode()) {
            return false;
        }

        GridPoint2 gridCell = screenToGridCell(screenX, screenY);
        levelEditor.setHoveredCell(gridCell.x, gridCell.y);

        if (draggingLeftButton && (gridCell.x != lastDragGridX || gridCell.y != lastDragGridY)) {
            levelEditor.getToolContext().onMouseDrag(levelEditor, gridCell.x, gridCell.y);
            lastDragGridX = gridCell.x;
            lastDragGridY = gridCell.y;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (!levelEditor.isEditorMode()) {
            return false;
        }

        GridPoint2 gridCell = screenToGridCell(screenX, screenY);
        levelEditor.setHoveredCell(gridCell.x, gridCell.y);
        levelEditor.getToolContext().onMouseUp(levelEditor, gridCell.x, gridCell.y, button);

        if (button == Input.Buttons.LEFT) {
            draggingLeftButton = false;
            lastDragGridX = Integer.MIN_VALUE;
            lastDragGridY = Integer.MIN_VALUE;
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (!levelEditor.isEditorMode()) {
            return false;
        }

        GridPoint2 gridCell = screenToGridCell(screenX, screenY);
        levelEditor.setHoveredCell(gridCell.x, gridCell.y);
        levelEditor.getToolContext().onMouseMove(levelEditor, gridCell.x, gridCell.y);
        return true;
    }

    public void update(float delta) {
        if (!levelEditor.isEditorMode()) {
            return;
        }

        float moveX = 0f;
        float moveY = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX -= CAMERA_PAN_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX += CAMERA_PAN_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveY -= CAMERA_PAN_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveY += CAMERA_PAN_SPEED * delta;
        }

        if (moveX != 0f || moveY != 0f) {
            worldCamera.position.add(moveX, moveY, 0f);
        }
    }

    public Vector2 screenToWorld(int screenX, int screenY) {
        tempScreenPosition.set(screenX, screenY, 0f);
        worldCamera.unproject(tempScreenPosition);
        return new Vector2(tempScreenPosition.x, tempScreenPosition.y);
    }

    public GridPoint2 screenToGridCell(int screenX, int screenY) {
        Vector2 worldPosition = screenToWorld(screenX, screenY);
        int tileSize = levelEditor.getRuntimeLevel().getTileSize();
        return new GridPoint2((int) Math.floor(worldPosition.x / tileSize), (int) Math.floor(worldPosition.y / tileSize));
    }
}
