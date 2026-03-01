package com.flocklab;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.flocklab.config.SimulationConfig;
import com.flocklab.input.InputHandler;
import com.flocklab.render.WorldRenderer;
import com.flocklab.sim.World;

/**
 * Main game entry point.
 */
public class FlockLabGame extends ApplicationAdapter {

    private World world;
    private WorldRenderer worldRenderer;
    private OrthographicCamera camera;

    private boolean isPaused = false;

    @Override
    public void create() {
        SimulationConfig config = new SimulationConfig();
        // The desktop launcher requests 1280x720, so we use that as logical size
        config.worldWidth = 1280f;
        config.worldHeight = 720f;

        world = new World(config);

        // Setup camera centered on world
        camera = new OrthographicCamera();
        camera.setToOrtho(false, config.worldWidth, config.worldHeight);

        worldRenderer = new WorldRenderer(world, camera);

        // Setup input processor
        Gdx.input.setInputProcessor(new InputHandler(world, camera));

        Gdx.app.log("FlockLab", "Initialized!");
    }

    @Override
    public void render() {
        // 1. Logic Update
        if (!isPaused) {
            float deltaTime = Gdx.graphics.getDeltaTime();
            // Cap delta time to prevent physics explosions during lag spikes
            if (deltaTime > 0.1f)
                deltaTime = 0.1f;
            world.update(deltaTime);
        }

        // 2. Rendering
        // Clear background entirely (WorldRenderer draws the gradient over this)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        worldRenderer.render();
    }

    @Override
    public void resize(int width, int height) {
        // Keep logical bounds fixed but allow zooming/resizing window to show more/less
        // In this MVP, we just stretch the viewport. We will improve it if needed.
        camera.setToOrtho(false, world.getConfig().worldWidth, world.getConfig().worldHeight);
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    public boolean isPaused() {
        return isPaused;
    }

    @Override
    public void dispose() {
        worldRenderer.dispose();
    }
}
