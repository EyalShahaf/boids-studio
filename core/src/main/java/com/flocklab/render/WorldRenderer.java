package com.flocklab.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.flocklab.model.Attractor;
import com.flocklab.model.Obstacle;
import com.flocklab.sim.World;

/**
 * Orchestrator that coordinates all rendering layers.
 */
public class WorldRenderer implements Disposable {
    private final World world;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapeRenderer;

    private final BackgroundRenderer backgroundRenderer;
    private final TrailRenderer trailRenderer;
    private final BoidRenderer boidRenderer;

    public WorldRenderer(World world, OrthographicCamera camera) {
        this.world = world;
        this.camera = camera;
        this.shapeRenderer = new ShapeRenderer();

        this.backgroundRenderer = new BackgroundRenderer();
        this.trailRenderer = new TrailRenderer();
        this.boidRenderer = new BoidRenderer();
    }

    public void render() {
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        // 1. Render Background
        backgroundRenderer.render(shapeRenderer, world.getConfig().worldWidth, world.getConfig().worldHeight,
                camera.combined);

        // Enable OpenGL blending for alpha transparency (trails, glow)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // 2. Render Environment (Obstacles and Attractors)
        renderEnvironment();

        // 3. Render Trails
        trailRenderer.updateAndRender(shapeRenderer, world.getBoids());

        // 4. Render Boids & Predators
        boidRenderer.render(shapeRenderer, world.getBoids(), world.getPredators(), world.getConfig());

        // Cleanup GL state
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderEnvironment() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Obstacles
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        for (Obstacle obs : world.getObstacles()) {
            shapeRenderer.circle(obs.center().x(), obs.center().y(), obs.radius());
        }

        // Attractors
        shapeRenderer.setColor(0.4f, 0.9f, 0.4f, 0.6f);
        for (Attractor att : world.getAttractors()) {
            shapeRenderer.circle(att.position().x(), att.position().y(), 8f);
            shapeRenderer.setColor(0.4f, 0.9f, 0.4f, 0.2f);
            shapeRenderer.circle(att.position().x(), att.position().y(), 16f); // outer glow
        }

        shapeRenderer.end();
    }

    public TrailRenderer getTrailRenderer() {
        return trailRenderer;
    }

    public BoidRenderer getBoidRenderer() {
        return boidRenderer;
    }

    public BackgroundRenderer getBackgroundRenderer() {
        return backgroundRenderer;
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
