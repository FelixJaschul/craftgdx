package io.github.some_example_name.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Matrix4;

/**
 * Manages the player's camera in the voxel world.
 * Handles camera movement, rotation, and rendering setup.
 * Implemented as a singleton to provide global access.
 */
public class Camera {
    private final Vector3 cameraDirection = new Vector3();
    private final Vector3 tempVector = new Vector3();
    private final Vector3 rightAxis = new Vector3();

    private int lastMouseX = -1;
    private int lastMouseY = -1;
    private float currentPitch = 0;
    private float currentYaw = 0;

    private static final float MAX_PITCH = 89.0f;
    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_SPEED = 30.0f;
    private static final float FOV = 70f;
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 300f;
    private static final float INITIAL_X = 1000f;
    private static final float INITIAL_Y = 30f;
    private static final float INITIAL_Z = 1000f;

    private PerspectiveCamera camera;
    private static final Camera INSTANCE = new Camera();

    public void init() {
        camera = new PerspectiveCamera(FOV, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(INITIAL_X, INITIAL_Y, INITIAL_Z);
        camera.near = NEAR_PLANE;
        camera.far = FAR_PLANE;
        camera.update();

        Gdx.input.setCursorCatched(true);
    }

    public void handleCameraMovement() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        float actualSpeed = CAMERA_SPEED * deltaTime;

        updateDirectionVectors();
        handleKeyboardInput(actualSpeed);
        handleMouseLook();

        camera.update();
    }

    private void updateDirectionVectors() {
        cameraDirection.set(camera.direction);
        cameraDirection.y = 0;
        cameraDirection.nor();
    }

    private void handleKeyboardInput(float speed) {
        // Forward/backward movement
        if (Gdx.input.isKeyPressed(Input.Keys.W))
            camera.position.add(tempVector.set(cameraDirection).scl(speed));
        if (Gdx.input.isKeyPressed(Input.Keys.S))
            camera.position.add(tempVector.set(cameraDirection).scl(-speed));

        // Strafe left/right
        if (Gdx.input.isKeyPressed(Input.Keys.A))
            camera.position.add(tempVector.set(cameraDirection).crs(Vector3.Y).nor().scl(-speed));
        if (Gdx.input.isKeyPressed(Input.Keys.D))
            camera.position.add(tempVector.set(cameraDirection).crs(Vector3.Y).nor().scl(speed));

        // Up/down movement
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE))
            camera.position.add(0, speed, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
            camera.position.add(0, -speed, 0);
    }

    private void handleMouseLook() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        if (lastMouseX == -1 || lastMouseY == -1) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return;
        }

        float deltaX = (mouseX - lastMouseX) * MOUSE_SENSITIVITY;
        float deltaY = (mouseY - lastMouseY) * MOUSE_SENSITIVITY;

        updateCameraRotation(deltaX, deltaY);

        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    private void updateCameraRotation(float deltaX, float deltaY) {
        currentYaw -= deltaX;
        currentPitch = Math.max(-MAX_PITCH, Math.min(MAX_PITCH, currentPitch - deltaY));

        camera.direction.set(0, 0, -1);
        camera.up.set(0, 1, 0);

        // Apply rotations
        camera.rotate(Vector3.Y, currentYaw);
        rightAxis.set(camera.direction).crs(camera.up).nor();
        camera.rotate(rightAxis, currentPitch);
    }

    /**
     * Begins a rendering frame with the specified model batch.
     *
     * @param modelBatch The model batch to use for rendering
     */
    public void startFrame(ModelBatch modelBatch) {
        modelBatch.begin(camera);
    }

    /**
     * Ends a rendering frame with the specified model batch.
     *
     * @param modelBatch The model batch to end
     */
    public void endFrame(ModelBatch modelBatch) {
        modelBatch.end();
    }

    /**
     * Handles window resize events by updating the camera viewport.
     *
     * @param width The new width of the window
     * @param height The new height of the window
     */
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    /**
     * Gets the singleton instance of the camera.
     *
     * @return The camera instance
     */
    public static Camera getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the current position of the camera.
     *
     * @return The camera position vector
     */
    public Vector3 getPosition() {
        return camera.position;
    }

    /**
     * Gets the combined projection and view matrix of the camera.
     *
     * @return The combined matrix
     */
    public Matrix4 getCombinedMatrix() {
        return camera.combined;
    }

    /**
     * Gets the underlying LibGDX camera instance.
     *
     * @return The LibGDX camera
     */
    public com.badlogic.gdx.graphics.Camera getCamera() {
        return camera;
    }
}
