package com.flocklab.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.flocklab.model.Attractor;
import com.flocklab.model.Obstacle;
import com.flocklab.model.Vec2;
import com.flocklab.sim.World;

/**
 * Handles mouse and keyboard input for interacting with the simulation.
 */
public class InputHandler extends InputAdapter implements GestureDetector.GestureListener {
    private final World world;
    private final OrthographicCamera camera;
    private final Stage stage;

    // Temporary vectors for unprojecting coordinates
    private final Vector3 tempVec = new Vector3();
    private final Vector2 tempVec2 = new Vector2();

    public InputHandler(World world, OrthographicCamera camera, Stage stage) {
        this.world = world;
        this.camera = camera;
        this.stage = stage;
    }

    private boolean isOverUI(int screenX, int screenY) {
        tempVec2.set(screenX, screenY);
        stage.screenToStageCoordinates(tempVec2);
        return stage.hit(tempVec2.x, tempVec2.y, true) != null;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isOverUI(screenX, screenY))
            return false;

        if (button == Input.Buttons.RIGHT) {
            handleRemoval(screenX, screenY);
            return true;
        }
        if (button != Input.Buttons.LEFT)
            return false;

        return handleInteraction(screenX, screenY);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (isOverUI(screenX, screenY))
            return false;

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            handleInteraction(screenX, screenY);
            return true;
        }
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        // Zooming
        camera.zoom += amountY * 0.1f;
        camera.zoom = Math.max(0.1f, Math.min(camera.zoom, 5f));
        return true;
    }

    private boolean handleInteraction(int screenX, int screenY) {
        tempVec.set(screenX, screenY, 0);
        camera.unproject(tempVec);
        Vec2 worldPos = new Vec2(tempVec.x, tempVec.y);

        World.CursorMode mode = world.getCursorMode();

        switch (mode) {
            case BOID:
                spawnBoid(screenX, screenY);
                break;
            case OBSTACLE:
                if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT))
                    return false; // Only once per click
                world.addObstacle(new Obstacle(worldPos, 30f));
                break;
            case ATTRACTOR:
                world.addAttractor(new Attractor(worldPos, 100f));
                break;
            case PREDATOR:
                world.spawnPredator(worldPos);
                break;
        }
        return true;
    }

    private void handleRemoval(int screenX, int screenY) {
        tempVec.set(screenX, screenY, 0);
        camera.unproject(tempVec);
        Vec2 worldPos = new Vec2(tempVec.x, tempVec.y);

        // Remove obstacle/attractor/predator near if clicked
        if (!world.removeObstacleNear(worldPos, 30f)) {
            // Future: could also remove attractors/predators
        }
    }

    private void spawnBoid(int screenX, int screenY) {
        tempVec.set(screenX, screenY, 0);
        camera.unproject(tempVec);

        // Random slight velocity jump to spread them out
        float vx = (float) (Math.random() * 2 - 1);
        float vy = (float) (Math.random() * 2 - 1);
        Vec2 vel = new Vec2(vx, vy).setMagnitude(world.getConfig().maxSpeed * 0.5f);

        world.addBoid(new Vec2(tempVec.x, tempVec.y), vel);
    }

    // --- GestureListener Implementation ---
    
    private float initialZoom = 1f;

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        initialZoom = camera.zoom;
        return false; // Let InputAdapter handle actual interactions
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        if (isOverUI((int)x, (int)y)) return false;
        handleRemoval((int)x, (int)y);
        return true;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        float ratio = initialDistance / distance;
        camera.zoom = Math.max(0.1f, Math.min(initialZoom * ratio, 5f));
        return true;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {
    }
}
