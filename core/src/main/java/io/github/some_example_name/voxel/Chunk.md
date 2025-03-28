# VoxelEngine.java Documentation

The `VoxelEngine` class manages the entire voxel world, including chunk creation, management, and rendering.

## Class Variables
- `chunks`: Map storing all chunks indexed by their position
- `worldSize`: Size of the world in chunks
- `renderDistance`: How far from the player chunks should be rendered

## Inner Class
- `ChunkPosition`: Helper class for storing and comparing chunk coordinates

## Methods

### Constructor
Initializes the chunk collection

### init(int worldSize, int renderDistance)
Sets up the voxel world:
- Stores world size and render distance
- Creates chunks for the entire world grid
- Adds all chunks to the chunks map

### render(ModelBatch modelBatch, Environment environment)
Renders the visible chunks:
- Calculates which chunk the camera is in
- Renders only chunks within the render distance of the camera
- Skips chunks that are too far away

### getWorldSize()
Returns the size of the world

### dispose()
Cleans up resources when the game closes:
- Disposes all chunks
- Clears the chunks collection

### ChunkPosition Class
- Stores x and z coordinates for a chunk
- Implements equals() and hashCode() for use as a map key
