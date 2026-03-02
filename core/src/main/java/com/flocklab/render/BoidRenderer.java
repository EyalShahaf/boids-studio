package com.flocklab.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.flocklab.model.Boid;
import com.flocklab.model.Predator;

import java.util.List;

/**
 * Responsible for rendering boids and predators as oriented triangles.
 * Uses level-of-detail (LOD) to reduce geometry at high boid counts.
 * Both boid and predator sizes are derived from a shared LOD scale so they
 * always remain proportional (predator = 2× boid at every LOD level).
 */
public class BoidRenderer {

    public Color boidColor = new Color(0.2f, 0.7f, 0.9f, 1f);
    public Color predatorColor = new Color(0.9f, 0.2f, 0.2f, 1f);

    private static final float BOID_BASE_SIZE = 6f;
    private static final float PREDATOR_BASE_SIZE = 12f;

    private static final int LOD_MEDIUM_THRESHOLD = 1000;
    private static final int LOD_SMALL_THRESHOLD = 1500;

    private boolean showLifeBars = true;

    public void render(ShapeRenderer shapeRenderer, List<Boid> boids, List<Predator> predators) {
        int boidCount = boids.size();
        float lodScale = computeLodScale(boidCount);
        float boidSize = BOID_BASE_SIZE * lodScale;
        float predatorSize = PREDATOR_BASE_SIZE * lodScale;

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
                    predator.getVelocity().angle(), predatorSize);
        }

        shapeRenderer.end();
    }

    /**
     * Returns a LOD scale factor based on boid count.
     * Both boid and predator base sizes are multiplied by this value so they
     * always stay proportional to each other.
     */
    private float computeLodScale(int boidCount) {
        if (boidCount >= LOD_SMALL_THRESHOLD) return 0.5f;
        if (boidCount >= LOD_MEDIUM_THRESHOLD) return 0.66f;
        return 1.0f;
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

    public boolean isShowLifeBars() {
        return showLifeBars;
    }

    public void setShowLifeBars(boolean showLifeBars) {
        this.showLifeBars = showLifeBars;
    }
}
