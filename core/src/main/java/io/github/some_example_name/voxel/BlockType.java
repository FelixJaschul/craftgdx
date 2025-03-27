package io.github.some_example_name.voxel;

import com.badlogic.gdx.graphics.Texture;

public enum BlockType {
    AIR(null, false),
    STONE("Blocks/Cobblestone.jpg", true),
    DIRT("Blocks/Dirt.jpg", true),;

    private final boolean isSolid;
    private final String texturePath;
    private Texture texture;

    BlockType(String texturePath, boolean isSolid) {
        this.isSolid = isSolid;
        this.texturePath = texturePath;
    }

    public Texture getTexture() {
        if (texture == null && texturePath != null)
            texture = new Texture(texturePath);
        return texture;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public boolean isSolid() {
        return isSolid;
    }

    public static void dispose() {
        for (BlockType type : values()) {
            if (type.texture != null) {
                type.texture.dispose();
                type.texture = null;
            }
        }
    }
}
