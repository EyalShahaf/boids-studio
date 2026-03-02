package com.flocklab.model;

/**
 * Mutable entity representing a single Boid in the simulation.
 */
public class Boid {
    private final int id;
    private Vec2 position;
    private Vec2 velocity;
    private Vec2 acceleration;

    public Boid(int id, Vec2 position, Vec2 velocity) {
        this.id = id;
        this.position = position;
        this.velocity = velocity;
        this.acceleration = Vec2.ZERO;
    }

    public int getId() {
        return id;
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

    /**
     * Adds a steering force to the current acceleration.
     */
    public void applyForce(Vec2 force) {
        this.acceleration = this.acceleration.add(force);
    }

    /**
     * Updates physics, limits speed, resets acceleration, and wraps around world
     * edges.
     */
    public void update(float deltaTime, float maxSpeed, float worldWidth, float worldHeight) {
        // Apply acceleration to velocity
        velocity = velocity.add(acceleration.scale(deltaTime));

        // Enforce maximum speed
        float speed = velocity.magnitude();
        if (speed > maxSpeed) {
            velocity = velocity.setMagnitude(maxSpeed);
        }

        // Apply velocity to position
        position = position.add(velocity.scale(deltaTime));

        // Reset acceleration for next frame
        acceleration = Vec2.ZERO;

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
