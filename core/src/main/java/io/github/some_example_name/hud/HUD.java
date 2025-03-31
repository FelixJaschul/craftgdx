package io.github.some_example_name.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Manages the Heads-Up Display (HUD) for the game.
 * Displays information like FPS counter and potentially other game stats.
 * Implemented as a singleton to provide global access.
 */
public class HUD implements Disposable {
    /** Singleton instance of the HUD */
    private static final HUD INSTANCE = new HUD();

    /** Camera for 2D rendering */
    private final OrthographicCamera camera;
    /** Viewport for handling different screen sizes */
    private final Viewport viewport;
    /** Sprite batch for rendering 2D elements */
    private final SpriteBatch batch;

    /** Font for rendering text */
    private BitmapFont minecraftFont;

    /** Counter for frames rendered in the current second */
    private int frameCounter;
    /** Counter for elapsed time since last FPS update */
    private float timeCounter;
    /** Last calculated FPS value */
    private int lastFps;

    /** Flag indicating if the HUD has been initialized */
    private boolean initialized = false;

    /**
     * Private constructor to enforce singleton pattern.
     * Initializes the camera, viewport, and sprite batch.
     */
    private HUD() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        batch = new SpriteBatch();

        frameCounter = 0;
        timeCounter = 0;
        lastFps = 0;
    }

    /**
     * Gets the singleton instance of the HUD.
     *
     * @return The HUD instance
     */
    public static HUD getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes the HUD resources.
     * Creates the font and sets its properties.
     */
    public void init() {
        if (initialized) return;

        minecraftFont = new BitmapFont();
        minecraftFont.setColor(Color.WHITE);

        initialized = true;
    }

    /**
     * Renders the HUD for the current frame.
     * Updates and displays the FPS counter.
     *
     * @param delta Time elapsed since the last frame in seconds
     */
    public void render(float delta) {
        if (!initialized) return;

        // Update FPS counter
        timeCounter += delta;
        frameCounter++;
        if (timeCounter >= 1.0f) {
            lastFps = frameCounter;
            frameCounter = 0;
            timeCounter = 0;
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        minecraftFont.draw(batch, "FPS: " + lastFps, 10, Gdx.graphics.getHeight() - 10);
        batch.end();
    }

    /**
     * Handles window resize events by updating the viewport.
     *
     * @param width The new width of the window
     * @param height The new height of the window
     */
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(width / 2f, height / 2f, 0);
    }

    /**
     * Disposes of all resources used by the HUD.
     * Should be called when the application is closing.
     */
    @Override
    public void dispose() {
        if (!initialized) return;

        batch.dispose();
        minecraftFont.dispose();

        initialized = false;
    }
}
