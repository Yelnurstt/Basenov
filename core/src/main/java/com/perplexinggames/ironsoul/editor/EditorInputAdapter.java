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
import com.perplexinggames.ironsoul.editor.command.UpdateSplinePointHandleModeCommand;
import com.perplexinggames.ironsoul.editor.command.DeleteSplinePointCommand;
import com.perplexinggames.ironsoul.editor.tool.EraseBlockTool;
import com.perplexinggames.ironsoul.editor.tool.GateTool;
import com.perplexinggames.ironsoul.editor.tool.PlaceBlockTool;
import com.perplexinggames.ironsoul.editor.tool.SelectBlockTool;
import com.perplexinggames.ironsoul.editor.tool.SplineEditTool;
import com.perplexinggames.ironsoul.editor.tool.SplinePenTool;
import com.perplexinggames.ironsoul.editor.tool.SpawnPointTool;
import com.perplexinggames.ironsoul.editor.tool.WorldMarkerTool;
import com.perplexinggames.ironsoul.terrain.spline.BezierHandleMode;

import java.util.List;

public class EditorInputAdapter extends InputAdapter {
    private static final float CAMERA_PAN_SPEED = 500f;
    private static final float MIN_CAMERA_ZOOM = 0.25f;
    private static final float MAX_CAMERA_ZOOM = 3.5f;
    private static final float ZOOM_STEP = 0.1f;

    private final LevelEditor levelEditor;
    private final OrthographicCamera worldCamera;
    private final Vector3 tempScreenPosition;
    private boolean draggingLeftButton;
    private boolean draggingPanButton;
    private int lastDragGridX;
    private int lastDragGridY;
    private int lastPanScreenX;
    private int lastPanScreenY;

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
            case Input.Keys.NUM_4:
                levelEditor.setTool(new SplinePenTool());
                return true;
            case Input.Keys.Q:
                levelEditor.setTool(new SplineEditTool());
                return true;
            case Input.Keys.NUM_5:
                levelEditor.setTool(new GateTool());
                return true;
            case Input.Keys.NUM_6:
                levelEditor.setTool(new SpawnPointTool());
                return true;
            case Input.Keys.NUM_7:
                levelEditor.setTool(new WorldMarkerTool("OBJECT", LevelEditor.MarkerLayer.OBJECT));
                return true;
            case Input.Keys.NUM_8:
                levelEditor.setTool(new WorldMarkerTool("ENEMY", LevelEditor.MarkerLayer.ENEMY));
                return true;
            case Input.Keys.NUM_9:
                levelEditor.setTool(new WorldMarkerTool("REWARD", LevelEditor.MarkerLayer.REWARD));
                return true;
            case Input.Keys.NUM_0:
                levelEditor.setTool(new WorldMarkerTool("TRIGGER", LevelEditor.MarkerLayer.TRIGGER));
                return true;
            case Input.Keys.G:
                levelEditor.toggleTerrainSnapToGrid();
                return true;
            case Input.Keys.T:
                levelEditor.cycleSelectedGateTransitionType();
                return true;
            case Input.Keys.Y:
                levelEditor.cycleSelectedGateState();
                return true;
            case Input.Keys.U:
                levelEditor.cycleSelectedGateTargetBlock();
                return true;
            case Input.Keys.I:
                levelEditor.cycleSelectedGateTargetSpawnPoint();
                return true;
            case Input.Keys.H:
                if (levelEditor.getSelectedSplinePathId() != null && levelEditor.getSelectedSplinePointId() != null) {
                    BezierHandleMode currentMode = levelEditor.getSelectedSplinePointHandleMode();
                    BezierHandleMode nextMode = switch (currentMode) {
                        case FREE -> BezierHandleMode.MIRRORED;
                        case MIRRORED -> BezierHandleMode.ALIGNED;
                        case ALIGNED -> BezierHandleMode.AUTO;
                        case AUTO -> BezierHandleMode.FREE;
                    };
                    levelEditor.executeCommand(new UpdateSplinePointHandleModeCommand(levelEditor,
                        levelEditor.getSelectedSplinePathId(), levelEditor.getSelectedSplinePointId(), nextMode));
                    return true;
                }
                return false;
            case Input.Keys.TAB:
                cycleBlocks();
                return true;
            case Input.Keys.S:
                levelEditor.executeCommand(new SaveLevelCommand(levelEditor));
                return true;
            case Input.Keys.L:
                levelEditor.executeCommand(new LoadLevelCommand(levelEditor));
                return true;
            case Input.Keys.DEL:
            case Input.Keys.FORWARD_DEL:
                if (levelEditor.getSelectedSplinePathId() != null && levelEditor.getSelectedSplinePointId() != null) {
                    levelEditor.executeCommand(new DeleteSplinePointCommand(levelEditor,
                        levelEditor.getSelectedSplinePathId(), levelEditor.getSelectedSplinePointId()));
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!levelEditor.isEditorMode()) {
            return false;
        }

        if (button == Input.Buttons.MIDDLE || button == Input.Buttons.RIGHT) {
            draggingPanButton = true;
            lastPanScreenX = screenX;
            lastPanScreenY = screenY;
            return true;
        }

        GridPoint2 gridCell = screenToGridCell(screenX, screenY);
        Vector2 worldPosition = screenToWorld(screenX, screenY);
        levelEditor.setHoveredCell(gridCell.x, gridCell.y);
        levelEditor.getToolContext().onMouseDown(levelEditor, gridCell.x, gridCell.y, worldPosition.x, worldPosition.y, button);

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

        if (draggingPanButton) {
            panCameraByDrag(screenX, screenY);
            return true;
        }

        GridPoint2 gridCell = screenToGridCell(screenX, screenY);
        Vector2 worldPosition = screenToWorld(screenX, screenY);
        levelEditor.setHoveredCell(gridCell.x, gridCell.y);

        boolean gridCellChanged = gridCell.x != lastDragGridX || gridCell.y != lastDragGridY;
        if (draggingLeftButton && (levelEditor.getToolContext().usesContinuousWorldDrag() || gridCellChanged)) {
            levelEditor.getToolContext().onMouseDrag(levelEditor, gridCell.x, gridCell.y, worldPosition.x, worldPosition.y);
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

        if (button == Input.Buttons.MIDDLE || button == Input.Buttons.RIGHT) {
            draggingPanButton = false;
            return true;
        }

        GridPoint2 gridCell = screenToGridCell(screenX, screenY);
        Vector2 worldPosition = screenToWorld(screenX, screenY);
        levelEditor.setHoveredCell(gridCell.x, gridCell.y);
        levelEditor.getToolContext().onMouseUp(levelEditor, gridCell.x, gridCell.y, worldPosition.x, worldPosition.y, button);

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
        Vector2 worldPosition = screenToWorld(screenX, screenY);
        levelEditor.setHoveredCell(gridCell.x, gridCell.y);
        levelEditor.getToolContext().onMouseMove(levelEditor, gridCell.x, gridCell.y, worldPosition.x, worldPosition.y);
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (!levelEditor.isEditorMode()) {
            return false;
        }

        Vector2 beforeZoom = screenToWorld(Gdx.input.getX(), Gdx.input.getY());
        float zoomFactor = 1f + (Math.abs(amountY) * ZOOM_STEP);
        if (amountY > 0f) {
            worldCamera.zoom = Math.min(MAX_CAMERA_ZOOM, worldCamera.zoom * zoomFactor);
        } else if (amountY < 0f) {
            worldCamera.zoom = Math.max(MIN_CAMERA_ZOOM, worldCamera.zoom / zoomFactor);
        }
        worldCamera.update();

        Vector2 afterZoom = screenToWorld(Gdx.input.getX(), Gdx.input.getY());
        worldCamera.position.add(beforeZoom.x - afterZoom.x, beforeZoom.y - afterZoom.y, 0f);
        worldCamera.update();
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

    private void cycleBlocks() {
        List<String> blockIds = levelEditor.getBlockIds();
        if (blockIds.isEmpty()) {
            return;
        }
        int currentIndex = Math.max(0, blockIds.indexOf(levelEditor.getActiveBlockId()));
        String nextBlockId = blockIds.get((currentIndex + 1) % blockIds.size());
        levelEditor.selectActiveBlock(nextBlockId);
    }

    private void panCameraByDrag(int screenX, int screenY) {
        float deltaX = (screenX - lastPanScreenX) * worldCamera.zoom;
        float deltaY = (screenY - lastPanScreenY) * worldCamera.zoom;
        worldCamera.position.add(-deltaX, deltaY, 0f);
        worldCamera.update();
        lastPanScreenX = screenX;
        lastPanScreenY = screenY;
    }
}
