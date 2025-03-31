package io.github.some_example_name.voxel;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import io.github.some_example_name.player.Camera;

import java.util.HashSet;
import java.util.Set;

/**
 * Core engine for managing the voxel-based world.
 * Handles chunk generation, loading, rendering, and memory management
 * based on player position and view distance.
 */
public class VoxelEngine implements Disposable {
    /** Map of all chunks indexed by their position */
    private ObjectMap<ChunkPosition, Chunk> chunks; // TODO: save in file for faster loading
    /** Size of the world in chunks */
    private int worldSize;
    /** Maximum distance (in chunks) to render from the player */
    private int renderDistance;
    /** Set of chunks that need to have their meshes built */
    private final Set<ChunkPosition> chunksToLoad = new HashSet<>();
    /** Maximum number of chunk meshes to build per frame for performance */
    private static final int MAX_CHUNKS_PER_FRAME = 100;

    /**
     * Initializes the voxel engine with the specified world parameters.
     * Generates the initial world chunks.
     *
     * @param worldSize The size of the world in chunks (width and depth)
     * @param renderDistance The maximum distance (in chunks) to render from the player
     */
    public void init(int worldSize, int renderDistance) {
        this.worldSize = worldSize;
        this.renderDistance = renderDistance;
        this.chunks = new ObjectMap<>();

        // Generate initial world
        for (int x = 0; x < worldSize; x++) {
            for (int z = 0; z < worldSize; z++) {
                ChunkPosition pos = new ChunkPosition(x, z);
                Chunk chunk = new Chunk(pos);
                chunk.generateTerrain();
                chunks.put(pos, chunk);
            }
        }
    }

    /**
     * Renders the voxel world for the current frame.
     * Handles dynamic loading and unloading of chunk meshes based on camera position.
     *
     * @param modelBatch The model batch to use for rendering
     * @param environment The lighting environment to use for rendering
     */
    public void render(ModelBatch modelBatch, Environment environment) {
        // Get camera position in chunk coordinates
        int camChunkX = (int) Math.floor(Camera.getInstance().getPosition().x / Chunk.CHUNK_SIZE);
        int camChunkZ = (int) Math.floor(Camera.getInstance().getPosition().z / Chunk.CHUNK_SIZE);

        // Track which chunks are currently visible
        Set<ChunkPosition> visibleChunks = new HashSet<>();

        // Find visible chunks and queue for loading if needed
        for (int x = camChunkX - renderDistance; x <= camChunkX + renderDistance; x++) {
            for (int z = camChunkZ - renderDistance; z <= camChunkZ + renderDistance; z++) {
                if (x >= 0 && x < worldSize && z >= 0 && z < worldSize) {
                    ChunkPosition pos = new ChunkPosition(x, z);
                    visibleChunks.add(pos);

                    Chunk chunk = chunks.get(pos);
                    if (chunk != null && !chunk.hasMesh()) chunksToLoad.add(pos);
                }
            }
        }

        // Build meshes for a limited number of chunks per frame
        int chunksBuilt = 0;
        for (ChunkPosition pos : chunksToLoad) {
            if (chunksBuilt >= MAX_CHUNKS_PER_FRAME) break;

            Chunk chunk = chunks.get(pos);
            if (chunk != null && !chunk.hasMesh()) {
                chunk.buildMesh();
                chunksBuilt++;
            }
        }

        // Remove built chunks from the queue
        chunksToLoad.removeIf(pos -> chunks.get(pos) == null || chunks.get(pos).hasMesh());

        // Render visible chunks
        for (ChunkPosition pos : visibleChunks) {
            Chunk chunk = chunks.get(pos);
            if (chunk != null && chunk.hasMesh()) chunk.render(modelBatch, environment);
        }

        // Unload meshes for chunks that are no longer visible
        int bufferDistance = renderDistance + 2;
        for (ObjectMap.Entry<ChunkPosition, Chunk> entry : chunks) {
            Chunk chunk = entry.value;
            ChunkPosition pos = entry.key;

            // If chunk is outside buffer distance and has a mesh, unload it
            if (chunk.hasMesh() && (Math.abs(pos.x - camChunkX) > bufferDistance || Math.abs(pos.z - camChunkZ) > bufferDistance))
                    chunk.disposeMesh();
        }
    }

    /**
     * Disposes of all resources used by the voxel engine.
     * Cleans up all chunks and clears the chunk collections.
     */
    @Override
    public void dispose() {
        for (Chunk chunk : chunks.values()) chunk.dispose();
        chunks.clear();
        chunksToLoad.clear();
    }
}
