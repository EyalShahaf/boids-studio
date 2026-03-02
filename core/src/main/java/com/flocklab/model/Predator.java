package com.flocklab.model;

/**
 * Predator entity that chases boids, causing them to flee.
 * Has stamina (for sprinting) and hunger (starves to death without eating).
 */
public class Predator {
    private final int id;
    private Vec2 position;
    private Vec2 velocity;
    private float maxSpeed;

    private float stamina;
    private float hunger;
    private boolean isSprinting;

    public Predator(int id, Vec2 position, Vec2 velocity, float maxSpeed,
            float initialStamina, float initialHunger) {
        this.id = id;
        this.position = position;
        this.velocity = velocity;
        this.maxSpeed = maxSpeed;
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

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
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
     * Updates predator position based on a given steering force, applying a sprint
     * speed multiplier when isSprinting is true.
     */
    public void update(Vec2 steeringForce, float deltaTime, float sprintMultiplier,
            float worldWidth, float worldHeight) {
        velocity = velocity.add(steeringForce.scale(deltaTime));

        float effectiveMax = isSprinting ? maxSpeed * sprintMultiplier : maxSpeed;
        velocity = velocity.limit(effectiveMax);

        position = position.add(velocity.scale(deltaTime));
        wrapEdges(worldWidth, worldHeight);
    }

    /**
     * Legacy update without sprint multiplier (used internally when no sprint is active).
     */
    public void update(Vec2 steeringForce, float deltaTime, float worldWidth, float worldHeight) {
        update(steeringForce, deltaTime, 1.0f, worldWidth, worldHeight);
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
