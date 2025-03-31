package io.github.some_example_name.terrain;

import io.github.some_example_name.voxel.BlockType;
import java.util.Random;

/**
 * Handles procedural terrain generation using Perlin noise.
 * This class creates height maps and fills chunks with appropriate block types
 * based on terrain features like mountains, plains, and lakes.
 */
public class Generation {
    private static final int SEED = 42;
    private static final float TERRAIN_SCALE = 0.01f;
    private static final float AMPLITUDE = 50.0f;
    private static final int BASE_HEIGHT = 20;
    private static final float MOUNTAIN_THRESHOLD = 0f;
    private static final float GRASS_HEIGHT_THRESHOLD = 0.3f;
    private static final float WATER_LEVEL = 2.0f;
    private static final float LAKE_THRESHOLD = 0f;
    private static final float LAKE_FREQUENCY = 0.00001f;
    private static final int LAKE_DEPTH = 1;
    private static final int MIN_HEIGHT = 1;
    private static final float MOUNTAIN_AMPLITUDE_MULTIPLIER = 1.5f;
    private static final float PLAINS_AMPLITUDE_MULTIPLIER = 0.7f;
    private static final float NOISE_NORMALIZATION = 0.57f;
    private static final int NEIGHBORHOOD_SIZE = 9; // 3x3 grid

    private final PerlinNoise perlinNoise;

    public Generation() {
        this(SEED);
    }

    public Generation(int seed) {
        this.perlinNoise = new PerlinNoise(seed);
    }

    public void fillChunkWithTerrain(BlockType[][][] blocks, int[][] heightMap, int chunkHeight) {
        int chunkSize = heightMap.length;
        boolean[][] isLake = identifyLakes(heightMap, chunkSize);
        fillBlocks(blocks, heightMap, isLake, chunkSize, chunkHeight);
    }

    private boolean[][] identifyLakes(int[][] heightMap, int chunkSize) {
        boolean[][] isLake = new boolean[chunkSize][chunkSize];

        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                if (heightMap[x][z] < WATER_LEVEL) {
                    isLake[x][z] = true;
                }
            }
        }

        return isLake;
    }

    private void fillBlocks(BlockType[][][] blocks, int[][] heightMap, boolean[][] isLake, int chunkSize, int chunkHeight) {
        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                int height = Math.min(heightMap[x][z], chunkHeight - 1);

                for (int y = 0; y < chunkHeight; y++) {
                    if (y > height) {
                        // Above terrain surface
                        blocks[x][y][z] = (y <= WATER_LEVEL && isLake[x][z]) ? BlockType.WATER : BlockType.AIR;
                    } else {
                        // Below or at terrain surface
                        blocks[x][y][z] = BlockType.DIRT;
                    }
                }
            }
        }
    }

    public int[][] generateHeightMap(int chunkX, int chunkZ, int chunkSize) {
        int[][] heightMap = new int[chunkSize][chunkSize];

        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                float worldX = (chunkX * chunkSize) + x;
                float worldZ = (chunkZ * chunkSize) + z;

                float noiseValue = calculateNoiseValue(worldX, worldZ);
                float biomeNoise = (float) perlinNoise.noise(worldX * TERRAIN_SCALE * 0.5f, worldZ * TERRAIN_SCALE * 0.5f);
                float lakeNoise = (float) perlinNoise.noise(worldX * LAKE_FREQUENCY, worldZ * LAKE_FREQUENCY);

                heightMap[x][z] = getHeight(biomeNoise, noiseValue, lakeNoise);
            }
        }

        return smoothTerrain(heightMap, 1);
    }

    private float calculateNoiseValue(float worldX, float worldZ) {
        float baseNoise = (float) perlinNoise.noise(worldX * TERRAIN_SCALE, worldZ * TERRAIN_SCALE);
        float detailNoise = (float) perlinNoise.noise(worldX * TERRAIN_SCALE * 2, worldZ * TERRAIN_SCALE * 2) * 0.5f;
        float microDetail = (float) perlinNoise.noise(worldX * TERRAIN_SCALE * 4, worldZ * TERRAIN_SCALE * 4) * 0.25f;

        return (baseNoise + detailNoise + microDetail) * NOISE_NORMALIZATION;
    }

    private static int getHeight(float biomeNoise, float noiseValue, float lakeNoise) {
        int height = BASE_HEIGHT;

        // Apply different amplitude based on biome type
        float amplitudeMultiplier = (biomeNoise > MOUNTAIN_THRESHOLD) ?
                                    MOUNTAIN_AMPLITUDE_MULTIPLIER :
                                    PLAINS_AMPLITUDE_MULTIPLIER;
        height += (int)(AMPLITUDE * amplitudeMultiplier * noiseValue);

        // Create lakes
        boolean isLakeCandidate = lakeNoise > LAKE_THRESHOLD &&
                                 height > WATER_LEVEL - 3 &&
                                 height < WATER_LEVEL + 5;

        if (isLakeCandidate) {
            height = (int)(WATER_LEVEL - LAKE_DEPTH);
        }

        return Math.max(MIN_HEIGHT, height);
    }

    public int[][] smoothTerrain(int[][] heightMap, int iterations) {
        int size = heightMap.length;
        int[][] result = copyHeightMap(heightMap, size);

        for (int iter = 0; iter < iterations; iter++) {
            result = performSmoothingIteration(result, size);
        }

        return result;
    }

    private int[][] copyHeightMap(int[][] heightMap, int size) {
        int[][] result = new int[size][size];
        for (int x = 0; x < size; x++) {
            System.arraycopy(heightMap[x], 0, result[x], 0, size);
        }
        return result;
    }

    private int[][] performSmoothingIteration(int[][] heightMap, int size) {
        int[][] temp = new int[size][size];

        // Preserve the edges
        preserveEdges(heightMap, temp, size);

        // Smooth the interior
        smoothInterior(heightMap, temp, size);

        return temp;
    }

    private void preserveEdges(int[][] source, int[][] target, int size) {
        for (int i = 0; i < size; i++) {
            target[0][i] = source[0][i];
            target[size-1][i] = source[size-1][i];
            target[i][0] = source[i][0];
            target[i][size-1] = source[i][size-1];
        }
    }

    private void smoothInterior(int[][] source, int[][] target, int size) {
        for (int x = 1; x < size-1; x++) {
            for (int z = 1; z < size-1; z++) {
                int sum = source[x-1][z-1] + source[x-1][z] + source[x-1][z+1] +
                          source[x][z-1]   + source[x][z]   + source[x][z+1] +
                          source[x+1][z-1] + source[x+1][z] + source[x+1][z+1];
                target[x][z] = sum / NEIGHBORHOOD_SIZE;
            }
        }
    }

    /**
     * Implementation of Perlin noise algorithm for terrain generation.
     * Based on Ken Perlin's improved noise reference implementation.
     */
    private static class PerlinNoise {
        /** Permutation table for noise calculation */
        private final int[] permutation;

        /**
         * Creates a Perlin noise generator with the specified seed.
         *
         * @param seed The seed value for random permutation generation
         */
        public PerlinNoise(int seed) {
            Random random = new Random(seed);
            permutation = new int[512];

            // Initialize and shuffle the permutation table
            for (int i = 0; i < 256; i++) permutation[i] = i;
            for (int i = 0; i < 256; i++) {
                int j = random.nextInt(256);
                int temp = permutation[i];
                permutation[i] = permutation[j];
                permutation[j] = temp;
            }
            // Duplicate the permutation table to avoid overflow
            for (int i = 0; i < 256; i++) permutation[i + 256] = permutation[i];
        }

        /**
         * Fade function for Perlin noise to smooth interpolation.
         *
         * @param t Value to apply the fade function to
         * @return The faded value
         */
        private double fade(double t) {
            return t * t * t * (t * (t * 6 - 15) + 10);
        }

        /**
         * Linear interpolation between two values.
         *
         * @param t Interpolation factor (0.0 to 1.0)
         * @param a First value
         * @param b Second value
         * @return The interpolated value
         */
        private double lerp(double t, double a, double b) {
            return a + t * (b - a);
        }

        /**
         * Gradient function for Perlin noise.
         *
         * @param hash Hash value to determine the gradient
         * @param x X coordinate
         * @param y Y coordinate
         * @param z Z coordinate
         * @return The dot product of the gradient and (x,y,z)
         */
        private double grad(int hash, double x, double y, double z) {
            int h = hash & 15;
            double u = h < 8 ? x : y;
            double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
            return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
        }

        /**
         * Generates 2D Perlin noise at the specified coordinates.
         *
         * @param x X coordinate
         * @param y Y coordinate
         * @return Noise value in the range [-1,1]
         */
        public double noise(double x, double y) {
            return noise(x, y, 0);
        }

        /**
         * Generates 3D Perlin noise at the specified coordinates.
         *
         * @param x X coordinate
         * @param y Y coordinate
         * @param z Z coordinate
         * @return Noise value in the range [-1,1]
         */
        public double noise(double x, double y, double z) {
            int X = (int) Math.floor(x) & 255;
            int Y = (int) Math.floor(y) & 255;
            int Z = (int) Math.floor(z) & 255;

            // Find relative x, y, z of point in cube
            x -= Math.floor(x);
            y -= Math.floor(y);
            z -= Math.floor(z);

            // Compute fade curves for x, y, z
            double u = fade(x);
            double v = fade(y);
            double w = fade(z);

            // Hash coordinates of the 8 cube corners
            int A = permutation[X] + Y;
            int AA = permutation[A] + Z;
            int AB = permutation[A + 1] + Z;
            int B = permutation[X + 1] + Y;
            int BA = permutation[B] + Z;
            int BB = permutation[B + 1] + Z;

            // Blend results from 8 corners of cube
            return lerp(w, lerp(v, lerp(u, grad(permutation[AA], x, y, z),
                        grad(permutation[BA], x - 1, y, z)),
                    lerp(u, grad(permutation[AB], x, y - 1, z),
                        grad(permutation[BB], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad(permutation[AA + 1], x, y, z - 1),
                        grad(permutation[BA + 1], x - 1, y, z - 1)),
                    lerp(u, grad(permutation[AB + 1], x, y - 1, z - 1),
                        grad(permutation[BB + 1], x - 1, y - 1, z - 1))));
        }
    }
}
