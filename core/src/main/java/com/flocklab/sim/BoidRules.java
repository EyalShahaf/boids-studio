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

    /**
     * Combines separation, alignment, and cohesion in a single neighbor pass using
     * raw float arithmetic to minimize object allocation. This is the primary
     * hot-path method used by World.update.
     *
     * Separation weight: inverse-distance weighted (stronger when very close).
     * Alignment weight: match average neighbor velocity.
     * Cohesion weight: steer toward average neighbor position.
     */
    public static Vec2 flock(Boid boid, List<Boid> neighbors, float perceptionRadiusSq,
            float separationWeight, float alignmentWeight, float cohesionWeight) {
        float bx = boid.getPosition().x();
        float by = boid.getPosition().y();
        float bvx = boid.getVelocity().x();
        float bvy = boid.getVelocity().y();

        float sepX = 0, sepY = 0;
        float alignX = 0, alignY = 0;
        float cohX = 0, cohY = 0;
        int count = 0;

        for (int n = 0; n < neighbors.size(); n++) {
            Boid other = neighbors.get(n);
            if (other == boid) continue;

            float dx = bx - other.getPosition().x();
            float dy = by - other.getPosition().y();
            float d2 = dx * dx + dy * dy;

            if (d2 > 0 && d2 < perceptionRadiusSq) {
                // Separation: normalize(diff)*500/d = diff*500/d² — no sqrt required
                float scale = 500f / d2;
                sepX += dx * scale;
                sepY += dy * scale;

                alignX += other.getVelocity().x();
                alignY += other.getVelocity().y();

                cohX += other.getPosition().x();
                cohY += other.getPosition().y();

                count++;
            }
        }

        if (count == 0) return Vec2.ZERO;

        float inv = 1f / count;
        float totalX = sepX * inv * separationWeight
                + (alignX * inv - bvx) * alignmentWeight
                + (cohX * inv - bx) * cohesionWeight;
        float totalY = sepY * inv * separationWeight
                + (alignY * inv - bvy) * alignmentWeight
                + (cohY * inv - by) * cohesionWeight;

        return new Vec2(totalX, totalY);
    }

    /**
     * Cohesion force toward the center of mass of neighbors within squared radius.
     * Used for predator AI chasing boids.
     */
    public static Vec2 cohesionSquaredRadius(Boid boid, List<Boid> neighbors, float perceptionRadiusSq) {
        float bx = boid.getPosition().x();
        float by = boid.getPosition().y();
        float cohX = 0, cohY = 0;
        int count = 0;

        for (int n = 0; n < neighbors.size(); n++) {
            Boid other = neighbors.get(n);
            if (other == boid) continue;
            float dx = bx - other.getPosition().x();
            float dy = by - other.getPosition().y();
            float d2 = dx * dx + dy * dy;
            if (d2 > 0 && d2 < perceptionRadiusSq) {
                cohX += other.getPosition().x();
                cohY += other.getPosition().y();
                count++;
            }
        }

        if (count == 0) return Vec2.ZERO;
        float inv = 1f / count;
        return new Vec2(cohX * inv - bx, cohY * inv - by);
    }

    public static Vec2 avoidObstacles(Boid boid, List<Obstacle> obstacles, float avoidRadius) {
        float totalX = 0, totalY = 0;
        for (int i = 0; i < obstacles.size(); i++) {
            Obstacle obs = obstacles.get(i);
            float dx = boid.getPosition().x() - obs.center().x();
            float dy = boid.getPosition().y() - obs.center().y();
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d < avoidRadius + obs.radius() && d > 0) {
                float force = (avoidRadius + obs.radius()) - d;
                float scale = force * 5f / d;
                totalX += dx * scale;
                totalY += dy * scale;
            }
        }
        return new Vec2(totalX, totalY);
    }

    public static Vec2 fleePredators(Boid boid, List<Predator> predators, float perceptionRadius) {
        float perceptionRadiusSq = perceptionRadius * perceptionRadius;
        float totalX = 0, totalY = 0;
        for (int i = 0; i < predators.size(); i++) {
            Predator p = predators.get(i);
            float dx = boid.getPosition().x() - p.getPosition().x();
            float dy = boid.getPosition().y() - p.getPosition().y();
            float d2 = dx * dx + dy * dy;
            if (d2 > 0 && d2 < perceptionRadiusSq) {
                float d = (float) Math.sqrt(d2);
                totalX += dx / d;
                totalY += dy / d;
            }
        }
        return new Vec2(totalX, totalY);
    }

    public static Vec2 seekAttractors(Boid boid, List<Attractor> attractors, float perceptionRadius) {
        float perceptionRadiusSq = perceptionRadius * perceptionRadius;
        float totalX = 0, totalY = 0;
        for (int i = 0; i < attractors.size(); i++) {
            Attractor a = attractors.get(i);
            float dx = a.position().x() - boid.getPosition().x();
            float dy = a.position().y() - boid.getPosition().y();
            float d2 = dx * dx + dy * dy;
            if (d2 < perceptionRadiusSq) {
                float d = (float) Math.sqrt(d2);
                if (d > 0) {
                    totalX += (dx / d) * a.strength();
                    totalY += (dy / d) * a.strength();
                }
            }
        }
        return new Vec2(totalX, totalY);
    }

    // ---- Compatibility wrappers (used by tests and non-hot code) ----

    public static Vec2 separation(Boid boid, List<Boid> neighbors, float perceptionRadius) {
        return separationSquaredRadius(boid, neighbors, perceptionRadius * perceptionRadius);
    }

    public static Vec2 separationSquaredRadius(Boid boid, List<Boid> neighbors, float perceptionRadiusSq) {
        float bx = boid.getPosition().x();
        float by = boid.getPosition().y();
        float sepX = 0, sepY = 0;
        int count = 0;
        for (int n = 0; n < neighbors.size(); n++) {
            Boid other = neighbors.get(n);
            if (other == boid) continue;
            float dx = bx - other.getPosition().x();
            float dy = by - other.getPosition().y();
            float d2 = dx * dx + dy * dy;
            if (d2 > 0 && d2 < perceptionRadiusSq) {
                sepX += dx * (500f / d2);
                sepY += dy * (500f / d2);
                count++;
            }
        }
        if (count > 0) { sepX /= count; sepY /= count; }
        return new Vec2(sepX, sepY);
    }

    public static Vec2 alignment(Boid boid, List<Boid> neighbors, float perceptionRadius) {
        return alignmentSquaredRadius(boid, neighbors, perceptionRadius * perceptionRadius);
    }

    public static Vec2 alignmentSquaredRadius(Boid boid, List<Boid> neighbors, float perceptionRadiusSq) {
        float alignX = 0, alignY = 0;
        int count = 0;
        float bx = boid.getPosition().x();
        float by = boid.getPosition().y();
        for (int n = 0; n < neighbors.size(); n++) {
            Boid other = neighbors.get(n);
            if (other == boid) continue;
            float dx = bx - other.getPosition().x();
            float dy = by - other.getPosition().y();
            float d2 = dx * dx + dy * dy;
            if (d2 > 0 && d2 < perceptionRadiusSq) {
                alignX += other.getVelocity().x();
                alignY += other.getVelocity().y();
                count++;
            }
        }
        if (count > 0) {
            return new Vec2(alignX / count - boid.getVelocity().x(),
                    alignY / count - boid.getVelocity().y());
        }
        return Vec2.ZERO;
    }

    public static Vec2 cohesion(Boid boid, List<Boid> neighbors, float perceptionRadius) {
        return cohesionSquaredRadius(boid, neighbors, perceptionRadius * perceptionRadius);
    }
}
