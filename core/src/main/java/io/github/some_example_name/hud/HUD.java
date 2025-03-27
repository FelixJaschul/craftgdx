package io.github.some_example_name.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class HUD implements Disposable {
    private static final HUD INSTANCE = new HUD();

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;

    private BitmapFont minecraftFont;

    private int frameCounter;
    private float timeCounter;
    private int lastFps;

    private boolean initialized = false;

    private HUD() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        batch = new SpriteBatch();

        frameCounter = 0;
        timeCounter = 0;
        lastFps = 0;
    }

    public static HUD getInstance() {
        return INSTANCE;
    }

    public void init() {
        if (initialized) return;

        minecraftFont = new BitmapFont();
        minecraftFont.setColor(Color.WHITE);

        initialized = true;
    }

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

    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(width / 2f, height / 2f, 0);
    }

    @Override
    public void dispose() {
        if (!initialized) return;

        batch.dispose();
        minecraftFont.dispose();

        initialized = false;
    }
}
