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
        Gdx.graphics.setVSync(true);
        Gdx.graphics.setForegroundFPS(Integer.MAX_VALUE);
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.6f, 0.6f, 0.6f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        int worldSize = 256;
        int renderDistance = 16;
        voxelEngine = new VoxelEngine();
        voxelEngine.init(worldSize, renderDistance);

        HUD.getInstance().init();
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        Camera.getInstance().handleCameraMovement();

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
        if (modelBatch != null) modelBatch.dispose();
        BlockType.dispose();
        if (voxelEngine != null) voxelEngine.dispose();
        HUD.getInstance().dispose();
    }
}
