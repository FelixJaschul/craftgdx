package io.github.some_example_name.voxel;

/**
 * Represents the position of a chunk in the world.
 * Used as a key in collections to identify and look up chunks.
 */
public class ChunkPosition {
    /** X-coordinate of the chunk in chunk space */
    public final int x;
    /** Z-coordinate of the chunk in chunk space */
    public final int z;

    /**
     * Creates a new chunk position with the specified coordinates.
     *
     * @param x The X-coordinate of the chunk in chunk space
     * @param z The Z-coordinate of the chunk in chunk space
     */
    public ChunkPosition(int x, int z) {
        this.x = x;
        this.z = z;
    }

    /**
     * Checks if this chunk position is equal to another object.
     * Two chunk positions are equal if they have the same x and z coordinates.
     *
     * @param obj The object to compare with
     * @return True if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChunkPosition other = (ChunkPosition) obj;
        return x == other.x && z == other.z;
    }

    /**
     * Generates a hash code for this chunk position.
     * The hash code is based on the x and z coordinates.
     *
     * @return The hash code
     */
    @Override
    public int hashCode() {
        return 31 * x + z;
    }

    /**
     * Returns a string representation of this chunk position.
     *
     * @return A string in the format "ChunkPosition{x=X, z=Z}"
     */
    @Override
    public String toString() {
        return "ChunkPosition{" + "x=" + x + ", z=" + z + '}';
    }
}
