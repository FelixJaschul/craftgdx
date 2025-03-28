# BlockType

`BlockType` is an enumeration that represents different types of blocks in a voxel-based world.

## Overview

This enum defines various block types such as AIR, STONE, DIRT, GRASS, and SAND. Each block type has associated properties including:
- A texture path
- A solidity flag (indicating whether the block is solid or not)
- Material and texture objects for rendering

## Enum Values

| Value | Texture Path | Is Solid |
|-------|-------------|----------|
| AIR   | null        | false    |
| STONE | Blocks/Cobblestone.jpg | true |
| DIRT  | Blocks/Dirt.jpg | true |
| GRASS | Blocks/Grass.jpg | true |
| SAND  | Blocks/Sand.jpg | true |

## Properties

- **texturePath**: The file path to the texture image for the block
- **isSolid**: Boolean flag indicating if the block is solid (affects collision)
- **material**: LibGDX Material object for rendering
- **texture**: LibGDX Texture object loaded from the texture path

## Methods

### getTexturePath()
Returns the path to the texture file for this block type.

### isSolid()
Returns whether this block type is solid (true) or not (false).

### getMaterial()
Returns the LibGDX Material for this block type. Creates the material if it doesn't exist yet.
- Returns null for AIR blocks
- Lazily initializes the material with a diffuse texture attribute

### getTexture()
Returns the LibGDX Texture for this block type. Loads the texture if it doesn't exist yet.
- Returns null if texturePath is null
- Lazily loads the texture from the specified path

### disposeTextures()
Static method that disposes all textures for all block types to free up GPU memory.
- Iterates through all enum values
- Disposes any non-null textures
- Sets texture references to null
