package com.flocklab.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.flocklab.model.Attractor;
import com.flocklab.model.Obstacle;
import com.flocklab.model.Vec2;
import com.flocklab.sim.World;

/**
 * Handles mouse/touch and keyboard input for the simulation.
 * Supports single-touch interactions and two-finger pinch-to-zoom.
 */
public class InputHandler extends InputAdapter {
    private final World world;
    private final OrthographicCamera camera;
    private final Stage stage;

    // Temporary vectors
    private final Vector3 tempVec  = new Vector3();
    private final Vector2 tempVec2 = new Vector2();

    // Two-finger pinch-to-zoom state
    private final Vector2 pointer0 = new Vector2();
    private final Vector2 pointer1 = new Vector2();
    private boolean pinching = false;
    private float   pinchStartDist = 0f;
    private float   pinchStartZoom = 1f;

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
        // Track pointer positions for pinch detection
        if (pointer == 0) pointer0.set(screenX, screenY);
        if (pointer == 1) {
            pointer1.set(screenX, screenY);
            pinching = true;
            pinchStartDist = pointer0.dst(pointer1);
            pinchStartZoom = camera.zoom;
            return true; // consume: no boid spawn during pinch
        }

        if (isOverUI(screenX, screenY)) return false;

        if (button == Input.Buttons.RIGHT) {
            handleRemoval(screenX, screenY);
            return true;
        }
        if (button != Input.Buttons.LEFT) return false;
        return handleInteraction(screenX, screenY);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer == 1) pinching = false;
        if (pointer == 0 && !Gdx.input.isTouched(1)) pinching = false;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (pointer == 0) pointer0.set(screenX, screenY);
        if (pointer == 1) pointer1.set(screenX, screenY);

        if (pinching && Gdx.input.isTouched(0) && Gdx.input.isTouched(1)) {
            float currentDist = pointer0.dst(pointer1);
            if (pinchStartDist > 0f) {
                camera.zoom = Math.max(0.1f, Math.min(pinchStartZoom * (pinchStartDist / currentDist), 5f));
            }
            return true;
        }

        if (isOverUI(screenX, screenY)) return false;
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) || (pointer == 0 && Gdx.input.isTouched(0))) {
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
                if (!world.getObstacles().isEmpty()) {
                    Obstacle last = world.getObstacles().get(world.getObstacles().size() - 1);
                    if (last.center().distanceTo(worldPos) < 15f) {
                        return false; // Throttle dense obstacle stacking on drag
                    }
                }
                world.addObstacle(new Obstacle(worldPos, 30f));
                break;
            case ATTRACTOR:
                world.addAttractor(new Attractor(worldPos, 100f));
                break;
            case PREDATOR:
                world.spawnPredator(worldPos);
                break;
            case ERASER:
                handleRemoval(screenX, screenY);
                break;
        }
        return true;
    }

    private void handleRemoval(int screenX, int screenY) {
        tempVec.set(screenX, screenY, 0);
        camera.unproject(tempVec);
        Vec2 worldPos = new Vec2(tempVec.x, tempVec.y);

        float radius = 40f;
        if (world.removeObstacleNear(worldPos, radius)) return;
        if (world.removeAttractorNear(worldPos, radius)) return;
        if (world.removePredatorNear(worldPos, radius)) return;
    }

    private void spawnBoid(int screenX, int screenY) {
        tempVec.set(screenX, screenY, 0);
        camera.unproject(tempVec);

        float vx = (float) (Math.random() * 2 - 1);
        float vy = (float) (Math.random() * 2 - 1);
        Vec2 vel = new Vec2(vx, vy).setMagnitude(world.getConfig().maxSpeed * 0.5f);

        world.addBoid(new Vec2(tempVec.x, tempVec.y), vel);
    }
}
