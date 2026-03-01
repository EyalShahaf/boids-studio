package com.flocklab.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.flocklab.model.Boid;
import com.flocklab.model.Vec2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Renders fading motion trails behind boids.
 */
public class TrailRenderer {

    // Number of past positions to remember
    private int trailLength = 15;
    public Color trailColor = new Color(0.2f, 0.7f, 0.9f, 0.3f);
    public boolean enabled = true;

    // Track history for each boid ID
    private final Map<Integer, LinkedList<Vec2>> history = new HashMap<>();

    public void updateAndRender(ShapeRenderer shapeRenderer, List<Boid> boids) {
        if (!enabled)
            return;

        // Clean up dead boids from history
        if (history.size() > boids.size() * 1.5) {
            history.keySet().retainAll(boids.stream().map(Boid::getId).collect(Collectors.toList()));
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (Boid boid : boids) {
            LinkedList<Vec2> path = history.computeIfAbsent(boid.getId(), k -> new LinkedList<>());

            // Check if boid jumped across screen (wrapped) — if so, clear trail to avoid a
            // long line across screen
            if (!path.isEmpty()) {
                if (path.getLast().distanceSquareTo(boid.getPosition()) > 100 * 100) {
                    path.clear();
                }
            }

            path.addLast(boid.getPosition());
            if (path.size() > trailLength) {
                path.removeFirst();
            }

            // Render path
            if (path.size() >= 2) {
                Iterator<Vec2> it = path.iterator();
                Vec2 prev = it.next();

                int i = 1;
                while (it.hasNext()) {
                    Vec2 curr = it.next();
                    // Fade out older segments
                    float alpha = (float) i / path.size() * trailColor.a;
                    shapeRenderer.setColor(trailColor.r, trailColor.g, trailColor.b, alpha);
                    shapeRenderer.line(prev.x(), prev.y(), curr.x(), curr.y());
                    prev = curr;
                    i++;
                }
            }
        }

        shapeRenderer.end();
    }

    public void setTrailLength(int length) {
        this.trailLength = length;
    }
}
