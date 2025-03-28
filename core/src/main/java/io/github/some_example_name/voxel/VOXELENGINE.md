# VoxelEngine.java Documentation

The `VoxelEngine` class manages the entire voxel world, including chunk creation, management, and rendering.

## Class Variables
- `chunks`: Map storing all chunks indexed by their position (using ChunkPosition as keys)
- `worldSize`: Size of the world in chunks
- `renderDistance`: How far from the player chunks should be rendered
- `camera`: Reference to the camera for frustum culling

## Methods

### Constructor
Initializes the chunk collection

### init(int worldSize, int renderDistance)
Sets up the voxel world:
- Stores world size and render distance
- Creates chunks for the entire world grid
- Adds all chunks to the chunks map

### render(ModelBatch modelBatch, Environment environment, Camera camera)
Renders the visible chunks:
- Calculates which chunk the camera is in
- Renders only chunks within the render distance of the camera
- Uses frustum culling to skip chunks that are not visible
- Skips chunks that are too far away

### getWorldSize()
Returns the size of the world

### dispose()
Cleans up resources when the game closes:
- Disposes all chunks
- Clears the chunks collection
