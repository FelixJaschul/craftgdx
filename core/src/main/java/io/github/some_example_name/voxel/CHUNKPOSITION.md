# ChunkPosition.java Documentation

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