package io.github.some_example_name.chunk;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;
import io.github.some_example_name.block.Block;
import io.github.some_example_name.block.BlockType;
import io.github.some_example_name.terrain.Generation;
import io.github.some_example_name.player.Camera;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a chunk of the voxel world.
 */
public class Chunk implements Disposable {
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_HEIGHT = 50;

    private static final int VERTEX_ATTRIBUTES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;
    private static final Generation terrainGenerator = new Generation();

    private final int chunkX;
    private final int chunkZ;
    private final BlockType[][][] blocks;
    private final BoundingBox boundingBox;

    private ModelInstance chunkModel;
    private boolean hasMesh = false;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blocks = new BlockType[CHUNK_SIZE][CHUNK_HEIGHT][CHUNK_SIZE];
        this.boundingBox = new BoundingBox();
    }

    public Chunk(ChunkPosition position) {
        this(position.x, position.z);
    }

    public void generateTerrain() {
        int[][] heightMap = terrainGenerator.generateHeightMap(chunkX, chunkZ, CHUNK_SIZE);
        terrainGenerator.fillChunkWithTerrain(blocks, heightMap, CHUNK_HEIGHT);
    }

    public void buildMesh() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        Map<BlockType, MeshPartBuilder> meshBuilders = new HashMap<>();
        buildBlockMeshes(modelBuilder, meshBuilders);

        if (meshBuilders.isEmpty()) {
            hasMesh = false;
            return;
        }

        createModelInstance(modelBuilder);
        hasMesh = true;
    }

    private void buildBlockMeshes(ModelBuilder modelBuilder, Map<BlockType, MeshPartBuilder> meshBuilders) {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    BlockType type = blocks[x][y][z];
                    if (type != BlockType.AIR && shouldRenderBlock(x, y, z)) {
                        MeshPartBuilder builder = getOrCreateMeshBuilder(modelBuilder, meshBuilders, type);
                        addBlockFaces(builder, x, y, z, getVisibleFaces(x, y, z));
                    }
                }
            }
        }
    }

    private MeshPartBuilder getOrCreateMeshBuilder(ModelBuilder modelBuilder, Map<BlockType, MeshPartBuilder> meshBuilders, BlockType type) {
        MeshPartBuilder builder = meshBuilders.get(type);
        if (builder == null) {
            String partId = "blocktype_" + type.name();
            builder = modelBuilder.part(partId, GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, type.getMaterial());
            meshBuilders.put(type, builder);
        }
        return builder;
    }

    private void createModelInstance(ModelBuilder modelBuilder) {
        Model model = modelBuilder.end();

        if (chunkModel != null && chunkModel.model != null) {
            chunkModel.model.dispose();
        }

        chunkModel = new ModelInstance(model);
        chunkModel.transform.setToTranslation(chunkX * CHUNK_SIZE, 0, chunkZ * CHUNK_SIZE);

        // Calculate bounding box for frustum culling
        chunkModel.calculateBoundingBox(boundingBox);
        boundingBox.mul(chunkModel.transform);
    }

    private boolean isBlockSolid(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_HEIGHT || z < 0 || z >= CHUNK_SIZE) {
            return false;
        }
        return blocks[x][y][z] != BlockType.AIR;
    }

    private boolean shouldRenderBlock(int x, int y, int z) {
        return !isBlockSolid(x + 1, y, z) || !isBlockSolid(x - 1, y, z) ||
               !isBlockSolid(x, y + 1, z) || !isBlockSolid(x, y - 1, z) ||
               !isBlockSolid(x, y, z + 1) || !isBlockSolid(x, y, z - 1);
    }

    private boolean[] getVisibleFaces(int x, int y, int z) {
        boolean[] visibleFaces = new boolean[6];
        visibleFaces[Block.RIGHT] = !isBlockSolid(x + 1, y, z);
        visibleFaces[Block.LEFT] = !isBlockSolid(x - 1, y, z);
        visibleFaces[Block.TOP] = !isBlockSolid(x, y + 1, z);
        visibleFaces[Block.BOTTOM] = !isBlockSolid(x, y - 1, z);
        visibleFaces[Block.FRONT] = !isBlockSolid(x, y, z + 1);
        visibleFaces[Block.BACK] = !isBlockSolid(x, y, z - 1);
        return visibleFaces;
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        if (!hasMesh || chunkModel == null) return;

        com.badlogic.gdx.graphics.Camera camera = Camera.getInstance().getCamera();
        if (isVisible(camera)) {
            modelBatch.render(chunkModel, environment);
        }
    }

    private boolean isVisible(com.badlogic.gdx.graphics.Camera camera) {
        return boundingBox.isValid() && camera.frustum.boundsInFrustum(boundingBox);
    }

    private void addBlockFaces(MeshPartBuilder builder, int x, int y, int z, boolean[] visibleFaces) {
        if (visibleFaces[Block.RIGHT]) {
            addRightFace(builder, x, y, z);
        }
        if (visibleFaces[Block.LEFT]) {
            addLeftFace(builder, x, y, z);
        }
        if (visibleFaces[Block.TOP]) {
            addTopFace(builder, x, y, z);
        }
        if (visibleFaces[Block.BOTTOM]) {
            addBottomFace(builder, x, y, z);
        }
        if (visibleFaces[Block.FRONT]) {
            addFrontFace(builder, x, y, z);
        }
        if (visibleFaces[Block.BACK]) {
            addBackFace(builder, x, y, z);
        }
    }

    private void addRightFace(MeshPartBuilder builder, int x, int y, int z) {
        builder.rect(
            new Vector3(x + 1, y + 1, z),       // top-back
            new Vector3(x + 1, y + 1, z + 1),   // top-front
            new Vector3(x + 1, y, z + 1),       // bottom-front
            new Vector3(x + 1, y, z),           // bottom-back
            new Vector3(1, 0, 0)                // normal pointing right
        );
    }

    private void addLeftFace(MeshPartBuilder builder, int x, int y, int z) {
        builder.rect(
            new Vector3(x, y + 1, z + 1),       // top-front
            new Vector3(x, y + 1, z),           // top-back
            new Vector3(x, y, z),               // bottom-back
            new Vector3(x, y, z + 1),           // bottom-front
            new Vector3(-1, 0, 0)               // normal pointing left
        );
    }

    private void addTopFace(MeshPartBuilder builder, int x, int y, int z) {
        builder.rect(
            new Vector3(x + 1, y + 1, z + 1),   // right-front
            new Vector3(x + 1, y + 1, z),       // right-back
            new Vector3(x, y + 1, z),           // left-back
            new Vector3(x, y + 1, z + 1),       // left-front
            new Vector3(0, 1, 0)                // normal pointing up
        );
    }

    private void addBottomFace(MeshPartBuilder builder, int x, int y, int z) {
        builder.rect(
            new Vector3(x + 1, y, z),           // right-back
            new Vector3(x + 1, y, z + 1),       // right-front
            new Vector3(x, y, z + 1),           // left-front
            new Vector3(x, y, z),               // left-back
            new Vector3(0, -1, 0)               // normal pointing down
        );
    }

    private void addFrontFace(MeshPartBuilder builder, int x, int y, int z) {
        builder.rect(
            new Vector3(x, y, z + 1),           // bottom-left
            new Vector3(x + 1, y, z + 1),       // bottom-right
            new Vector3(x + 1, y + 1, z + 1),   // top-right
            new Vector3(x, y + 1, z + 1),       // top-left
            new Vector3(0, 0, 1)                // normal pointing front
        );
    }

    private void addBackFace(MeshPartBuilder builder, int x, int y, int z) {
        builder.rect(
            new Vector3(x + 1, y, z),           // bottom-right
            new Vector3(x, y, z),               // bottom-left
            new Vector3(x, y + 1, z),           // top-left
            new Vector3(x + 1, y + 1, z),       // top-right
            new Vector3(0, 0, -1)               // normal pointing back
        );
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public boolean hasMesh() {
        return hasMesh;
    }

    public void disposeMesh() {
        if (chunkModel != null && chunkModel.model != null) {
            chunkModel.model.dispose();
            chunkModel = null;
        }
        hasMesh = false;
    }

    @Override
    public void dispose() {
        disposeMesh();
    }
}
