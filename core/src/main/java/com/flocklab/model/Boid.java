package com.flocklab.model;

/**
 * Mutable entity representing a single Boid in the simulation.
 */
public class Boid {
    private final int id;
    private Vec2 position;
    private Vec2 velocity;
    private Vec2 acceleration;

    private float stamina;
    private float hunger;
    private boolean isSprinting;

    public Boid(int id, Vec2 position, Vec2 velocity, float initialStamina, float initialHunger) {
        this.id = id;
        this.position = position;
        this.velocity = velocity;
        this.acceleration = Vec2.ZERO;
        this.stamina = initialStamina;
        this.hunger = initialHunger;
        this.isSprinting = false;
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

    public float getStamina() {
        return stamina;
    }

    public void setStamina(float stamina) {
        this.stamina = stamina;
    }

    public float getHunger() {
        return hunger;
    }

    public void setHunger(float hunger) {
        this.hunger = hunger;
    }

    public boolean isSprinting() {
        return isSprinting;
    }

    public void setSprinting(boolean sprinting) {
        this.isSprinting = sprinting;
    }

    /**
     * Adds a steering force to the current acceleration.
     */
    public void applyForce(Vec2 force) {
        this.acceleration = this.acceleration.add(force);
    }

    /**
     * Updates physics, limits speed, resets acceleration, and wraps around world edges.
     */
    public void update(float deltaTime, float maxSpeed, float worldWidth, float worldHeight) {
        velocity = velocity.add(acceleration.scale(deltaTime));

        float speed = velocity.magnitude();
        if (speed > maxSpeed) {
            velocity = velocity.setMagnitude(maxSpeed);
        }

        position = position.add(velocity.scale(deltaTime));
        acceleration = Vec2.ZERO;
        wrapEdges(worldWidth, worldHeight);
    }

    /**
     * Sprint-aware update that applies a speed boost when sprinting.
     */
    public void update(float deltaTime, float maxSpeed, float sprintMultiplier,
            float worldWidth, float worldHeight) {
        velocity = velocity.add(acceleration.scale(deltaTime));

        float effectiveMax = isSprinting ? maxSpeed * sprintMultiplier : maxSpeed;
        float speed = velocity.magnitude();
        if (speed > effectiveMax) {
            velocity = velocity.setMagnitude(effectiveMax);
        }

        position = position.add(velocity.scale(deltaTime));
        acceleration = Vec2.ZERO;
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
