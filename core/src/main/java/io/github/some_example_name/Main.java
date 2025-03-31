package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.utils.ObjectMap;

import io.github.some_example_name.voxel.BlockType;
import io.github.some_example_name.voxel.Chunk;
import io.github.some_example_name.voxel.ChunkPosition;
import io.github.some_example_name.voxel.VoxelEngine;
import io.github.some_example_name.player.Camera;
import io.github.some_example_name.hud.HUD;

import java.io.File;
import java.io.IOException;

/**
 * Main application class for the voxel-based game.
 * This class initializes and manages the core components of the game,
 * including the voxel engine, camera, and HUD.
 *
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class Main extends ApplicationAdapter {
    /** Handles rendering of 3D models */
    private ModelBatch modelBatch;
    /** Manages lighting and environment settings */
    private Environment environment;
    /** Core engine for voxel world generation and rendering */
    private VoxelEngine voxelEngine;

    /**
     * Initializes the game components when the application starts.
     * Sets up the camera, graphics settings, voxel engine, lighting,
     * and HUD components.
     */
    @Override
    public void create() {
        Camera.getInstance().init();
        Gdx.graphics.setVSync(true);
        Gdx.graphics.setForegroundFPS(Integer.MAX_VALUE);
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

        modelBatch = new ModelBatch();
        voxelEngine = new VoxelEngine();

        // Set up lighting environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.6f, 0.6f, 0.6f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        // Initialize voxel world parameters
        int worldSize = 128;
        int renderDistance = 16;
        voxelEngine.init(worldSize, renderDistance);

        HUD.getInstance().init();
    }

    /**
     * Main game loop that updates and renders the game state.
     * Called once per frame to handle camera movement, render the voxel world,
     * and update the HUD.
     */
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

        // Render the HUD
        HUD.getInstance().render(deltaTime);
    }

    /**
     * Handles window resize events by updating the camera and HUD viewports.
     *
     * @param width The new width of the window
     * @param height The new height of the window
     */
    @Override
    public void resize(int width, int height) {
        Camera.getInstance().resize(width, height);
        HUD.getInstance().resize(width, height);
    }

    /**
     * Cleans up resources when the application is closed.
     * Disposes of the voxel engine, model batch, block types, and HUD.
     */
    @Override
    public void dispose() {
        if (voxelEngine != null) voxelEngine.dispose();
        if (modelBatch != null) modelBatch.dispose();
        BlockType.dispose();
        HUD.getInstance().dispose();
    }
}
