package io.github.some_example_name.voxel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.utils.Disposable;

/**
 * Enum representing the different types of blocks in the voxel world.
 * Each block type has its own texture and physical properties.
 */
public enum BlockType {
    /** Empty block with no texture or collision */
    AIR(null, false),
    /** Stone block with stone texture */
    STONE("Blocks/Stone.jpg", true),
    /** Dirt block with dirt texture */
    DIRT("Blocks/Dirt.jpg", true),
    /** Grass block with grass texture */
    GRASS("Blocks/Grass.jpg", true),
    /** Sand block with sand texture */
    SAND("Blocks/Sand.jpg", true),
    /** Water block with water texture and no collision */
    WATER("Blocks/Water.jpg", false);

    /** Whether this block type is solid (has collision) */
    private final boolean isSolid;
    /** Path to the texture file for this block type */
    private final String texturePath;
    /** Material used for rendering this block type */
    private Material material;
    /** Texture used for this block type */
    private Texture texture;

    /**
     * Creates a new block type with the specified texture and solidity.
     *
     * @param texturePath Path to the texture file, or null for no texture
     * @param isSolid Whether this block type is solid (has collision)
     */
    BlockType(String texturePath, boolean isSolid) {
        this.isSolid = isSolid;
        this.texturePath = texturePath;
    }

    /**
     * Gets the path to the texture file for this block type.
     *
     * @return The texture path, or null if this block type has no texture
     */
    public String getTexturePath() {
        return texturePath;
    }

    /**
     * Checks if this block type is solid (has collision).
     *
     * @return True if this block type is solid, false otherwise
     */
    public boolean isSolid() {
        return isSolid;
    }

    /**
     * Gets the material used for rendering this block type.
     * Creates the material if it doesn't exist yet.
     *
     * @return The material, or null if this block type is AIR
     */
    public Material getMaterial() {
        if (this == AIR) return null;

        if (material == null && texturePath != null) {
            material = new Material();
            TextureAttribute textureAttribute = TextureAttribute.createDiffuse(getTexture());
            material.set(textureAttribute);
        }

        return material;
    }

    /**
     * Gets the texture used for this block type.
     * Loads the texture from file if it hasn't been loaded yet.
     *
     * @return The texture, or null if this block type has no texture
     */
    public Texture getTexture() {
        if (texturePath == null) return null;
        if (texture == null) texture = new Texture(texturePath);
        return texture;
    }

    /**
     * Disposes of all textures used by all block types.
     * Should be called when the application is closing to free resources.
     */
    public static void dispose() {
        for (BlockType type : values()) {
            if (type.texture != null) {
                type.texture.dispose();
                type.texture = null;
            }
        }
    }
}
