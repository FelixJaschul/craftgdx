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
 */
public class Block {
    private static final float BLOCK_SIZE = 1.0f;
    private static final float HALF_SIZE = BLOCK_SIZE / 2f;
    private static final int VERTEX_ATTRIBUTES = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

    // Face indices
    public static final int RIGHT = 0;   // Positive X
    public static final int LEFT = 1;    // Negative X
    public static final int TOP = 2;     // Positive Y
    public static final int BOTTOM = 3;  // Negative Y
    public static final int FRONT = 4;   // Positive Z
    public static final int BACK = 5;    // Negative Z

    private final BlockType type;
    private final float x;
    private final float y;
    private final float z;
    private final boolean[] visibleFaces;
    private ModelInstance modelInstance;

    public Block(BlockType type, float x, float y, float z) {
        this(type, x, y, z, new boolean[]{true, true, true, true, true, true});
    }

    public Block(BlockType type, float x, float y, float z, boolean[] visibleFaces) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.visibleFaces = visibleFaces;
        createModelInstance();
    }

    private void createModelInstance() {
        if (type == BlockType.AIR || !hasVisibleFaces()) {
            return;
        }

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        Material material = type.getMaterial();
        MeshPartBuilder meshBuilder = modelBuilder.part("block", GL20.GL_TRIANGLES,
                                                      VERTEX_ATTRIBUTES, material);

        addVisibleFaces(meshBuilder);
        createAndPositionModel(modelBuilder);
    }

    private boolean hasVisibleFaces() {
        for (boolean face : visibleFaces) {
            if (face) {
                return true;
            }
        }
        return false;
    }

    private void addVisibleFaces(MeshPartBuilder meshBuilder) {
        if (visibleFaces[RIGHT]) createRightFace(meshBuilder);
        if (visibleFaces[LEFT]) createLeftFace(meshBuilder);
        if (visibleFaces[TOP]) createTopFace(meshBuilder);
        if (visibleFaces[BOTTOM]) createBottomFace(meshBuilder);
        if (visibleFaces[FRONT]) createFrontFace(meshBuilder);
        if (visibleFaces[BACK]) createBackFace(meshBuilder);
    }

    private void createAndPositionModel(ModelBuilder modelBuilder) {
        Model model = modelBuilder.end();
        modelInstance = new ModelInstance(model);
        modelInstance.transform.setToTranslation(x, y, z);
    }

    private void createRightFace(MeshPartBuilder meshBuilder) {
        meshBuilder.rect(
            new Vector3(HALF_SIZE, HALF_SIZE, -HALF_SIZE),   // top-back
            new Vector3(HALF_SIZE, HALF_SIZE, HALF_SIZE),    // top-front
            new Vector3(HALF_SIZE, -HALF_SIZE, HALF_SIZE),   // bottom-front
            new Vector3(HALF_SIZE, -HALF_SIZE, -HALF_SIZE),  // bottom-back
            new Vector3(1, 0, 0)                            // normal pointing right
        );
    }

    private void createLeftFace(MeshPartBuilder meshBuilder) {
        meshBuilder.rect(
            new Vector3(-HALF_SIZE, HALF_SIZE, HALF_SIZE),    // top-front
            new Vector3(-HALF_SIZE, HALF_SIZE, -HALF_SIZE),   // top-back
            new Vector3(-HALF_SIZE, -HALF_SIZE, -HALF_SIZE),  // bottom-back
            new Vector3(-HALF_SIZE, -HALF_SIZE, HALF_SIZE),   // bottom-front
            new Vector3(-1, 0, 0)                            // normal pointing left
        );
    }

    private void createTopFace(MeshPartBuilder meshBuilder) {
        meshBuilder.rect(
            new Vector3(HALF_SIZE, HALF_SIZE, HALF_SIZE),     // right-front
            new Vector3(HALF_SIZE, HALF_SIZE, -HALF_SIZE),    // right-back
            new Vector3(-HALF_SIZE, HALF_SIZE, -HALF_SIZE),   // left-back
            new Vector3(-HALF_SIZE, HALF_SIZE, HALF_SIZE),    // left-front
            new Vector3(0, 1, 0)                             // normal pointing up
        );
    }

    private void createBottomFace(MeshPartBuilder meshBuilder) {
        meshBuilder.rect(
            new Vector3(HALF_SIZE, -HALF_SIZE, -HALF_SIZE),   // right-back
            new Vector3(HALF_SIZE, -HALF_SIZE, HALF_SIZE),    // right-front
            new Vector3(-HALF_SIZE, -HALF_SIZE, HALF_SIZE),   // left-front
            new Vector3(-HALF_SIZE, -HALF_SIZE, -HALF_SIZE),  // left-back
            new Vector3(0, -1, 0)                            // normal pointing down
        );
    }

    private void createFrontFace(MeshPartBuilder meshBuilder) {
        meshBuilder.rect(
            new Vector3(-HALF_SIZE, -HALF_SIZE, HALF_SIZE),   // bottom-left
            new Vector3(HALF_SIZE, -HALF_SIZE, HALF_SIZE),    // bottom-right
            new Vector3(HALF_SIZE, HALF_SIZE, HALF_SIZE),     // top-right
            new Vector3(-HALF_SIZE, HALF_SIZE, HALF_SIZE),    // top-left
            new Vector3(0, 0, 1)                             // normal pointing front
        );
    }

    private void createBackFace(MeshPartBuilder meshBuilder) {
        meshBuilder.rect(
            new Vector3(HALF_SIZE, -HALF_SIZE, -HALF_SIZE),   // bottom-right
            new Vector3(-HALF_SIZE, -HALF_SIZE, -HALF_SIZE),  // bottom-left
            new Vector3(-HALF_SIZE, HALF_SIZE, -HALF_SIZE),   // top-left
            new Vector3(HALF_SIZE, HALF_SIZE, -HALF_SIZE),    // top-right
            new Vector3(0, 0, -1)                            // normal pointing back
        );
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }
}
