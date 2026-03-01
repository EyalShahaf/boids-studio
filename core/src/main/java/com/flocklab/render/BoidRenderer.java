package com.flocklab.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.flocklab.model.Boid;
import com.flocklab.model.Predator;

import java.util.List;

/**
 * Responsible for rendering boids and predators as oriented shapes.
 */
public class BoidRenderer {

    // Configurable colors
    public Color boidColor = new Color(0.2f, 0.7f, 0.9f, 1f);
    public Color predatorColor = new Color(0.9f, 0.2f, 0.2f, 1f);

    // Geometry constants
    private static final float BOID_SIZE = 6f;
    private static final float PREDATOR_SIZE = 12f;

    public void render(ShapeRenderer shapeRenderer, List<Boid> boids, List<Predator> predators) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw normal boids
        shapeRenderer.setColor(boidColor);
        for (Boid boid : boids) {
            drawOrientedTriangle(shapeRenderer, boid.getPosition().x(), boid.getPosition().y(),
                    boid.getVelocity().angle(), BOID_SIZE);
        }

        // Draw predators
        shapeRenderer.setColor(predatorColor);
        for (Predator predator : predators) {
            drawOrientedTriangle(shapeRenderer, predator.getPosition().x(), predator.getPosition().y(),
                    predator.getVelocity().angle(), PREDATOR_SIZE);
        }

        shapeRenderer.end();
    }

    private void drawOrientedTriangle(ShapeRenderer sr, float x, float y, float angleRad, float size) {
        // Pointing forward
        float x1 = x + (float) Math.cos(angleRad) * size * 1.5f;
        float y1 = y + (float) Math.sin(angleRad) * size * 1.5f;

        // Back left
        float x2 = x + (float) Math.cos(angleRad + 2.5) * size;
        float y2 = y + (float) Math.sin(angleRad + 2.5) * size;

        // Back right
        float x3 = x + (float) Math.cos(angleRad - 2.5) * size;
        float y3 = y + (float) Math.sin(angleRad - 2.5) * size;

        sr.triangle(x1, y1, x2, y2, x3, y3);
    }
}
