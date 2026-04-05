package com.flocklab;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.flocklab.config.DeviceProfile;
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
    private final com.flocklab.config.PlatformHints platformHints;

    public FlockLabGame() {
        this(null);
    }

    public FlockLabGame(com.flocklab.config.PlatformHints hints) {
        this.platformHints = hints;
    }

    @Override
    public void create() {
        DeviceProfile profile = DeviceProfile.detect(platformHints);
        SimulationConfig config = new SimulationConfig(profile);

        // Initialise world dimensions to exact screen size (avoid default stretching)
        config.worldWidth = Gdx.graphics.getWidth();
        config.worldHeight = Gdx.graphics.getHeight();
        if (config.worldWidth == 0) config.worldWidth = 1280f;
        if (config.worldHeight == 0) config.worldHeight = 720f;

        world = new World(config);

        // Setup world camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, config.worldWidth, config.worldHeight);
        worldRenderer = new WorldRenderer(world, camera);

        // Setup UI
        skin = SkinFactory.createSkin(profile);
        stage = new Stage(new ScreenViewport());

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Simulation area (left/center/top)
        Table mainArea = new Table();
        if (profile == DeviceProfile.DESKTOP) {
            root.add(mainArea).expand().fill();
        } else {
            root.add(mainArea).expand().fill().row();
        }

        // Footer in mainArea - use a spacer to push it to the bottom
        mainArea.add().expand().fill().row();

        if (profile == DeviceProfile.DESKTOP) {
            Table footer = new Table();
            TextButton creditBtn = new TextButton("Developed by Eyal Shahaf  |  Version " + AppVersion.VERSION, skin);
            creditBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.net.openURI("https://github.com/EyalShahaf/boids-studio");
                }
            });
            footer.add(creditBtn).padBottom(10);
            mainArea.add(footer).bottom().center();
        }

        // Control Panel (right)
        controlPanel = new ControlPanel(root, stage, skin, world, this);
        statsOverlay = new StatsOverlay(stage, skin, world);

        // Input distribution: UI first, then world/touch interactions
        InputMultiplexer multiplexer = new InputMultiplexer();
        InputHandler inputHandler = new InputHandler(world, camera, stage);
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(inputHandler);
        Gdx.input.setInputProcessor(multiplexer);

        // Allow FPS higher than 60 if the hardware supports it (locks to refresh rate
        // by default)
        Gdx.graphics.setForegroundFPS(0);

        Gdx.app.log("FlockLab", "Initialized successfully with UI!");
    }

    @Override
    public void render() {
        float rawDelta = Gdx.graphics.getDeltaTime();

        // 1. Logic Update
        if (!isPaused) {
            float deltaTime = Math.min(rawDelta, 0.1f);
            world.update(deltaTime);
        }

        // Update UI logic (throttled to ~8 Hz to reduce string allocation pressure)
        controlPanel.update(rawDelta);
        statsOverlay.update(rawDelta);
        stage.act(rawDelta);

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
        world.getConfig().worldWidth = width;
        world.getConfig().worldHeight = height;
        camera.setToOrtho(false, width, height);
        stage.getViewport().update(width, height, true);
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public WorldRenderer getWorldRenderer() {
        return worldRenderer;
    }

    @Override
    public void dispose() {
        worldRenderer.dispose();
        stage.dispose();
        skin.dispose();
    }
}
