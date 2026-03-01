package com.flocklab;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.flocklab.config.SimulationConfig;
import com.flocklab.input.InputHandler;
import com.flocklab.render.WorldRenderer;
import com.flocklab.sim.World;
import com.flocklab.ui.ControlPanel;
import com.flocklab.ui.SkinFactory;
import com.flocklab.ui.StatsOverlay;

/**
 * Main game entry point.
 */
public class FlockLabGame extends ApplicationAdapter {

    private World world;
    private WorldRenderer worldRenderer;
    private OrthographicCamera camera;

    // UI components
    private Stage stage;
    private Skin skin;
    private ControlPanel controlPanel;
    private StatsOverlay statsOverlay;

    private boolean isPaused = false;

    @Override
    public void create() {
        SimulationConfig config = new SimulationConfig();
        config.worldWidth = 1280f;
        config.worldHeight = 720f;

        world = new World(config);

        // Setup world camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, config.worldWidth, config.worldHeight);
        worldRenderer = new WorldRenderer(world, camera);

        // Setup UI
        skin = SkinFactory.createSkin();
        stage = new Stage(new ScreenViewport());
        controlPanel = new ControlPanel(stage, skin, world, this);
        statsOverlay = new StatsOverlay(stage, skin, world);

        // Input distribution: UI first, then world interactions
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new InputHandler(world, camera));
        Gdx.input.setInputProcessor(multiplexer);

        Gdx.app.log("FlockLab", "Initialized successfully with UI!");
    }

    @Override
    public void render() {
        // 1. Logic Update
        if (!isPaused) {
            float deltaTime = Gdx.graphics.getDeltaTime();
            if (deltaTime > 0.1f)
                deltaTime = 0.1f;
            world.update(deltaTime);
        }

        // Update UI logic
        controlPanel.update();
        statsOverlay.update();
        stage.act(Gdx.graphics.getDeltaTime());

        // 2. Rendering
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render world
        worldRenderer.render();

        // Render UI over world
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, world.getConfig().worldWidth, world.getConfig().worldHeight);
        stage.getViewport().update(width, height, true);
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
        stage.dispose();
        skin.dispose();
    }
}
