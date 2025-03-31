package io.github.some_example_name.voxel;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import io.github.some_example_name.player.Camera;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Core engine for managing the voxel-based world.
 * Handles chunk generation, loading, rendering, and memory management
 * based on player position and view distance.
 */
public class VoxelEngine implements Disposable {
    private ObjectMap<ChunkPosition, Chunk> chunks;
    private int worldSize;
    private int renderDistance;
    private final Set<ChunkPosition> chunksToLoad = new HashSet<>();

    private static final int MAX_CHUNKS_PER_FRAME = 100;
    private static final int UNLOAD_BUFFER = 2;

    public void init(int worldSize, int renderDistance) {
        this.worldSize = worldSize;
        this.renderDistance = renderDistance;
        this.chunks = new ObjectMap<>();

        generateInitialWorld();
    }

    private void generateInitialWorld() {
        for (int x = 0; x < worldSize; x++) {
            for (int z = 0; z < worldSize; z++) {
                ChunkPosition pos = new ChunkPosition(x, z);
                Chunk chunk = new Chunk(pos);
                chunk.generateTerrain();
                chunks.put(pos, chunk);
            }
        }
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        Vector3 cameraPos = Camera.getInstance().getPosition();
        int camChunkX = (int) Math.floor(cameraPos.x / Chunk.CHUNK_SIZE);
        int camChunkZ = (int) Math.floor(cameraPos.z / Chunk.CHUNK_SIZE);

        Set<ChunkPosition> visibleChunks = findVisibleChunks(camChunkX, camChunkZ);
        buildChunkMeshes();
        renderVisibleChunks(visibleChunks, modelBatch, environment);
        unloadDistantChunks(camChunkX, camChunkZ);
    }

    private Set<ChunkPosition> findVisibleChunks(int camChunkX, int camChunkZ) {
        Set<ChunkPosition> visibleChunks = new HashSet<>();

        for (int x = camChunkX - renderDistance; x <= camChunkX + renderDistance; x++) {
            for (int z = camChunkZ - renderDistance; z <= camChunkZ + renderDistance; z++) {
                if (isValidChunkPosition(x, z)) {
                    ChunkPosition pos = new ChunkPosition(x, z);
                    visibleChunks.add(pos);

                    Chunk chunk = chunks.get(pos);
                    if (chunk != null && !chunk.hasMesh()) {
                        chunksToLoad.add(pos);
                    }
                }
            }
        }

        return visibleChunks;
    }

    private boolean isValidChunkPosition(int x, int z) {
        return x >= 0 && x < worldSize && z >= 0 && z < worldSize;
    }

    private void buildChunkMeshes() {
        int chunksBuilt = 0;
        Iterator<ChunkPosition> iterator = chunksToLoad.iterator();

        while (iterator.hasNext() && chunksBuilt < MAX_CHUNKS_PER_FRAME) {
            ChunkPosition pos = iterator.next();
            Chunk chunk = chunks.get(pos);

            if (chunk != null && !chunk.hasMesh()) {
                chunk.buildMesh();
                chunksBuilt++;
                iterator.remove();
            } else if (chunk == null || chunk.hasMesh()) {
                iterator.remove();
            }
        }
    }

    private void renderVisibleChunks(Set<ChunkPosition> visibleChunks, ModelBatch modelBatch, Environment environment) {
        for (ChunkPosition pos : visibleChunks) {
            Chunk chunk = chunks.get(pos);
            if (chunk != null && chunk.hasMesh()) {
                chunk.render(modelBatch, environment);
            }
        }
    }

    private void unloadDistantChunks(int camChunkX, int camChunkZ) {
        int bufferDistance = renderDistance + UNLOAD_BUFFER;

        for (ObjectMap.Entry<ChunkPosition, Chunk> entry : chunks) {
            Chunk chunk = entry.value;
            ChunkPosition pos = entry.key;

            boolean isTooFar = Math.abs(pos.x - camChunkX) > bufferDistance ||
                               Math.abs(pos.z - camChunkZ) > bufferDistance;

            if (chunk.hasMesh() && isTooFar) {
                chunk.disposeMesh();
            }
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
