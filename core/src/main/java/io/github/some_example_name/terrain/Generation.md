# Terrain Generation System

The terrain generation system uses Perlin noise to create realistic, procedurally generated landscapes. This document explains how the system works and how to customize it.

## Overview

The `Generation` class in the `terrain` package handles all terrain generation using Perlin noise algorithms. It creates height maps for chunks and then fills the 3D block arrays with appropriate block types based on the height and other factors.

## Key Components

### Generation Class

The main class responsible for terrain generation with these key methods:

- `generateHeightMap(chunkX, chunkZ, chunkSize)`: Creates a 2D height map for a chunk
- `fillChunkWithTerrain(blocks, heightMap, chunkHeight)`: Fills a 3D block array with terrain based on the height map

### PerlinNoise Class

An inner class that implements the Perlin noise algorithm:

- `noise(x, y)`: Generates 2D Perlin noise
- `noise(x, y, z)`: Generates 3D Perlin noise

## Customization Options

The terrain generation can be customized by modifying these constants in the `Generation` class:

- `SEED`: The random seed for terrain generation
- `TERRAIN_SCALE`: Controls the "zoom" level of the noise (smaller values = larger features)
- `AMPLITUDE`: Controls the height variation of the terrain
- `BASE_HEIGHT`: The base height of the terrain
- `MOUNTAIN_THRESHOLD`: Noise threshold for mountain generation
- `WATER_LEVEL`: Height level for water/beaches

## How It Works

1. The system generates a height map using Perlin noise for each chunk
2. It uses multiple octaves of noise to add detail to the terrain
3. The height map determines the surface level at each x,z coordinate
4. The system fills the 3D block array with appropriate block types:
   - Air above the surface
   - Surface blocks (dirt, stone) at the top layer
   - Dirt for a few layers below the surface
   - Stone for deep underground

## Future Enhancements

Possible enhancements to the terrain generation system:

1. Add more biomes (desert, forest, tundra)
2. Implement caves and overhangs using 3D noise
3. Add structures like trees, villages, etc.
4. Implement rivers and lakes
5. Add ore generation at different depths