package com.flocklab.model;

/**
 * A circular obstacle that boids must avoid.
 */
public record Obstacle(Vec2 center, float radius) {
}
