# ChunkPosition.java Documentation

The `ChunkPosition` class is a simple utility class that stores the coordinates of a chunk in the voxel world. It's designed to be used as a key in hash maps and collections that store chunks.

## Class Variables
- `x`: The x-coordinate of the chunk in the world grid
- `z`: The z-coordinate of the chunk in the world grid

## Methods

### Constructor
- `ChunkPosition(int x, int z)`: Creates a new chunk position with the specified coordinates

### equals(Object obj)
Overrides the default equals method to compare chunk positions based on their x and z coordinates. This is essential for using ChunkPosition as a key in maps.

### hashCode()
Generates a hash code based on the x and z coordinates, ensuring that equal positions have the same hash code. This is required for efficient lookups in hash-based collections.

### toString()
Returns a string representation of the chunk position for debugging purposes.

## Usage
The ChunkPosition class is primarily used in the VoxelEngine to:
1. Index chunks in the chunk map
2. Track which chunks are currently visible or being loaded
3. Efficiently look up chunks by their position in the world# ChunkPosition.java Documentation

The `ChunkPosition` class represents the coordinates of a chunk in the voxel world, serving as a key in the chunks map.

## Class Variables
- `x`: The x-coordinate of the chunk in the world grid
- `z`: The z-coordinate of the chunk in the world grid

## Methods

### Constructor
- `ChunkPosition(int x, int z)`: Creates a new ChunkPosition with the specified coordinates

### equals(Object obj)
Compares this ChunkPosition with another object:
- Returns true if the object is a ChunkPosition with the same x and z coordinates
- Used for map lookups when managing chunks

### hashCode()
Generates a hash code for this ChunkPosition:
- Combines the x and z coordinates to create a unique hash
- Essential for efficient storage and retrieval in HashMap collections

### toString()
Returns a string representation of the ChunkPosition:
- Format: "ChunkPosition{x=X, z=Z}"
- Useful for debugging and logging