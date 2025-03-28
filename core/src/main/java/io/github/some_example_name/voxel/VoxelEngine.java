package io.github.some_example_name.voxel;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import io.github.some_example_name.player.Camera;

public class VoxelEngine implements Disposable {
    private final ObjectMap<ChunkPosition, Chunk> chunks;
    private int worldSize;
    private int renderDistance;

    public VoxelEngine() {
        chunks = new ObjectMap<>();
    }

    public void init(int worldSize, int renderDistance) {
        this.worldSize = worldSize;
        this.renderDistance = renderDistance / 2;

        for (int x = 0; x < worldSize; x++) {
            for (int z = 0; z < worldSize; z++) {
                ChunkPosition position = new ChunkPosition(x, z);
                Chunk chunk = new Chunk(x, z);
                chunks.put(position, chunk);
            }
        }
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        // Get camera position in chunk coordinates
        float camX = Camera.getInstance().getPosition().x / Chunk.CHUNK_SIZE;
        float camZ = Camera.getInstance().getPosition().z / Chunk.CHUNK_SIZE;

        int camChunkX = (int) Math.floor(camX);
        int camChunkZ = (int) Math.floor(camZ);

        // Render only chunks within render distance
        for (ObjectMap.Entry<ChunkPosition, Chunk> entry : chunks) {
            ChunkPosition pos = entry.key;
            Chunk chunk = entry.value;

            int distX = Math.abs(pos.x - camChunkX);
            int distZ = Math.abs(pos.z - camChunkZ);

            // If chunk is within render distance, render it
            if (distX <= renderDistance && distZ <= renderDistance)
                chunk.render(modelBatch, environment);
        }
    }

    public int getWorldSize() {
        return worldSize;
    }

    @Override
    public void dispose() {
        for (Chunk chunk : chunks.values())
            chunk.dispose();
        chunks.clear();
    }

    private static class ChunkPosition {
        final int x;
        final int z;

        ChunkPosition(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ChunkPosition other = (ChunkPosition) obj;
            return x == other.x && z == other.z;
        }

        @Override
        public int hashCode() {
            return 31 * x + z;
        }
    }
}
