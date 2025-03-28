package io.github.some_example_name.voxel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
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
        if (texture == null) {
            try {
                texture = new Texture(texturePath);
            } catch (Exception e) {
                // If texture file is missing, create a 1x1 white texture as fallback
                Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pixmap.setColor(getDefaultColor());
                pixmap.fill();
                texture = new Texture(pixmap);
                pixmap.dispose();
                System.out.println("Warning: Could not load texture " + texturePath + ". Using fallback.");
            }
        }
        return texture;
    }

    private Color getDefaultColor() {
        // Return a different color for each block type
        switch (this) {
            case STONE: return new Color(0.5f, 0.5f, 0.5f, 1f); // Gray
            case DIRT: return new Color(0.6f, 0.3f, 0f, 1f);    // Brown
            case GRASS: return new Color(0.3f, 0.7f, 0.2f, 1f); // Green
            case SAND: return new Color(0.9f, 0.9f, 0.6f, 1f);  // Light yellow
            default: return Color.WHITE;
        }
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
