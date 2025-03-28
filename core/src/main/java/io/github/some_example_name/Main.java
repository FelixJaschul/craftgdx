package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import io.github.some_example_name.voxel.BlockType;
import io.github.some_example_name.voxel.VoxelEngine;
import io.github.some_example_name.player.Camera;
import io.github.some_example_name.hud.HUD;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private ModelBatch modelBatch;
    private Environment environment;
    private VoxelEngine voxelEngine;

    @Override
    public void create() {
        Camera.getInstance().init();
        Gdx.graphics.setVSync(false);
        Gdx.graphics.setForegroundFPS(Integer.MAX_VALUE);
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.6f, 0.6f, 0.6f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        AssetManager assetManager = new AssetManager();
        // Add error handling for texture loading
        assetManager.setErrorListener(new AssetErrorListener() {
            @Override
            public void error(AssetDescriptor asset, Throwable throwable) {
                System.err.println("Error loading asset: " + asset.fileName);
                // Continue loading other assets
            }
        });

        for (BlockType type : BlockType.values()) {
            if (type.getTexturePath() != null) {
                try {
                    assetManager.load(type.getTexturePath(), Texture.class);
                } catch (Exception e) {
                    System.err.println("Could not queue texture: " + type.getTexturePath());
                }
            }
        }

        try {
            assetManager.finishLoading();
        } catch (Exception e) {
            System.err.println("Error during texture loading: " + e.getMessage());
        }

        int worldSize = 16;
        int renderDistance = 4;
        voxelEngine = new VoxelEngine();
        voxelEngine.init(worldSize, renderDistance);

        HUD.getInstance().init();
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        Camera.getInstance().handleCameraMovement();

        // Clear the screen
        Gdx.gl.glClearColor(0.4f, 0.6f, 0.9f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Render the voxel world
        Camera.getInstance().startFrame(modelBatch);
        voxelEngine.render(modelBatch, environment);
        Camera.getInstance().endFrame(modelBatch);

        HUD.getInstance().render(deltaTime);
    }

    @Override
    public void resize(int width, int height) {
        Camera.getInstance().resize(width, height);
        HUD.getInstance().resize(width, height);
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        BlockType.disposeTextures();
    }
}
