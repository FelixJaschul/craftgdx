# Chunk.java Documentation

The `Chunk` class represents a section of the voxel world containing multiple blocks in a 3D grid.

## Constants
- `CHUNK_SIZE`: Size of the chunk in blocks (default: 16)
- `CHUNK_HEIGHT`: Height of the chunk in blocks (default: 16)

## Class Variables
- `chunkX`, `chunkZ`: Position of this chunk in the world grid
- `blocks`: 3D array storing all blocks in the chunk
- `chunkModel`: Single ModelInstance for the entire chunk
- `boundingBox`: BoundingBox for frustum culling
- `hasMesh`: Flag indicating if this chunk has a mesh built

## Methods

### Constructors
- `Chunk(int chunkX, int chunkZ)`: Creates a chunk at the specified position
- `Chunk(ChunkPosition position)`: Creates a chunk using a ChunkPosition object

### generateTerrain()
Generates the terrain for this chunk:
- Creates blocks based on terrain generation algorithm
- Sets block types based on height (grass, dirt, stone, etc.)
- Doesn't build the mesh automatically (deferred to VoxelEngine)

### buildMesh()
Creates a single optimized mesh for the entire chunk:
- Uses a single ModelBuilder for the entire chunk
- Maps materials to MeshPartBuilders to batch by material
- Only adds faces that are visible (not adjacent to solid blocks)
- Creates a bounding box for frustum culling
- Sets the hasMesh flag to true when complete

### render(ModelBatch modelBatch, Environment environment)
Renders the chunk if it's visible:
- Checks if the chunk has a mesh
- Gets the camera for frustum culling
- Uses frustum culling to skip rendering if not visible
- Renders the chunk's model with a single draw call

### isVisible(Camera camera)
Determines if the chunk is visible to the camera:
- Uses the camera's frustum to check if the chunk's bounding box is visible
- Prevents rendering chunks that are off-screen for better performance

### isBlockSolid(int x, int y, int z)
Checks if a block at the given position is solid (not air).

### shouldRenderBlock(int x, int y, int z)
Determines if a block should be rendered based on its neighbors:
- Only renders blocks that have at least one face adjacent to air or chunk boundary
- Reduces the number of faces that need to be rendered

### getVisibleFaces(int x, int y, int z)
Determines which faces of a block are visible:
- Checks each of the six faces (right, left, top, bottom, front, back)
- Returns a boolean array indicating which faces are visible

### addBlockFaces(MeshPartBuilder builder, int x, int y, int z, boolean[] visibleFaces)
Adds the visible faces of a block to the mesh:
- Only adds faces that are not adjacent to solid blocks
- Uses the appropriate material for the block type

### hasMesh()
Checks if the chunk has a mesh built.

### disposeMesh()
Disposes the chunk's mesh to free memory:
- Disposes the model
- Sets the hasMesh flag to false

### dispose()
Cleans up resources when the chunk is no longer needed:
- Calls disposeMesh() to clean up the model

## Performance Optimizations
1. **Single Mesh Per Chunk**: Creates one mesh for the entire chunk instead of per block type
2. **Material Batching**: Groups blocks with the same material together to minimize state changes
3. **Face Culling**: Only renders faces that are visible (not adjacent to solid blocks)
4. **Frustum Culling**: Skips rendering chunks that are not visible to the camera
5. **Efficient Memory Management**: Disposes meshes when not needed to save memory