package io.github.some_example_name.voxel;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.ObjectMap;
import io.github.some_example_name.player.Camera;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoxelEngine {
    private ObjectMap<ChunkPosition, Chunk> chunks;
    private int worldSize;
    private int renderDistance;
    private ExecutorService chunkLoadExecutor;
    private Set<ChunkPosition> loadingChunks = new HashSet<>();

    public void init(int worldSize, int renderDistance) {
        this.worldSize = worldSize;
        this.renderDistance = renderDistance;
        this.chunks = new ObjectMap<>();
        this.chunkLoadExecutor = Executors.newFixedThreadPool(2); // Use 2 threads for chunk loading

        // Initial world generation - only generate chunk data, not meshes
        for (int x = 0; x < worldSize; x++) {
            for (int z = 0; z < worldSize; z++) {
                ChunkPosition pos = new ChunkPosition(x, z);
                Chunk chunk = new Chunk(pos);
                chunk.generateTerrain(); // Only generate terrain data, not mesh
                chunks.put(pos, chunk);
            }
        }
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        // Get camera position in chunk coordinates
        int camChunkX = (int) Math.floor(Camera.getInstance().getPosition().x / Chunk.CHUNK_SIZE);
        int camChunkZ = (int) Math.floor(Camera.getInstance().getPosition().z / Chunk.CHUNK_SIZE);

        // Track which chunks are currently visible
        Set<ChunkPosition> visibleChunks = new HashSet<>();

        // Render only chunks within render distance
        for (int x = camChunkX - renderDistance; x <= camChunkX + renderDistance; x++) {
            for (int z = camChunkZ - renderDistance; z <= camChunkZ + renderDistance; z++) {
                if (x >= 0 && x < worldSize && z >= 0 && z < worldSize) {
                    ChunkPosition pos = new ChunkPosition(x, z);
                    visibleChunks.add(pos);

                    Chunk chunk = chunks.get(pos);
                    if (chunk != null) {
                        // If the chunk doesn't have a mesh yet and isn't already loading, build it
                        if (!chunk.hasMesh() && !loadingChunks.contains(pos)) {
                            loadingChunks.add(pos);
                            final Chunk chunkToLoad = chunk;
                            chunkLoadExecutor.submit(() -> {
                                chunkToLoad.buildMesh();
                                loadingChunks.remove(pos);
                            });
                        }

                        // Only render if the chunk has a mesh
                        if (chunk.hasMesh()) {
                            chunk.render(modelBatch, environment);
                        }
                    }
                }
            }
        }

        // Unload meshes for chunks that are no longer visible
        // Use a buffer zone to prevent frequent loading/unloading when moving
        int bufferDistance = renderDistance + 2;
        for (ObjectMap.Entry<ChunkPosition, Chunk> entry : chunks) {
            ChunkPosition pos = entry.key;
            Chunk chunk = entry.value;

            // If chunk is outside buffer distance and has a mesh, unload it
            if (chunk.hasMesh() && !isWithinDistance(pos, camChunkX, camChunkZ, bufferDistance)) {
                chunk.disposeMesh();
            }
        }
    }

    private boolean isWithinDistance(ChunkPosition pos, int camX, int camZ, int distance) {
        int dx = Math.abs(pos.x - camX);
        int dz = Math.abs(pos.z - camZ);
        return dx <= distance && dz <= distance;
    }

    public void dispose() {
        chunkLoadExecutor.shutdown();
        for (Chunk chunk : chunks.values()) {
            chunk.dispose();
        }
    }
}
