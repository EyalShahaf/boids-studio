package com.flocklab.model;

/**
 * Immutable 2D Vector used for position, velocity, and forces.
 * Refactored to standard class for GWT compatibility (no records supported).
 */
public final class Vec2 {
    public static final Vec2 ZERO = new Vec2(0, 0);

    private final float x;
    private final float y;

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public Vec2 add(Vec2 other) {
        return new Vec2(this.x + other.x, this.y + other.y);
    }

    public Vec2 sub(Vec2 other) {
        return new Vec2(this.x - other.x, this.y - other.y);
    }

    public Vec2 scale(float scalar) {
        return new Vec2(this.x * scalar, this.y * scalar);
    }

    public float magnitude() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public float magnitudeSquare() {
        return x * x + y * y;
    }

    public Vec2 normalize() {
        float mag = magnitude();
        if (mag > 0) {
            return new Vec2(x / mag, y / mag);
        }
        return ZERO;
    }

    /**
     * Limits the magnitude of this vector to the given max value.
     */
    public Vec2 limit(float max) {
        float magSq = magnitudeSquare();
        if (magSq > max * max) {
            return normalize().scale(max);
        }
        return this;
    }

    /**
     * Set the magnitude of this vector to a specific value.
     */
    public Vec2 setMagnitude(float length) {
        return normalize().scale(length);
    }

    public float distanceTo(Vec2 other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public float distanceSquareTo(Vec2 other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;
        return dx * dx + dy * dy;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Vec2 vec2 = (Vec2) o;
        return Float.compare(vec2.x, x) == 0 && Float.compare(vec2.y, y) == 0;
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Vec2{" + "x=" + x + ", y=" + y + '}';
    }
}
