package com.flocklab.sim;

import com.flocklab.model.Attractor;
import com.flocklab.model.Boid;
import com.flocklab.model.Obstacle;
import com.flocklab.model.Predator;
import com.flocklab.model.Vec2;

import java.util.List;

/**
 * Static methods calculating the steering forces for boids.
 */
public class BoidRules {

    public static Vec2 separation(Boid boid, List<Boid> neighbors, float perceptionRadius) {
        Vec2 steering = Vec2.ZERO;
        int count = 0;

        for (Boid other : neighbors) {
            float d2 = boid.getPosition().distanceSquareTo(other.getPosition());
            if (other != boid && d2 > 0 && d2 < perceptionRadius * perceptionRadius) {
                float d = (float) Math.sqrt(d2);
                Vec2 diff = boid.getPosition().sub(other.getPosition());
                diff = diff.normalize().scale(1f / d); // Weight by inverse distance
                steering = steering.add(diff);
                count++;
            }
        }

        if (count > 0) {
            steering = steering.scale(1f / count);
        }
        return steering;
    }

    public static Vec2 alignment(Boid boid, List<Boid> neighbors, float perceptionRadius) {
        Vec2 steering = Vec2.ZERO;
        int count = 0;

        for (Boid other : neighbors) {
            float d2 = boid.getPosition().distanceSquareTo(other.getPosition());
            if (other != boid && d2 > 0 && d2 < perceptionRadius * perceptionRadius) {
                steering = steering.add(other.getVelocity());
                count++;
            }
        }

        if (count > 0) {
            steering = steering.scale(1f / count).sub(boid.getVelocity());
        }
        return steering;
    }

    public static Vec2 cohesion(Boid boid, List<Boid> neighbors, float perceptionRadius) {
        Vec2 centerOfMass = Vec2.ZERO;
        int count = 0;

        for (Boid other : neighbors) {
            float d2 = boid.getPosition().distanceSquareTo(other.getPosition());
            if (other != boid && d2 > 0 && d2 < perceptionRadius * perceptionRadius) {
                centerOfMass = centerOfMass.add(other.getPosition());
                count++;
            }
        }

        if (count > 0) {
            centerOfMass = centerOfMass.scale(1f / count);
            return centerOfMass.sub(boid.getPosition());
        }
        return Vec2.ZERO;
    }

    public static Vec2 avoidObstacles(Boid boid, List<Obstacle> obstacles, float avoidRadius) {
        Vec2 steering = Vec2.ZERO;
        for (Obstacle obs : obstacles) {
            Vec2 diff = boid.getPosition().sub(obs.center());
            float d = diff.magnitude();
            // Steer away strongly when close to obstacle radius
            if (d < avoidRadius + obs.radius()) {
                float force = (avoidRadius + obs.radius()) - d;
                steering = steering.add(diff.normalize().scale(force));
            }
        }
        return steering;
    }

    public static Vec2 fleePredators(Boid boid, List<Predator> predators, float perceptionRadius) {
        Vec2 steering = Vec2.ZERO;
        for (Predator p : predators) {
            Vec2 diff = boid.getPosition().sub(p.getPosition());
            float d2 = diff.magnitudeSquare();
            if (d2 > 0 && d2 < perceptionRadius * perceptionRadius) {
                steering = steering.add(diff.normalize().scale(1f / (float) Math.sqrt(d2)));
            }
        }
        return steering;
    }

    public static Vec2 seekAttractors(Boid boid, List<Attractor> attractors, float perceptionRadius) {
        Vec2 steering = Vec2.ZERO;
        for (Attractor a : attractors) {
            float d2 = boid.getPosition().distanceSquareTo(a.position());
            if (d2 < perceptionRadius * perceptionRadius) {
                Vec2 desired = a.position().sub(boid.getPosition());
                steering = steering.add(desired.normalize().scale(a.strength()));
            }
        }
        return steering;
    }
}
