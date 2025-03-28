# Block.java Documentation

The `Block` class represents a single block in the voxel world, handling its rendering and position.

## Constants
- `BLOCK_SIZE`: Size of each block (1.0 unit)
- `HALF_SIZE`: Half the block size, used for vertex calculations
- Face indices: `RIGHT`, `LEFT`, `TOP`, `BOTTOM`, `FRONT`, `BACK` for identifying block faces

## Class Variables
- `type`: The BlockType of this block
- `x`, `y`, `z`: Position coordinates in the world
- `modelInstance`: LibGDX ModelInstance for rendering
- `visibleFaces`: Boolean array indicating which faces should be rendered

## Methods

### Constructors
- Default constructor creates a block with all faces visible
- Overloaded constructor allows specifying which faces are visible

### createModelInstance()
Creates the 3D model for the block:
- Skips if the block is AIR or has no visible faces
- Creates a material with the block's texture
- Adds only the visible faces to the model
- Positions the model at the block's coordinates

### createXXXFace() Methods
Six methods that create the geometry for each face of the block:
- Each method defines the vertices and normal for one face
- Uses LibGDX's MeshPartBuilder to create the face geometry

### getModelInstance()
Returns the ModelInstance for rendering
