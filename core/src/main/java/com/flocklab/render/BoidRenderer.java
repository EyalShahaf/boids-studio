package com.flocklab.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.flocklab.model.Boid;
import com.flocklab.model.Predator;

import java.util.List;

/**
 * Responsible for rendering boids and predators as oriented triangles.
 * Uses level-of-detail (LOD) to reduce geometry at high boid counts.
 */
public class BoidRenderer {

    public Color boidColor = new Color(0.2f, 0.7f, 0.9f, 1f);
    public Color predatorColor = new Color(0.9f, 0.2f, 0.2f, 1f);

    private static final float BOID_SIZE_NORMAL = 6f;
    private static final float BOID_SIZE_MEDIUM = 4f;
    private static final float BOID_SIZE_SMALL = 3f;
    private static final float PREDATOR_SIZE = 12f;

    private static final int LOD_MEDIUM_THRESHOLD = 1000;
    private static final int LOD_SMALL_THRESHOLD = 1500;

    public void render(ShapeRenderer shapeRenderer, List<Boid> boids, List<Predator> predators) {
        int boidCount = boids.size();
        float boidSize = boidCount >= LOD_SMALL_THRESHOLD ? BOID_SIZE_SMALL
                : boidCount >= LOD_MEDIUM_THRESHOLD ? BOID_SIZE_MEDIUM
                : BOID_SIZE_NORMAL;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < boidCount; i++) {
            Boid boid = boids.get(i);
            float angleRad = boid.getVelocity().angle();
            // Map velocity angle to hue (0–360) for dynamic coloring
            boidColor.fromHsv(((float) Math.toDegrees(angleRad) + 360) % 360, 0.7f, 0.9f);
            shapeRenderer.setColor(boidColor);
            drawOrientedTriangle(shapeRenderer, boid.getPosition().x(), boid.getPosition().y(),
                    angleRad, boidSize);
        }

        shapeRenderer.setColor(predatorColor);
        for (int i = 0; i < predators.size(); i++) {
            Predator predator = predators.get(i);
            drawOrientedTriangle(shapeRenderer, predator.getPosition().x(), predator.getPosition().y(),
                    predator.getVelocity().angle(), PREDATOR_SIZE);
        }

        shapeRenderer.end();
    }

    private void drawOrientedTriangle(ShapeRenderer sr, float x, float y, float angleRad, float size) {
        float cos = (float) Math.cos(angleRad);
        float sin = (float) Math.sin(angleRad);

        float x1 = x + cos * size * 1.5f;
        float y1 = y + sin * size * 1.5f;

        float cos2 = (float) Math.cos(angleRad + 2.5);
        float sin2 = (float) Math.sin(angleRad + 2.5);
        float x2 = x + cos2 * size;
        float y2 = y + sin2 * size;

        float cos3 = (float) Math.cos(angleRad - 2.5);
        float sin3 = (float) Math.sin(angleRad - 2.5);
        float x3 = x + cos3 * size;
        float y3 = y + sin3 * size;

        sr.triangle(x1, y1, x2, y2, x3, y3);
    }
}
