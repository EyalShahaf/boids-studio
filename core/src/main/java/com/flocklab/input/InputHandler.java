package com.flocklab.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.flocklab.model.Attractor;
import com.flocklab.model.Obstacle;
import com.flocklab.model.Predator;
import com.flocklab.model.Vec2;
import com.flocklab.sim.World;

/**
 * Handles mouse and keyboard input for interacting with the simulation.
 */
public class InputHandler extends InputAdapter {
    private final World world;
    private final OrthographicCamera camera;
    private final Stage stage;

    // Temporary vector for unprojecting mouse coordinates to world coordinates
    private final Vector3 tempVec = new Vector3();

    public InputHandler(World world, OrthographicCamera camera, Stage stage) {
        this.world = world;
        this.camera = camera;
        this.stage = stage;
    }

    private boolean isOverUI(int screenX, int screenY) {
        // Scene2D coordinates are y-down for hit detection, but stage uses internal
        // viewport logic
        return stage.hit(screenX, Gdx.graphics.getHeight() - screenY, true) != null;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isOverUI(screenX, screenY))
            return false;

        if (button == Input.Buttons.RIGHT) {
            handleRightClick(screenX, screenY);
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
                world.addPredator(new Predator(worldPos, Vec2.ZERO, world.getConfig().maxSpeed * 1.5f));
                break;
        }
        return true;
    }

    private void handleRightClick(int screenX, int screenY) {
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
}
