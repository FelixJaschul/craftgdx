package io.github.some_example_name.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;

public class Camera {
    private PerspectiveCamera camera;

    // Camera movement
    private final Vector3 cameraDirection = new Vector3();
    private final Vector3 tempVector = new Vector3();
    private float cameraSpeed = 5.0f;

    // Mouse control variables
    private float mouseSensitivity = 0.2f;
    private int lastMouseX = -1;
    private int lastMouseY = -1;

    private float currentPitch = 0;
    private float currentYaw = 0;
    private static final float MAX_PITCH = 89.0f;


    private static final Camera INSTANCE = new Camera();

    public static Camera getInstance() {
        return INSTANCE;
    }

    public void init() {
        // Set up camera
        camera = new PerspectiveCamera(70, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(16f, 10f, 16f);
        camera.near = 0.1f;
        camera.far = 300f;
        camera.update();

        // Center the cursor
        Gdx.input.setCursorCatched(true);
    }

    public void handleCameraMovement() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        float actualSpeed = cameraSpeed * deltaTime;

        // Get camera direction vectors
        cameraDirection.set(camera.direction);
        cameraDirection.y = 0;
        cameraDirection.nor();
        tempVector.set(cameraDirection).crs(Vector3.Y).nor();

        // Forward/backward
        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.position.add(tempVector.set(cameraDirection).scl(actualSpeed));
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.position.add(tempVector.set(cameraDirection).scl(-actualSpeed));

        // Strafe left/right
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.position.add(tempVector.set(cameraDirection).crs(Vector3.Y).nor().scl(-actualSpeed));
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.position.add(tempVector.set(cameraDirection).crs(Vector3.Y).nor().scl(actualSpeed));

        // Up/down
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) camera.position.add(0, actualSpeed, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) camera.position.add(0, -actualSpeed, 0);

        // Handle mouse movement for camera rotation
        handleMouseLook();

        camera.update();
    }

    private void handleMouseLook() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        // Calculate how much the mouse has moved
        float deltaX = (mouseX - lastMouseX) * mouseSensitivity;
        float deltaY = (mouseY - lastMouseY) * mouseSensitivity;

        currentYaw -= deltaX;
        currentPitch -= deltaY;

        if (currentPitch > MAX_PITCH) currentPitch = MAX_PITCH;
        else if (currentPitch < -MAX_PITCH) currentPitch = -MAX_PITCH;

        camera.direction.set(0, 0, -1);
        camera.up.set(0, 1, 0);

        camera.rotate(Vector3.Y, currentYaw);

        Vector3 rightAxis = new Vector3();
        rightAxis.set(camera.direction).crs(camera.up).nor();
        camera.rotate(rightAxis, currentPitch);

        // Store current mouse position for next frame
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    public void startFrame(ModelBatch modelBatch) {
        modelBatch.begin(camera);
    }

    public void endFrame(ModelBatch modelBatch) {
        modelBatch.end();
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }
}
