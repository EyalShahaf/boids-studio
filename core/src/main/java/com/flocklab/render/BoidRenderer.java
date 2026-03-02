package com.flocklab.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.flocklab.config.SimulationConfig;
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

    public void render(ShapeRenderer shapeRenderer, List<Boid> boids, List<Predator> predators,
            SimulationConfig config) {
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

        // Life bars drawn after triangles so they render on top
        if (showLifeBars) {
            drawBoidLifeBars(shapeRenderer, boids, config, boidSize);
            drawPredatorLifeBars(shapeRenderer, predators, config, predatorSize);
        }

        shapeRenderer.end();
    }

    /** Fraction below which a boid's hunger bar becomes visible. */
    private static final float BOID_HUNGER_WARN_THRESHOLD = 0.75f;

    /**
     * Draws tiny life bars above boids when relevant:
     *  - Stamina bar: only when the boid is sprinting (predator nearby).
     *  - Hunger bar: when hunger drops below BOID_HUNGER_WARN_THRESHOLD.
     * Stacking both bars keeps the display compact while showing meaningful state.
     */
    private void drawBoidLifeBars(ShapeRenderer sr, List<Boid> boids,
            SimulationConfig config, float boidSize) {
        float barWidth = 10f;
        float barHeight = 2f;
        float gap = 1f;
        float baseOffsetY = boidSize * 1.5f + 3f;

        for (int i = 0; i < boids.size(); i++) {
            Boid boid = boids.get(i);
            boolean showStamina = boid.isSprinting();
            boolean showHunger = boid.getHunger() / config.boidHungerMax < BOID_HUNGER_WARN_THRESHOLD;

            if (!showStamina && !showHunger) continue;

            float bx = boid.getPosition().x() - barWidth / 2f;
            float by = boid.getPosition().y() + baseOffsetY;

            if (showStamina) {
                float fraction = boid.getStamina() / config.boidStaminaMax;
                sr.setColor(0.2f, 0.2f, 0.2f, 0.8f);
                sr.rect(bx, by, barWidth, barHeight);
                // Cyan (full) → red (empty)
                sr.setColor(1f - fraction, fraction, fraction * 0.8f, 1f);
                sr.rect(bx, by, barWidth * fraction, barHeight);
                by += barHeight + gap;
            }

            if (showHunger) {
                float fraction = boid.getHunger() / config.boidHungerMax;
                sr.setColor(0.2f, 0.2f, 0.2f, 0.8f);
                sr.rect(bx, by, barWidth, barHeight);
                // Orange (full) → dark red (empty)
                sr.setColor(0.9f, fraction * 0.6f, 0f, 1f);
                sr.rect(bx, by, barWidth * fraction, barHeight);
            }
        }
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

    /**
     * Draws hunger and stamina bars above each predator (always visible when showLifeBars is on).
     * Hunger bar: green (full) → red (empty). Stamina bar: white → dark.
     */
    private void drawPredatorLifeBars(ShapeRenderer sr, List<Predator> predators,
            SimulationConfig config, float predatorSize) {
        float barWidth = 20f;
        float hungerBarHeight = 3f;
        float staminaBarHeight = 2f;
        float gap = 2f;
        float baseY = predatorSize * 1.5f + 4f;

        for (int i = 0; i < predators.size(); i++) {
            Predator predator = predators.get(i);
            float px = predator.getPosition().x() - barWidth / 2f;
            float py = predator.getPosition().y() + baseY;

            float hungerFraction = predator.getHunger() / config.predatorHungerMax;
            float staminaFraction = predator.getStamina() / config.predatorStaminaMax;

            // Hunger bar background
            sr.setColor(0.2f, 0.2f, 0.2f, 0.8f);
            sr.rect(px, py, barWidth, hungerBarHeight);
            // Hunger bar fill: green → red
            sr.setColor(1f - hungerFraction, hungerFraction, 0f, 1f);
            sr.rect(px, py, barWidth * hungerFraction, hungerBarHeight);

            float staminaY = py + hungerBarHeight + gap;

            // Stamina bar background
            sr.setColor(0.2f, 0.2f, 0.2f, 0.8f);
            sr.rect(px, staminaY, barWidth, staminaBarHeight);
            // Stamina bar fill: white → dark gray
            sr.setColor(staminaFraction, staminaFraction, staminaFraction, 1f);
            sr.rect(px, staminaY, barWidth * staminaFraction, staminaBarHeight);
        }
    }

    public boolean isShowLifeBars() {
        return showLifeBars;
    }

    public void setShowLifeBars(boolean showLifeBars) {
        this.showLifeBars = showLifeBars;
    }
}
