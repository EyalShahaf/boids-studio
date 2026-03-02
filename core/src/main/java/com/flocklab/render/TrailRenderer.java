package com.flocklab.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.flocklab.model.Boid;
import com.flocklab.model.Vec2;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Renders fading motion trails behind boids.
 *
 * Adapts trail length and rendering density based on boid count to keep
 * draw call cost manageable at high population sizes.
 */
public class TrailRenderer {

    private static final int TRAIL_LENGTH_NORMAL = 15;
    private static final int TRAIL_LENGTH_MEDIUM = 8;
    private static final int TRAIL_LENGTH_SHORT = 4;

    /** Boid count thresholds for adaptive trail quality reduction. */
    private static final int THRESHOLD_REDUCE = 600;
    private static final int THRESHOLD_SHORT = 1000;
    private static final int THRESHOLD_DISABLE = 1500;

    private int trailLength = TRAIL_LENGTH_NORMAL;
    public Color trailColor = new Color(0.2f, 0.7f, 0.9f, 0.3f);
    public boolean enabled = true;

    /** ArrayDeque gives O(1) addLast/removeFirst with array cache locality. */
    private final Map<Integer, ArrayDeque<Vec2>> history = new HashMap<>();

    public void updateAndRender(ShapeRenderer shapeRenderer, List<Boid> boids) {
        if (!enabled) return;

        int boidCount = boids.size();

        // Determine active trail length from current boid population
        int activeLength;
        if (boidCount >= THRESHOLD_DISABLE) {
            return; // trails off entirely at very high counts
        } else if (boidCount >= THRESHOLD_SHORT) {
            activeLength = TRAIL_LENGTH_SHORT;
        } else if (boidCount >= THRESHOLD_REDUCE) {
            activeLength = TRAIL_LENGTH_MEDIUM;
        } else {
            activeLength = trailLength;
        }

        // Render every Nth segment at medium-high counts to halve draw calls
        int renderStep = (boidCount >= THRESHOLD_SHORT) ? 2 : 1;

        // Evict stale entries only when history has grown beyond the live boid set
        if (history.size() > boidCount + 10) {
            Set<Integer> activeIds = new HashSet<>(boidCount * 2);
            for (int i = 0; i < boidCount; i++) {
                activeIds.add(boids.get(i).getId());
            }
            history.keySet().retainAll(activeIds);
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (int b = 0; b < boidCount; b++) {
            Boid boid = boids.get(b);
            ArrayDeque<Vec2> path = history.computeIfAbsent(boid.getId(), k -> new ArrayDeque<>(activeLength + 1));

            // Clear trail when boid wraps across the world edge
            if (!path.isEmpty() && path.peekLast().distanceSquareTo(boid.getPosition()) > 100 * 100) {
                path.clear();
            }

            path.addLast(boid.getPosition());
            while (path.size() > activeLength) {
                path.removeFirst();
            }

            if (path.size() < 2) continue;

            Iterator<Vec2> it = path.iterator();
            Vec2 prev = it.next();
            int i = 1;

            while (it.hasNext()) {
                Vec2 curr = it.next();
                if (i % renderStep == 0) {
                    float alpha = (float) i / path.size() * trailColor.a;
                    shapeRenderer.setColor(trailColor.r, trailColor.g, trailColor.b, alpha);
                    shapeRenderer.line(prev.x(), prev.y(), curr.x(), curr.y());
                }
                prev = curr;
                i++;
            }
        }

        shapeRenderer.end();
    }

    public void setTrailLength(int length) {
        this.trailLength = length;
    }

    public int getTrailLength() {
        return trailLength;
    }
}
