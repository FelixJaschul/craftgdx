package io.github.some_example_name.terrain;

import io.github.some_example_name.voxel.BlockType;
import java.util.Random;

public class Generation {
    private static final int SEED = 42;
    private static final float TERRAIN_SCALE = 0.01f;
    private static final float AMPLITUDE = 50.0f;
    private static final int BASE_HEIGHT = 20;
    private static final float MOUNTAIN_THRESHOLD = 0f;
    private static final float GRASS_HEIGHT_THRESHOLD = 0.3f; // Define a specific threshold for grass blocks
    private static final float WATER_LEVEL = 2.0f;
    private static final float LAKE_THRESHOLD = 0f;
    private static final float LAKE_FREQUENCY = 0.00001f;

    private final PerlinNoise perlinNoise;

    public Generation() {
        this(SEED);
    }

    public Generation(int seed) {
        new Random(seed);
        this.perlinNoise = new PerlinNoise(seed);
    }

    public void fillChunkWithTerrain(BlockType[][][] blocks, int[][] heightMap, int chunkHeight) {
        int chunkSize = heightMap.length;
        boolean[][] isLake = new boolean[chunkSize][chunkSize];

        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                if (heightMap[x][z] < WATER_LEVEL) isLake[x][z] = true;
            }
        }

        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                int height = Math.min(heightMap[x][z], chunkHeight - 1);

                for (int y = 0; y < chunkHeight; y++) {
                    if (y > height) {
                        if (y <= WATER_LEVEL && isLake[x][z]) blocks[x][y][z] = BlockType.WATER;
                        else blocks[x][y][z] = BlockType.AIR;
                    } else {
                        if (heightMap[x][z] > GRASS_HEIGHT_THRESHOLD) {
                            blocks[x][y][z] = BlockType.GRASS;
                        } else {
                            blocks[x][y][z] = BlockType.DIRT;
                        }
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

    private static int getHeight(float biomeNoise, float noiseValue, float lakeNoise) {
        int height = BASE_HEIGHT;

        if (biomeNoise > MOUNTAIN_THRESHOLD) height += (int)(AMPLITUDE * 1.5f * noiseValue);
        else height += (int) (AMPLITUDE * 0.7f * noiseValue);

        if (lakeNoise > LAKE_THRESHOLD && height > WATER_LEVEL - 3 && height < WATER_LEVEL + 5) height = (int) (WATER_LEVEL - 1);

        height = Math.max(1, height);
        return height;
    }

    public int[][] smoothTerrain(int[][] heightMap, int iterations) {
        int size = heightMap.length;
        int[][] result = new int[size][size];

        for (int x = 0; x < size; x++) System.arraycopy(heightMap[x], 0, result[x], 0, size);

        for (int iter = 0; iter < iterations; iter++) {
            int[][] temp = new int[size][size];

            for (int i = 0; i < size; i++) {
                temp[0][i] = result[0][i];
                temp[size-1][i] = result[size-1][i];
                temp[i][0] = result[i][0];
                temp[i][size-1] = result[i][size-1];
            }

            for (int x = 1; x < size-1; x++) {
                for (int z = 1; z < size-1; z++) {
                    int sum = result[x-1][z-1] + result[x-1][z] + result[x-1][z+1] +
                        result[x][z-1]   + result[x][z]   + result[x][z+1] +
                        result[x+1][z-1] + result[x+1][z] + result[x+1][z+1];
                    temp[x][z] = sum / 9;
                }
            }

            for (int x = 0; x < size; x++) System.arraycopy(temp[x], 0, result[x], 0, size);
        }

        return result;
    }

    private static class PerlinNoise {
        private final int[] permutation;

        public PerlinNoise(int seed) {
            Random random = new Random(seed);
            permutation = new int[512];
            for (int i = 0; i < 256; i++) permutation[i] = i;
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
            int h = hash & 15;
            double u = h < 8 ? x : y;
            double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
            return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
        }

        public double noise(double x, double y) {
            return noise(x, y, 0);
        }

        public double noise(double x, double y, double z) {
            int X = (int) Math.floor(x) & 255;
            int Y = (int) Math.floor(y) & 255;
            int Z = (int) Math.floor(z) & 255;

            x -= Math.floor(x);
            y -= Math.floor(y);
            z -= Math.floor(z);

            double u = fade(x);
            double v = fade(y);
            double w = fade(z);

            int A = permutation[X] + Y;
            int AA = permutation[A] + Z;
            int AB = permutation[A + 1] + Z;
            int B = permutation[X + 1] + Y;
            int BA = permutation[B] + Z;
            int BB = permutation[B + 1] + Z;

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
