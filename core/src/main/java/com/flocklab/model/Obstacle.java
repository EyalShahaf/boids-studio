package com.flocklab.model;

/**
 * Circular obstacle that boids must avoid.
 */
public class Obstacle {
    private final Vec2 center;
    private final float radius;

    public Obstacle(Vec2 center, float radius) {
        this.center = center;
        this.radius = radius;
    }

    public Vec2 center() {
        return center;
    }

    public float radius() {
        return radius;
    }
}
