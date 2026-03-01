package com.flocklab.model;

/**
 * Immutable 2D vector for simulation math.
 * All math operations return a new Vec2 instance to simplify reasoning
 * and prevent unexpected mutations in the game loop.
 */
public record Vec2(float x, float y) {

    public static final Vec2 ZERO = new Vec2(0, 0);

    public Vec2 add(Vec2 other) {
        return new Vec2(this.x + other.x, this.y + other.y);
    }

    public Vec2 sub(Vec2 other) {
        return new Vec2(this.x - other.x, this.y - other.y);
    }

    public Vec2 scale(float scalar) {
        return new Vec2(this.x * scalar, this.y * scalar);
    }

    public float magnitudeSquare() {
        return (x * x) + (y * y);
    }

    public float magnitude() {
        return (float) Math.sqrt(magnitudeSquare());
    }

    public float distanceTo(Vec2 other) {
        return this.sub(other).magnitude();
    }

    public float distanceSquareTo(Vec2 other) {
        return this.sub(other).magnitudeSquare();
    }

    public Vec2 normalize() {
        float mag = magnitude();
        if (mag == 0)
            return ZERO;
        return new Vec2(x / mag, y / mag);
    }

    /**
     * Limits the magnitude of this vector to the given max value.
     */
    public Vec2 limit(float max) {
        if (magnitudeSquare() > max * max) {
            return this.normalize().scale(max);
        }
        return this;
    }

    /**
     * Set the magnitude of this vector to a specific value.
     */
    public Vec2 setMagnitude(float mag) {
        return this.normalize().scale(mag);
    }

    public float dot(Vec2 other) {
        return this.x * other.x + this.y * other.y;
    }

    /**
     * Returns the angle in radians of this vector.
     */
    public float angle() {
        return (float) Math.atan2(y, x);
    }
}
