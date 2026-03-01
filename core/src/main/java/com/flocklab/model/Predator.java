package com.flocklab.model;

/**
 * Predator entity that chases boids, causing them to flee.
 * Similar to a boid but uses its own speed limitation and logic.
 */
public class Predator {
    private Vec2 position;
    private Vec2 velocity;
    private float maxSpeed;

    public Predator(Vec2 position, Vec2 velocity, float maxSpeed) {
        this.position = position;
        this.velocity = velocity;
        this.maxSpeed = maxSpeed;
    }

    public Vec2 getPosition() {
        return position;
    }

    public void setPosition(Vec2 position) {
        this.position = position;
    }

    public Vec2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vec2 velocity) {
        this.velocity = velocity;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    /**
     * Updates predator position based on given steering force.
     * Wraps around edges similar to boids.
     */
    public void update(Vec2 steeringForce, float deltaTime, float worldWidth, float worldHeight) {
        // Apply steering to velocity
        velocity = velocity.add(steeringForce.scale(deltaTime));

        // Limit speed
        velocity = velocity.limit(maxSpeed);

        // Apply velocity to position
        position = position.add(velocity.scale(deltaTime));

        // Wrap edges
        wrapEdges(worldWidth, worldHeight);
    }

    private void wrapEdges(float worldWidth, float worldHeight) {
        float x = position.x();
        float y = position.y();

        if (x < 0)
            x += worldWidth;
        else if (x >= worldWidth)
            x -= worldWidth;

        if (y < 0)
            y += worldHeight;
        else if (y >= worldHeight)
            y -= worldHeight;

        this.position = new Vec2(x, y);
    }
}
