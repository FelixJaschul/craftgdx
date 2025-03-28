package io.github.some_example_name.terrain;

import io.github.some_example_name.voxel.BlockType;
import java.util.Random;

public class Generation {
    // Constants for noise generation
    private static final int SEED = 42;
    private static final float TERRAIN_SCALE = 0.02f;
    private static final float AMPLITUDE = 20.0f;
    private static final int BASE_HEIGHT = 2;

    // Constants for terrain features
    private static final float MOUNTAIN_THRESHOLD = 0.6f;
    private static final float WATER_LEVEL = 3.0f;

    private final PerlinNoise perlinNoise;

    public Generation() {
        this(SEED);
    }

    public Generation(int seed) {
        new Random(seed);
        this.perlinNoise = new PerlinNoise(seed);
    }

    public int[][] generateHeightMap(int chunkX, int chunkZ, int chunkSize) {
        int[][] heightMap = new int[chunkSize][chunkSize];

        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                // Convert local coordinates to world coordinates
                float worldX = (chunkX * chunkSize) + x;
                float worldZ = (chunkZ * chunkSize) + z;

                // Generate base terrain using Perlin noise
                float noiseValue = (float) perlinNoise.noise(
                        worldX * TERRAIN_SCALE,
                        worldZ * TERRAIN_SCALE);

                // Add some variation with a second octave of noise
                float detailNoise = (float) perlinNoise.noise(
                        worldX * TERRAIN_SCALE * 2,
                        worldZ * TERRAIN_SCALE * 2) * 0.5f;

                // Combine noise values
                noiseValue = (noiseValue + detailNoise) * 0.75f;

                // Calculate height based on noise
                int height = BASE_HEIGHT;

                // Add mountains in areas with high noise values
                if (noiseValue > MOUNTAIN_THRESHOLD) height += (int)(AMPLITUDE * noiseValue * 1.5f);
                else height += (int)(AMPLITUDE * noiseValue);

                height = Math.max(1, height);

                heightMap[x][z] = height;
            }
        }

        return heightMap;
    }

    public void fillChunkWithTerrain(BlockType[][][] blocks, int[][] heightMap, int chunkHeight) {
        int chunkSize = heightMap.length;

        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                int height = Math.min(heightMap[x][z], chunkHeight - 1);

                // Fill blocks based on height
                for (int y = 0; y < chunkHeight; y++) {
                    if (y > height) blocks[x][y][z] = BlockType.AIR;  // Air above the surface
                    else if (y == height) {
                        if (height <= WATER_LEVEL + 1) blocks[x][y][z] = BlockType.GRASS; // Beaches
                        else if (height > WATER_LEVEL + 6) blocks[x][y][z] = BlockType.GRASS; // Mountains
                        else blocks[x][y][z] = BlockType.GRASS; // Normal terrain
                    } else if (y >= height - 3 && y < height) blocks[x][y][z] = BlockType.DIRT; // Layer below surface
                    else blocks[x][y][z] = BlockType.STONE; // Deep underground
                }
            }
        }
    }


    // Perlin Noise Implementation

    private static class PerlinNoise {
        private final int[] permutation;

        public PerlinNoise(int seed) {
            Random random = new Random(seed);
            permutation = new int[512];

            // Initialize the permutation array with values 0-255
            for (int i = 0; i < 256; i++) permutation[i] = i;

            // Shuffle the permutation array
            for (int i = 0; i < 256; i++) {
                int j = random.nextInt(256);
                int temp = permutation[i];
                permutation[i] = permutation[j];
                permutation[j] = temp;
            }

            for (int i = 0; i < 256; i++) permutation[i + 256] = permutation[i];
        }

        private double fade(double t) {
            return t * t * t * (t * (t * 6 - 15) + 10);
        }

        private double lerp(double t, double a, double b) {
            return a + t * (b - a);
        }

        private double grad(int hash, double x, double y, double z) {
            // Convert low 4 bits of hash code into 12 gradient directions
            int h = hash & 15;
            double u = h < 8 ? x : y;
            double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
            return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
        }

        public double noise(double x, double y) {
            return noise(x, y, 0);
        }

        public double noise(double x, double y, double z) {
            // Find unit cube that contains point
            int X = (int) Math.floor(x) & 255;
            int Y = (int) Math.floor(y) & 255;
            int Z = (int) Math.floor(z) & 255;
            // Find relative x y z of point in cube
            x -= Math.floor(x);
            y -= Math.floor(y);
            z -= Math.floor(z);
            // Fade curves for x y z
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
            // Add blended results from 8 corners of cube
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

    // End Implementation
}
