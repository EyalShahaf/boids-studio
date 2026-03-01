package com.flocklab.sim;

import com.flocklab.model.Boid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spatial partitioning grid for fast O(1) neighboring boid lookup.
 * Rebuilt every frame to avoid O(N^2) checks for large boid counts.
 */
public class SpatialGrid {
    private final float cellSize;
    private final Map<Integer, List<Boid>> grid = new HashMap<>();
    private final float worldWidth;
    private final float worldHeight;
    private final int cols;

    public SpatialGrid(float cellSize, float worldWidth, float worldHeight) {
        this.cellSize = cellSize;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.cols = (int) Math.ceil(worldWidth / cellSize);
    }

    public void clear() {
        grid.clear();
    }

    public void insert(Boid boid) {
        int cellHash = getCellHash(boid.getPosition().x(), boid.getPosition().y());
        grid.computeIfAbsent(cellHash, k -> new ArrayList<>()).add(boid);
    }

    public List<Boid> getNeighbors(Boid boid, float radius) {
        List<Boid> neighbors = new ArrayList<>();
        int cellX = (int) (boid.getPosition().x() / cellSize);
        int cellY = (int) (boid.getPosition().y() / cellSize);

        // Search 3x3 surrounding cells to cover the radius
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                // Handle edge wrapping for spatial queries
                int checkX = cellX + i;
                int checkY = cellY + j;

                // Wrap logic for grid bounds
                if (checkX < 0)
                    checkX += cols;
                else if (checkX >= cols)
                    checkX -= cols;

                int rows = (int) Math.ceil(worldHeight / cellSize);
                if (checkY < 0)
                    checkY += rows;
                else if (checkY >= rows)
                    checkY -= rows;

                int hash = checkX + checkY * cols;
                List<Boid> cellBoids = grid.get(hash);
                if (cellBoids != null) {
                    neighbors.addAll(cellBoids);
                }
            }
        }
        return neighbors;
    }

    private int getCellHash(float x, float y) {
        int cx = (int) (x / cellSize);
        int cy = (int) (y / cellSize);

        // Clamp bounds just in case wrapping hasn't occurred exactly
        if (cx < 0)
            cx = 0;
        else if (cx >= cols)
            cx = cols - 1;

        int rows = (int) Math.ceil(worldHeight / cellSize);
        if (cy < 0)
            cy = 0;
        else if (cy >= rows)
            cy = rows - 1;

        return cx + cy * cols;
    }
}
