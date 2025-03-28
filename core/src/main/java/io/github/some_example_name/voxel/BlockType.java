package io.github.some_example_name.voxel;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;

public enum BlockType {
    AIR(null, false),
    STONE("Blocks/Cobblestone.jpg", true),
    DIRT("Blocks/Dirt.jpg", true),
    GRASS("Blocks/Grass.jpg", true),
    SAND("Blocks/Sand.jpg", true);

    private final boolean isSolid;
    private final String texturePath;
    private Material material;
    private Texture texture;

    BlockType(String texturePath, boolean isSolid) {
        this.isSolid = isSolid;
        this.texturePath = texturePath;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public boolean isSolid() {
        return isSolid;
    }

    public Material getMaterial() {
        if (this == AIR) return null;

        if (material == null && texturePath != null) {
            material = new Material();
            TextureAttribute textureAttribute = TextureAttribute.createDiffuse(getTexture());
            material.set(textureAttribute);
        }

        return material;
    }

    public Texture getTexture() {
        if (texturePath == null) return null;
        if (texture == null) texture = new Texture(texturePath);
        return texture;
    }

    public static void disposeTextures() {
        for (BlockType type : values()) {
            if (type.texture != null) {
                type.texture.dispose();
                type.texture = null;
            }
        }
    }
}
