package io.github.some_example_name.voxel;

/**
 * Represents the position of a chunk in the world.
 */
public class ChunkPosition {
    public final int x;
    public final int z;

    private static final int HASH_MULTIPLIER = 31;

    public ChunkPosition(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChunkPosition other = (ChunkPosition) obj;
        return x == other.x && z == other.z;
    }

    @Override
    public int hashCode() {
        return HASH_MULTIPLIER * x + z;
    }

    @Override
    public String toString() {
        return "ChunkPosition{x=" + x + ", z=" + z + '}';
    }
}
