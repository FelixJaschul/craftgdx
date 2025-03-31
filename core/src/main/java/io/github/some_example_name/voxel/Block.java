package io.github.some_example_name.voxel;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Vector3;

/**
 * Represents a single block in the voxel world.
 * Handles the creation and rendering of individual block meshes.
 */
public class Block {
    /** Size of a block in world units */
    private static final float BLOCK_SIZE = 1.0f;
    /** Half size of a block, used for vertex positioning */
    private static final float HALF_SIZE = BLOCK_SIZE / 2f;

    /** Face indices for the six sides of a block */
    public static final int RIGHT = 0;   // Positive X
    public static final int LEFT = 1;    // Negative X
    public static final int TOP = 2;     // Positive Y
    public static final int BOTTOM = 3;  // Negative Y
    public static final int FRONT = 4;   // Positive Z
    public static final int BACK = 5;    // Negative Z

    /** Type of this block */
    private final BlockType type;
    /** X-coordinate in world space */
    private final float x;
    /** Y-coordinate in world space */
    private final float y;
    /** Z-coordinate in world space */
    private final float z;
    /** Model instance for rendering this block */
    private ModelInstance modelInstance;
    /** Array indicating which faces of the block are visible */
    private final boolean[] visibleFaces;

    /**
     * Creates a new block with all faces visible.
     *
     * @param type The type of block
     * @param x The x-coordinate in world space
     * @param y The y-coordinate in world space
     * @param z The z-coordinate in world space
     */
    public Block(BlockType type, float x, float y, float z) {
        this(type, x, y, z, new boolean[]{true, true, true, true, true, true});
    }

    /**
     * Creates a new block with specified visible faces.
     *
     * @param type The type of block
     * @param x The x-coordinate in world space
     * @param y The y-coordinate in world space
     * @param z The z-coordinate in world space
     * @param visibleFaces Array indicating which faces are visible
     */
    public Block(BlockType type, float x, float y, float z, boolean[] visibleFaces) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.visibleFaces = visibleFaces;
        createModelInstance();
    }

    /**
     * Creates a model instance for this block if it has visible faces.
     * Air blocks and blocks with no visible faces don't get model instances.
     */
    private void createModelInstance() {
        if (type == BlockType.AIR) return;

        // Check if any face is visible
        boolean hasVisibleFace = false;
        for (boolean face : visibleFaces) {
            if (face) {
                hasVisibleFace = true;
                break;
            }
        }
        if (!hasVisibleFace) return;

        // Create the model
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        Material material = type.getMaterial();
        MeshPartBuilder meshBuilder = modelBuilder.part("block", GL20.GL_TRIANGLES,
            Usage.Position | Usage.Normal | Usage.TextureCoordinates, material);

        // Add visible faces to the mesh
        if (visibleFaces[RIGHT]) createRightFace(meshBuilder);
        if (visibleFaces[LEFT]) createLeftFace(meshBuilder);
        if (visibleFaces[TOP]) createTopFace(meshBuilder);
        if (visibleFaces[BOTTOM]) createBottomFace(meshBuilder);
        if (visibleFaces[FRONT]) createFrontFace(meshBuilder);
        if (visibleFaces[BACK]) createBackFace(meshBuilder);

        // Create the model instance and position it
        Model model = modelBuilder.end();
        modelInstance = new ModelInstance(model);
        modelInstance.transform.setToTranslation(x, y, z);
    }

    /**
     * Creates the right face (positive X) of the block.
     *
     * @param meshBuilder The mesh builder to add the face to
     */
    private void createRightFace(MeshPartBuilder meshBuilder) {
        meshBuilder.rect(
            new Vector3(HALF_SIZE, HALF_SIZE, -HALF_SIZE),   // top-back
            new Vector3(HALF_SIZE, HALF_SIZE, HALF_SIZE),    // top-front
            new Vector3(HALF_SIZE, -HALF_SIZE, HALF_SIZE),   // bottom-front
            new Vector3(HALF_SIZE, -HALF_SIZE, -HALF_SIZE),  // bottom-back
            new Vector3(1, 0, 0)                            // normal pointing right
        );
    }

    /**
     * Creates the left face (negative X) of the block.
     *
     * @param meshBuilder The mesh builder to add the face to
     */
    private void createLeftFace(MeshPartBuilder meshBuilder) {
        meshBuilder.rect(
            new Vector3(-HALF_SIZE, HALF_SIZE, HALF_SIZE),    // top-front
            new Vector3(-HALF_SIZE, HALF_SIZE, -HALF_SIZE),   // top-back
            new Vector3(-HALF_SIZE, -HALF_SIZE, -HALF_SIZE),  // bottom-back
            new Vector3(-HALF_SIZE, -HALF_SIZE, HALF_SIZE),   // bottom-front
            new Vector3(-1, 0, 0)                            // normal pointing left
        );
    }

    /**
     * Creates the top face (positive Y) of the block.
     *
     * @param meshBuilder The mesh builder to add the face to
     */
    private void createTopFace(MeshPartBuilder meshBuilder) {
        meshBuilder.rect(
            new Vector3(HALF_SIZE, HALF_SIZE, HALF_SIZE),     // right-front
            new Vector3(HALF_SIZE, HALF_SIZE, -HALF_SIZE),    // right-back
            new Vector3(-HALF_SIZE, HALF_SIZE, -HALF_SIZE),   // left-back
            new Vector3(-HALF_SIZE, HALF_SIZE, HALF_SIZE),    // left-front
            new Vector3(0, 1, 0)                             // normal pointing up
        );
    }

    /**
     * Creates the bottom face (negative Y) of the block.
     *
     * @param meshBuilder The mesh builder to add the face to
     */
    private void createBottomFace(MeshPartBuilder meshBuilder) {
        meshBuilder.rect(
            new Vector3(HALF_SIZE, -HALF_SIZE, -HALF_SIZE),   // right-back
            new Vector3(HALF_SIZE, -HALF_SIZE, HALF_SIZE),    // right-front
            new Vector3(-HALF_SIZE, -HALF_SIZE, HALF_SIZE),   // left-front
            new Vector3(-HALF_SIZE, -HALF_SIZE, -HALF_SIZE),  // left-back
            new Vector3(0, -1, 0)                            // normal pointing down
        );
    }

    /**
     * Creates the front face (positive Z) of the block.
     *
     * @param meshBuilder The mesh builder to add the face to
     */
    private void createFrontFace(MeshPartBuilder meshBuilder) {
        meshBuilder.rect(
            new Vector3(-HALF_SIZE, -HALF_SIZE, HALF_SIZE),   // bottom-left
            new Vector3(HALF_SIZE, -HALF_SIZE, HALF_SIZE),    // bottom-right
            new Vector3(HALF_SIZE, HALF_SIZE, HALF_SIZE),     // top-right
            new Vector3(-HALF_SIZE, HALF_SIZE, HALF_SIZE),    // top-left
            new Vector3(0, 0, 1)                             // normal pointing front
        );
    }

    /**
     * Creates the back face (negative Z) of the block.
     *
     * @param meshBuilder The mesh builder to add the face to
     */
    private void createBackFace(MeshPartBuilder meshBuilder) {
        meshBuilder.rect(
            new Vector3(HALF_SIZE, -HALF_SIZE, -HALF_SIZE),   // bottom-right
            new Vector3(-HALF_SIZE, -HALF_SIZE, -HALF_SIZE),  // bottom-left
            new Vector3(-HALF_SIZE, HALF_SIZE, -HALF_SIZE),   // top-left
            new Vector3(HALF_SIZE, HALF_SIZE, -HALF_SIZE),    // top-right
            new Vector3(0, 0, -1)                            // normal pointing back
        );
    }

    /**
     * Gets the model instance for this block.
     *
     * @return The model instance, or null if the block has no visible faces
     */
    public ModelInstance getModelInstance() {
        return modelInstance;
    }
}
