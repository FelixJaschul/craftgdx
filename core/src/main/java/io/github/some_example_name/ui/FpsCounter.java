package io.github.some_example_name.ui;

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
 */
public class FpsCounter implements Disposable {
    private static final FpsCounter INSTANCE = new FpsCounter();
    private static final float FPS_UPDATE_INTERVAL = 1.0f;
    private static final int FPS_X_POSITION = 10;
    private static final Color FONT_COLOR = Color.WHITE;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;

    private BitmapFont font;
    private int frameCounter;
    private float timeCounter;
    private int lastFps;
    private boolean initialized = false;

    private FpsCounter() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        batch = new SpriteBatch();
    }

    public static FpsCounter getInstance() {
        return INSTANCE;
    }

    public void init() {
        if (initialized) return;

        font = new BitmapFont();
        font.setColor(FONT_COLOR);
        initialized = true;
    }

    public void render(float delta) {
        if (!initialized) return;

        updateFpsCounter(delta);
        renderHudElements();
    }

    private void updateFpsCounter(float delta) {
        timeCounter += delta;
        frameCounter++;

        if (timeCounter >= FPS_UPDATE_INTERVAL) {
            lastFps = frameCounter;
            frameCounter = 0;
            timeCounter = 0;
        }
    }

    private void renderHudElements() {
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        font.draw(batch, "FPS: " + lastFps, FPS_X_POSITION, Gdx.graphics.getHeight() - FPS_X_POSITION);
        batch.end();
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(width / 2f, height / 2f, 0);
    }

    @Override
    public void dispose() {
        if (!initialized) return;

        batch.dispose();
        font.dispose();
        initialized = false;
    }
}
