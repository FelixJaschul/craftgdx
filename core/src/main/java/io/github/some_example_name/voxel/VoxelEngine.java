package io.github.some_example_name.voxel;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import io.github.some_example_name.player.Camera;

import java.util.HashSet;
import java.util.Set;

public class VoxelEngine implements Disposable {
    private ObjectMap<ChunkPosition, Chunk> chunks; // Stores the Map TODO: save in file for faster loading
    private int worldSize;
    private int renderDistance;
    private final Set<ChunkPosition> chunksToLoad = new HashSet<>();
    private static final int MAX_CHUNKS_PER_FRAME = 1;

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
                    if (chunk != null && !chunk.hasMesh()) {
                        chunksToLoad.add(pos);
                    }
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
            if (chunk != null && chunk.hasMesh()) {
                chunk.render(modelBatch, environment);
            }
        }

        // Unload meshes for chunks that are no longer visible
        int bufferDistance = renderDistance + 2;
        for (ObjectMap.Entry<ChunkPosition, Chunk> entry : chunks) {
            Chunk chunk = entry.value;
            ChunkPosition pos = entry.key;

            // If chunk is outside buffer distance and has a mesh, unload it
            if (chunk.hasMesh() &&
                (Math.abs(pos.x - camChunkX) > bufferDistance ||
                 Math.abs(pos.z - camChunkZ) > bufferDistance)) {
                chunk.disposeMesh();
            }
        }
    }

    @Override
    public void dispose() {
        for (Chunk chunk : chunks.values()) {
            chunk.dispose();
        }
        chunks.clear();
        chunksToLoad.clear();
    }
}
