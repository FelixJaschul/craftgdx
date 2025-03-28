package io.github.some_example_name.voxel;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class Block {
    private static final float BLOCK_SIZE = 1.0f;
    private static final float HALF_SIZE = BLOCK_SIZE / 2f;

    // Face indices: right, left, top, bottom, front, back
    public static final int RIGHT = 0;
    public static final int LEFT = 1;
    public static final int TOP = 2;
    public static final int BOTTOM = 3;
    public static final int FRONT = 4;
    public static final int BACK = 5;

    private final BlockType type;
    private final float x;
    private final float y;
    private final float z;
    private ModelInstance modelInstance;
    private final boolean[] visibleFaces;

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
        if (type == BlockType.AIR) return;

        // If no faces are visible, don't create a model
        boolean hasVisibleFace = false;
        for (boolean face : visibleFaces) {
            if (face) {
                hasVisibleFace = true;
                break;
            }
        }

        if (!hasVisibleFace) return;

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        // Create material with texture
        Material material = new Material();
        TextureAttribute textureAttribute = TextureAttribute.createDiffuse(type.getTexture());
        material.set(textureAttribute);

        MeshPartBuilder meshBuilder = modelBuilder.part("block", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal | Usage.TextureCoordinates, material);

        // Create only the visible faces
        if (visibleFaces[RIGHT]) createRightFace(meshBuilder);
        if (visibleFaces[LEFT]) createLeftFace(meshBuilder);
        if (visibleFaces[TOP]) createTopFace(meshBuilder);
        if (visibleFaces[BOTTOM]) createBottomFace(meshBuilder);
        if (visibleFaces[FRONT]) createFrontFace(meshBuilder);
        if (visibleFaces[BACK]) createBackFace(meshBuilder);

        Model model = modelBuilder.end();
        modelInstance = new ModelInstance(model);
        modelInstance.transform.setToTranslation(x, y, z);
    }

    private void createRightFace(MeshPartBuilder meshBuilder) {
        // Right face (x = HALF_SIZE)
        meshBuilder.rect(
            new Vector3(HALF_SIZE, HALF_SIZE, -HALF_SIZE),
            new Vector3(HALF_SIZE, HALF_SIZE, HALF_SIZE),
            new Vector3(HALF_SIZE, -HALF_SIZE, HALF_SIZE),
            new Vector3(HALF_SIZE, -HALF_SIZE, -HALF_SIZE),
            new Vector3(1, 0, 0)
        );
    }

    private void createLeftFace(MeshPartBuilder meshBuilder) {
        // Left face (x = -HALF_SIZE)
        meshBuilder.rect(
            new Vector3(-HALF_SIZE, HALF_SIZE, HALF_SIZE),
            new Vector3(-HALF_SIZE, HALF_SIZE, -HALF_SIZE),
            new Vector3(-HALF_SIZE, -HALF_SIZE, -HALF_SIZE),
            new Vector3(-HALF_SIZE, -HALF_SIZE, HALF_SIZE),
            new Vector3(-1, 0, 0)
        );
    }

    private void createTopFace(MeshPartBuilder meshBuilder) {
        // Top face (y = HALF_SIZE)
        meshBuilder.rect(
            new Vector3(HALF_SIZE, HALF_SIZE, HALF_SIZE),
            new Vector3(HALF_SIZE, HALF_SIZE, -HALF_SIZE),
            new Vector3(-HALF_SIZE, HALF_SIZE, -HALF_SIZE),
            new Vector3(-HALF_SIZE, HALF_SIZE, HALF_SIZE),
            new Vector3(0, 1, 0)
        );
    }

    private void createBottomFace(MeshPartBuilder meshBuilder) {
        // Bottom face (y = -HALF_SIZE)
        meshBuilder.rect(
            new Vector3(HALF_SIZE, -HALF_SIZE, -HALF_SIZE),
            new Vector3(HALF_SIZE, -HALF_SIZE, HALF_SIZE),
            new Vector3(-HALF_SIZE, -HALF_SIZE, HALF_SIZE),
            new Vector3(-HALF_SIZE, -HALF_SIZE, -HALF_SIZE),
            new Vector3(0, -1, 0)
        );
    }

    private void createFrontFace(MeshPartBuilder meshBuilder) {
        // Front face (z = HALF_SIZE)
        meshBuilder.rect(
            new Vector3(-HALF_SIZE, -HALF_SIZE, HALF_SIZE),
            new Vector3(HALF_SIZE, -HALF_SIZE, HALF_SIZE),
            new Vector3(HALF_SIZE, HALF_SIZE, HALF_SIZE),
            new Vector3(-HALF_SIZE, HALF_SIZE, HALF_SIZE),
            new Vector3(0, 0, 1)
        );
    }

    private void createBackFace(MeshPartBuilder meshBuilder) {
        // Back face (z = -HALF_SIZE)
        meshBuilder.rect(
            new Vector3(HALF_SIZE, -HALF_SIZE, -HALF_SIZE),
            new Vector3(-HALF_SIZE, -HALF_SIZE, -HALF_SIZE),
            new Vector3(-HALF_SIZE, HALF_SIZE, -HALF_SIZE),
            new Vector3(HALF_SIZE, HALF_SIZE, -HALF_SIZE),
            new Vector3(0, 0, -1)
        );
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }
}
