package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

import io.github.some_example_name.block.BlockType;
import io.github.some_example_name.engine.VoxelEngine;
import io.github.some_example_name.player.Camera;
import io.github.some_example_name.ui.FpsCounter;

/**
 * Main application class for the voxel-based game.
 */
public class Main extends ApplicationAdapter {
    private ModelBatch modelBatch;
    private Environment environment;
    private VoxelEngine voxelEngine;

    private static final int WORLD_SIZE = 128;
    private static final int RENDER_DISTANCE = 12;
    private static final float SKY_COLOR_R = 0.4f;
    private static final float SKY_COLOR_G = 0.6f;
    private static final float SKY_COLOR_B = 0.9f;

    @Override
    public void create() {
        Camera.getInstance().init();
        Gdx.graphics.setVSync(true);
        Gdx.graphics.setForegroundFPS(Integer.MAX_VALUE);

        modelBatch = new ModelBatch();
        voxelEngine = new VoxelEngine();

        setupEnvironment();
        voxelEngine.init(WORLD_SIZE, RENDER_DISTANCE);
        FpsCounter.getInstance().init();
    }

    private void setupEnvironment() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.6f, 0.6f, 0.6f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        Camera camera = Camera.getInstance();

        camera.handleCameraMovement();
        clearScreen();

        camera.startFrame(modelBatch);
        voxelEngine.render(modelBatch, environment);
        camera.endFrame(modelBatch);

        FpsCounter.getInstance().render(deltaTime);
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(SKY_COLOR_R, SKY_COLOR_G, SKY_COLOR_B, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void resize(int width, int height) {
        Camera.getInstance().resize(width, height);
        FpsCounter.getInstance().resize(width, height);
    }

    @Override
    public void dispose() {
        if (voxelEngine != null) voxelEngine.dispose();
        if (modelBatch != null) modelBatch.dispose();
        BlockType.dispose();
        FpsCounter.getInstance().dispose();
    }
}
