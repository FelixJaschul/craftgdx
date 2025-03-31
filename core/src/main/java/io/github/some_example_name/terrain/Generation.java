package io.github.some_example_name.terrain;

import io.github.some_example_name.voxel.BlockType;
import java.util.Random;

/**
 * Handles procedural terrain generation using Perlin noise.
 * This class creates height maps and fills chunks with appropriate block types
 * based on terrain features like mountains, plains, and lakes.
 */
public class Generation {
    /** Default seed value for terrain generation */
    private static final int SEED = 42;
    /** Controls the overall scale of terrain features */
    private static final float TERRAIN_SCALE = 0.01f;
    /** Controls the height variation of terrain */
    private static final float AMPLITUDE = 50.0f;
    /** The base height level for terrain */
    private static final int BASE_HEIGHT = 20;
    /** Threshold for mountain generation */
    private static final float MOUNTAIN_THRESHOLD = 0f;
    /** Height threshold for grass block placement */
    private static final float GRASS_HEIGHT_THRESHOLD = 0.3f;
    /** Height level for water */
    private static final float WATER_LEVEL = 2.0f;
    /** Threshold for lake generation */
    private static final float LAKE_THRESHOLD = 0f;
    /** Controls the frequency of lakes in the terrain */
    private static final float LAKE_FREQUENCY = 0.00001f;

    /** Perlin noise generator for terrain features */
    private final PerlinNoise perlinNoise;

    /**
     * Creates a terrain generator with the default seed.
     */
    public Generation() {
        this(SEED);
    }

    /**
     * Creates a terrain generator with a specified seed.
     *
     * @param seed The seed value for random terrain generation
     */
    public Generation(int seed) {
        new Random(seed);
        this.perlinNoise = new PerlinNoise(seed);
    }

    /**
     * Fills a chunk with appropriate block types based on the height map.
     *
     * @param blocks 3D array to be filled with block types
     * @param heightMap 2D array containing terrain height values
     * @param chunkHeight The maximum height of the chunk
     */
    public void fillChunkWithTerrain(BlockType[][][] blocks, int[][] heightMap, int chunkHeight) {
        int chunkSize = heightMap.length;
        boolean[][] isLake = new boolean[chunkSize][chunkSize];

        // Identify lake areas based on height being below water level
        for (int x = 0; x < chunkSize; x++)
            for (int z = 0; z < chunkSize; z++)
                if (heightMap[x][z] < WATER_LEVEL) isLake[x][z] = true;

        // Fill the chunk with appropriate block types
        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                int height = Math.min(heightMap[x][z], chunkHeight - 1);

                for (int y = 0; y < chunkHeight; y++) {
                    if (y > height) {
                        // Above terrain surface
                        if (y <= WATER_LEVEL && isLake[x][z]) blocks[x][y][z] = BlockType.WATER;
                        else blocks[x][y][z] = BlockType.AIR;
                    } else {
                        // Below or at terrain surface
                        if (heightMap[x][z] > GRASS_HEIGHT_THRESHOLD) blocks[x][y][z] = BlockType.GRASS;
                        else blocks[x][y][z] = BlockType.DIRT;
                    }
                }
            }
        }
    }

    /**
     * Generates a height map for a chunk at the specified coordinates.
     *
     * @param chunkX The X coordinate of the chunk in chunk space
     * @param chunkZ The Z coordinate of the chunk in chunk space
     * @param chunkSize The size of the chunk (width and depth)
     * @return A 2D array containing height values for the terrain
     */
    public int[][] generateHeightMap(int chunkX, int chunkZ, int chunkSize) {
        int[][] heightMap = new int[chunkSize][chunkSize];

        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                float worldX = (chunkX * chunkSize) + x;
                float worldZ = (chunkZ * chunkSize) + z;

                // Generate different layers of noise for varied terrain
                float baseNoise = (float) perlinNoise.noise(worldX * TERRAIN_SCALE, worldZ * TERRAIN_SCALE);
                float detailNoise = (float) perlinNoise.noise(worldX * TERRAIN_SCALE * 2, worldZ * TERRAIN_SCALE * 2) * 0.5f;
                float microDetail = (float) perlinNoise.noise(worldX * TERRAIN_SCALE * 4, worldZ * TERRAIN_SCALE * 4) * 0.25f;

                float noiseValue = (baseNoise + detailNoise + microDetail) * 0.57f;
                float biomeNoise = (float) perlinNoise.noise(worldX * TERRAIN_SCALE * 0.5f, worldZ * TERRAIN_SCALE * 0.5f);
                float lakeNoise = (float) perlinNoise.noise(worldX * LAKE_FREQUENCY, worldZ * LAKE_FREQUENCY);

                int height = getHeight(biomeNoise, noiseValue, lakeNoise);
                heightMap[x][z] = height;
            }
        }

        return smoothTerrain(heightMap, 1);
    }

    /**
     * Calculates the terrain height based on various noise values.
     *
     * @param biomeNoise Noise value determining the biome type
     * @param noiseValue Combined noise value for terrain variation
     * @param lakeNoise Noise value for lake generation
     * @return The calculated height value for the terrain
     */
    private static int getHeight(float biomeNoise, float noiseValue, float lakeNoise) {
        int height = BASE_HEIGHT;

        // Apply different amplitude based on biome type (mountains vs plains)
        if (biomeNoise > MOUNTAIN_THRESHOLD) height += (int)(AMPLITUDE * 1.5f * noiseValue);
        else height += (int) (AMPLITUDE * 0.7f * noiseValue);

        // Create lakes by flattening certain areas below water level
        if (lakeNoise > LAKE_THRESHOLD && height > WATER_LEVEL - 3 && height < WATER_LEVEL + 5) height = (int) (WATER_LEVEL - 1);

        height = Math.max(1, height);
        return height;
    }

    /**
     * Smooths the terrain by averaging neighboring height values.
     *
     * @param heightMap The original height map to smooth
     * @param iterations Number of smoothing passes to perform
     * @return A new smoothed height map
     */
    public int[][] smoothTerrain(int[][] heightMap, int iterations) {
        int size = heightMap.length;
        int[][] result = new int[size][size];

        // Copy the original height map
        for (int x = 0; x < size; x++) System.arraycopy(heightMap[x], 0, result[x], 0, size);

        // Perform multiple smoothing iterations
        for (int iter = 0; iter < iterations; iter++) {
            int[][] temp = new int[size][size];

            // Preserve the edges
            for (int i = 0; i < size; i++) {
                temp[0][i] = result[0][i];
                temp[size-1][i] = result[size-1][i];
                temp[i][0] = result[i][0];
                temp[i][size-1] = result[i][size-1];
            }

            // Smooth the interior by averaging 3x3 neighborhoods
            for (int x = 1; x < size-1; x++) {
                for (int z = 1; z < size-1; z++) {
                    int sum = result[x-1][z-1] + result[x-1][z] + result[x-1][z+1] +
                        result[x][z-1]   + result[x][z]   + result[x][z+1] +
                        result[x+1][z-1] + result[x+1][z] + result[x+1][z+1];
                    temp[x][z] = sum / 9;
                }
            }

            // Update the result with the smoothed values
            for (int x = 0; x < size; x++) System.arraycopy(temp[x], 0, result[x], 0, size);
        }

        return result;
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
