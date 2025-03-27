package io.github.some_example_name.voxel;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class VoxelEngine implements Disposable {
    private final Array<Chunk> chunks;

    public VoxelEngine() {
        chunks = new Array<>();
    }

    public void initialize(int numChunks) {
        int gridSize = (int) Math.sqrt(numChunks);

        // Create chunks in a grid pattern
        for (int x = 0; x < gridSize; x++) {
            for (int z = 0; z < gridSize; z++) {
                Chunk chunk = new Chunk(x, z);
                chunks.add(chunk);
            }
        }
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        for (Chunk chunk : chunks) chunk.render(modelBatch, environment);
    }

    @Override
    public void dispose() {
        for (Chunk chunk : chunks) chunk.dispose();
        chunks.clear();
    }
}
