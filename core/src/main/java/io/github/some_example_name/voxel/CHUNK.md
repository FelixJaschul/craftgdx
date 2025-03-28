# Chunk.java Documentation

The `Chunk` class represents a section of the voxel world containing multiple blocks in a 3D grid.

## Constants
- `SIZE`: Size of the chunk in blocks (default: 16)
- `HEIGHT`: Height of the chunk in blocks (default: 128)

## Class Variables
- `blocks`: 3D array storing all blocks in the chunk
- `position`: ChunkPosition storing the chunk's coordinates in the world grid
- `model`: Single model for the entire chunk
- `boundingBox`: BoundingBox for frustum culling

## Methods

### Constructor
- `Chunk(ChunkPosition position)`: Creates a new chunk at the specified position

### generateTerrain()
Generates the terrain for this chunk:
- Creates blocks based on terrain generation algorithm
- Sets block types based on height (grass, dirt, stone, etc.)
- Marks blocks as visible or hidden based on neighboring blocks

### buildMesh()
Creates a single optimized mesh for the entire chunk:
- Batches all geometry of the same material together
- Creates one mesh per chunk instead of per block type
- Significantly reduces draw calls for better performance

### render(ModelBatch modelBatch, Environment environment, Camera camera)
Renders the chunk if it's visible:
- Uses frustum culling to skip rendering if the chunk is not visible to the camera
- Renders the chunk's model with the appropriate environment

### isVisible(Camera camera)
Determines if the chunk is visible to the camera:
- Uses the camera's frustum to check if the chunk's bounding box is visible
- Prevents rendering chunks that are off-screen for better performance

### hasMesh()
Checks if the chunk has a mesh built

### disposeMesh()
Disposes the chunk's mesh to free memory

### dispose()
Cleans up resources when the chunk is no longer needed:
- Disposes the model
- Releases memory