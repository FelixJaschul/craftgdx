# VoxelEngine.java Documentation

The `VoxelEngine` class manages the entire voxel world, including chunk creation, management, and rendering.

## Class Variables
- `chunks`: Map storing all chunks indexed by their position (using ChunkPosition as keys)
- `worldSize`: Size of the world in chunks
- `renderDistance`: How far from the player chunks should be rendered
- `chunkLoadExecutor`: Thread pool for asynchronous chunk loading
- `loadingChunks`: Set of chunks currently being loaded

## Methods

### Constructor
Initializes the chunk collection and thread pool

### init(int worldSize, int renderDistance)
Sets up the voxel world:
- Stores world size and render distance
- Creates chunks for the entire world grid
- Only generates terrain data initially, not meshes
- Adds all chunks to the chunks map

### render(ModelBatch modelBatch, Environment environment)
Renders the visible chunks:
- Calculates which chunk the camera is in
- Renders only chunks within the render distance of the camera
- Asynchronously builds meshes for chunks that need them
- Uses frustum culling to skip chunks that are not visible
- Unloads meshes for chunks that are no longer visible

### isWithinDistance(ChunkPosition pos, int camX, int camZ, int distance)
Helper method to determine if a chunk is within a certain distance of the camera

### dispose()
Cleans up resources when the game closes:
- Shuts down the thread pool
- Disposes all chunks
- Clears the chunks collection

## Performance Optimizations
1. **Asynchronous Chunk Loading**: Uses a thread pool to build chunk meshes without blocking the main thread
2. **Dynamic Mesh Management**: Only builds meshes for chunks that are visible to the camera
3. **Mesh Unloading**: Disposes meshes for chunks that are no longer visible to save memory
4. **Buffer Zone**: Uses a buffer zone around the visible area to prevent frequent loading/unloading when moving
5. **Efficient Chunk Indexing**: Uses ChunkPosition objects as keys for O(1) chunk lookups
