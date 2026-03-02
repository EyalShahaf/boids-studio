package com.flocklab.sim;

import com.flocklab.model.Boid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spatial partitioning grid for fast neighboring boid lookup.
 * Reused across frames — call clear() then insert all boids each frame.
 */
public class SpatialGrid {
    private final float cellSize;
    private final Map<Integer, List<Boid>> grid = new HashMap<>();
    private final int cols;
    private final int rows;

    public SpatialGrid(float cellSize, float worldWidth, float worldHeight) {
        this.cellSize = cellSize;
        this.cols = (int) Math.ceil(worldWidth / cellSize);
        this.rows = (int) Math.ceil(worldHeight / cellSize);
    }

    public float getCellSize() {
        return cellSize;
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public void clear() {
        grid.clear();
    }

    public void insert(Boid boid) {
        int cellHash = getCellHash(boid.getPosition().x(), boid.getPosition().y());
        grid.computeIfAbsent(cellHash, k -> new ArrayList<>()).add(boid);
    }

    /**
     * Clears neighborsOut then populates it with all boids in the 3×3 cell
     * neighborhood surrounding the given boid.
     */
    public void getNeighborsInto(Boid boid, float radius, List<Boid> neighborsOut) {
        neighborsOut.clear();
        int cellX = (int) (boid.getPosition().x() / cellSize);
        int cellY = (int) (boid.getPosition().y() / cellSize);

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int hash = wrapCellX(cellX + i) + wrapCellY(cellY + j) * cols;
                List<Boid> cellBoids = grid.get(hash);
                if (cellBoids != null) {
                    neighborsOut.addAll(cellBoids);
                }
            }
        }
    }

    /** Convenience wrapper that allocates a new list. Prefer getNeighborsInto for hot paths. */
    public List<Boid> getNeighbors(Boid boid, float radius) {
        List<Boid> neighbors = new ArrayList<>();
        getNeighborsInto(boid, radius, neighbors);
        return neighbors;
    }

    private int getCellHash(float x, float y) {
        return clampCellX((int) (x / cellSize)) + clampCellY((int) (y / cellSize)) * cols;
    }

    private int wrapCellX(int x) {
        if (x < 0) return x + cols;
        if (x >= cols) return x - cols;
        return x;
    }

    private int wrapCellY(int y) {
        if (y < 0) return y + rows;
        if (y >= rows) return y - rows;
        return y;
    }

    private int clampCellX(int x) {
        if (x < 0) return 0;
        if (x >= cols) return cols - 1;
        return x;
    }

    private int clampCellY(int y) {
        if (y < 0) return 0;
        if (y >= rows) return rows - 1;
        return y;
    }
}
