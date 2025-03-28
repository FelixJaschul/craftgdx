package io.github.some_example_name.voxel;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import io.github.some_example_name.terrain.Generation;
import io.github.some_example_name.player.Camera;
import java.util.HashMap;
import java.util.Map;

public class Chunk implements Disposable {
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_HEIGHT = 16;

    private final int chunkX;
    private final int chunkZ;
    private final BlockType[][][] blocks; // Stores the Block data
    private ModelInstance chunkModel;
    private final BoundingBox boundingBox;
    private static final Generation terrainGenerator = new Generation();
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

    // Implement Face Culling

    public void buildMesh() {
        // Create a single ModelBuilder for the entire chunk
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        // Map to store MeshPartBuilders for each material
        Map<Material, MeshPartBuilder> meshBuilders = new HashMap<>();

        // First pass: determine which blocks need to be rendered and batch by material
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    BlockType type = blocks[x][y][z];
                    if (type != BlockType.AIR && shouldRenderBlock(x, y, z)) {
                        Material material = type.getMaterial();

                        // Get or create mesh builder for this material
                        MeshPartBuilder builder = meshBuilders.get(material);
                        if (builder == null) {
                            // Create a new part for this material
                            String partId = "material_" + material.hashCode();
                            builder = modelBuilder.part(
                                partId,
                                GL20.GL_TRIANGLES,
                                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
                                material
                            );
                            meshBuilders.put(material, builder);
                        }

                        // Add block faces to the appropriate mesh builder
                        addBlockFaces(builder, x, y, z, getVisibleFaces(x, y, z));
                    }
                }
            }
        }

        // If no blocks were added, don't create an empty model
        if (meshBuilders.isEmpty()) {
            hasMesh = false;
            return;
        }

        // Create a single model for the entire chunk
        Model model = modelBuilder.end();

        // Dispose old model if it exists
        if (chunkModel != null && chunkModel.model != null) {
            chunkModel.model.dispose();
        }

        // Create model instance and set its position
        chunkModel = new ModelInstance(model);
        chunkModel.transform.setToTranslation(chunkX * CHUNK_SIZE, 0, chunkZ * CHUNK_SIZE);

        // Calculate bounding box for frustum culling
        chunkModel.calculateBoundingBox(boundingBox);
        boundingBox.mul(chunkModel.transform);

        hasMesh = true;
    }

    private boolean isBlockSolid(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_HEIGHT || z < 0 || z >= CHUNK_SIZE) return false;
        return blocks[x][y][z] != BlockType.AIR;  // Solid means NOT air
    }

    private boolean shouldRenderBlock(int x, int y, int z) {
        // Only render if at least one face is adjacent to air or chunk boundary
        return !isBlockSolid(x + 1, y, z) || !isBlockSolid(x - 1, y, z) ||
            !isBlockSolid(x, y + 1, z) || !isBlockSolid(x, y - 1, z) ||
            !isBlockSolid(x, y, z + 1) || !isBlockSolid(x, y, z - 1);
    }

    // Determine which faces of the block are visible
    private boolean[] getVisibleFaces(int x, int y, int z) {
        // Order: right, left, top, bottom, front, back
        boolean[] visibleFaces = new boolean[6];
        visibleFaces[Block.RIGHT] = isBlockSolid(x + 1, y, z);
        visibleFaces[Block.LEFT] = isBlockSolid(x - 1, y, z);
        visibleFaces[Block.TOP] = isBlockSolid(x, y + 1, z);
        visibleFaces[Block.BOTTOM] = isBlockSolid(x, y - 1, z);
        visibleFaces[Block.FRONT] = isBlockSolid(x, y, z + 1);
        visibleFaces[Block.BACK] = isBlockSolid(x, y, z - 1);
        return visibleFaces;
    }

    // End Face Culling

    public void render(ModelBatch modelBatch, Environment environment) {
        if (!hasMesh || chunkModel == null) return;

        com.badlogic.gdx.graphics.Camera camera = Camera.getInstance().getCamera();
        if (isVisible(camera)) modelBatch.render(chunkModel, environment);
    }

    // Frustum culling - check if chunk is visible to the camera
    private boolean isVisible(com.badlogic.gdx.graphics.Camera camera) {
        return boundingBox.isValid() && camera.frustum.boundsInFrustum(boundingBox);
    }

    // Part of the ModelBuilder
    private void addBlockFaces(MeshPartBuilder builder, int x, int y, int z, boolean[] visibleFaces) {
        // Right face (positive X)
        if (!visibleFaces[Block.RIGHT])
            builder.rect(
                new Vector3(x + 1, y + 1, z),
                new Vector3(x + 1, y + 1, z + 1),
                new Vector3(x + 1, y, z + 1),
                new Vector3(x + 1, y, z),
                new Vector3(1, 0, 0)
            );

        // Left face (negative X)
        if (!visibleFaces[Block.LEFT])
            builder.rect(
                new Vector3(x, y + 1, z + 1),
                new Vector3(x, y + 1, z),
                new Vector3(x, y, z),
                new Vector3(x, y, z + 1),
                new Vector3(-1, 0, 0)
            );

        // Top face (positive Y)
        if (!visibleFaces[Block.TOP])
            builder.rect(
                new Vector3(x + 1, y + 1, z + 1),
                new Vector3(x + 1, y + 1, z),
                new Vector3(x, y + 1, z),
                new Vector3(x, y + 1, z + 1),
                new Vector3(0, 1, 0)
            );

        // Bottom face (negative Y)
        if (!visibleFaces[Block.BOTTOM])
            builder.rect(
                new Vector3(x + 1, y, z),
                new Vector3(x + 1, y, z + 1),
                new Vector3(x, y, z + 1),
                new Vector3(x, y, z),
                new Vector3(0, -1, 0)
            );

        // Front face (positive Z)
        if (!visibleFaces[Block.FRONT])
            builder.rect(
                new Vector3(x, y, z + 1),
                new Vector3(x + 1, y, z + 1),
                new Vector3(x + 1, y + 1, z + 1),
                new Vector3(x, y + 1, z + 1),
                new Vector3(0, 0, 1)
            );

        // Back face (negative Z)
        if (!visibleFaces[Block.BACK])
            builder.rect(
                new Vector3(x, y, z),
                new Vector3(x, y + 1, z),
                new Vector3(x + 1, y + 1, z),
                new Vector3(x + 1, y, z),
                new Vector3(0, 0, -1)
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
