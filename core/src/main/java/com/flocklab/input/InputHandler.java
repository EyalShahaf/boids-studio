package com.flocklab.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.flocklab.model.Attractor;
import com.flocklab.model.Obstacle;
import com.flocklab.model.Vec2;
import com.flocklab.sim.World;

/**
 * Handles mouse and keyboard input for interacting with the simulation.
 */
public class InputHandler extends InputAdapter {
    private final World world;
    private final OrthographicCamera camera;

    // Temporary vector for unprojecting mouse coordinates to world coordinates
    private final Vector3 tempVec = new Vector3();

    public InputHandler(World world, OrthographicCamera camera) {
        this.world = world;
        this.camera = camera;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT)
            return false;
        return handleInteraction(screenX, screenY);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Only spawn continuously if left click is held and no modifiers are pressed
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) &&
                !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) &&
                !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            spawnBoid(screenX, screenY);
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

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            // Shift + Click -> Place Obstacle
            world.addObstacle(new Obstacle(worldPos, 30f));
            return true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SYM)) {
            // Ctrl/Cmd + Click -> Place Attractor
            world.addAttractor(new Attractor(worldPos, 100f));
            return true;
        } else {
            // Normal Click -> Spawn Boid
            spawnBoid(screenX, screenY);
            return true;
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
