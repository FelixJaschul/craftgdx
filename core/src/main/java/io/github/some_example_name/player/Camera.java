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
    /** Temporary vector for camera direction calculations */
    private final Vector3 cameraDirection = new Vector3();
    /** Temporary vector for various calculations */
    private final Vector3 tempVector = new Vector3();

    /** Last mouse X position for calculating mouse movement */
    private int lastMouseX = -1;
    /** Last mouse Y position for calculating mouse movement */
    private int lastMouseY = -1;

    /** Current pitch angle of the camera (up/down rotation) */
    private float currentPitch = 0;
    /** Current yaw angle of the camera (left/right rotation) */
    private float currentYaw = 0;
    /** Maximum pitch angle to prevent camera flipping */
    private static final float MAX_PITCH = 89.0f;

    /** The LibGDX camera instance */
    private PerspectiveCamera camera;
    /** Singleton instance of the camera */
    private static final Camera INSTANCE = new Camera();

    /**
     * Initializes the camera with default settings.
     * Sets up the perspective, position, and input handling.
     */
    public void init() {
        camera = new PerspectiveCamera(70, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(1000f, 30f, 1000f);
        camera.near = 0.1f;
        camera.far = 300f;
        camera.update();

        Gdx.input.setCursorCatched(true);
    }

    /**
     * Updates the camera position and rotation based on user input.
     * Should be called once per frame in the render loop.
     */
    public void handleCameraMovement() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        float cameraSpeed = 30.0f;
        float actualSpeed = cameraSpeed * deltaTime;

        // Get camera direction vectors
        cameraDirection.set(camera.direction);
        cameraDirection.y = 0;
        cameraDirection.nor();
        tempVector.set(cameraDirection).crs(Vector3.Y).nor();

        // Forward/backward movement
        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.position.add(tempVector.set(cameraDirection).scl(actualSpeed));
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.position.add(tempVector.set(cameraDirection).scl(-actualSpeed));

        // Strafe left/right
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.position.add(tempVector.set(cameraDirection).crs(Vector3.Y).nor().scl(-actualSpeed));
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.position.add(tempVector.set(cameraDirection).crs(Vector3.Y).nor().scl(actualSpeed));

        // Up/down movement
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) camera.position.add(0, actualSpeed, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) camera.position.add(0, -actualSpeed, 0);

        // Handle mouse movement for camera rotation
        handleMouseLook();

        camera.update();
    }

    /**
     * Handles mouse input for camera rotation.
     * Updates the camera direction based on mouse movement.
     */
    private void handleMouseLook() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        // Calculate how much the mouse has moved
        float mouseSensitivity = 0.2f;
        float deltaX = (mouseX - lastMouseX) * mouseSensitivity;
        float deltaY = (mouseY - lastMouseY) * mouseSensitivity;

        currentYaw -= deltaX;
        currentPitch -= deltaY;

        // Clamp pitch to prevent camera flipping
        if (currentPitch > MAX_PITCH) currentPitch = MAX_PITCH;
        else if (currentPitch < -MAX_PITCH) currentPitch = -MAX_PITCH;

        // Reset camera direction and up vector
        camera.direction.set(0, 0, -1);
        camera.up.set(0, 1, 0);

        // Apply yaw rotation (around Y axis)
        camera.rotate(Vector3.Y, currentYaw);

        // Apply pitch rotation (around X axis)
        Vector3 rightAxis = new Vector3();
        rightAxis.set(camera.direction).crs(camera.up).nor();
        camera.rotate(rightAxis, currentPitch);

        // Store current mouse position for next frame
        lastMouseX = mouseX;
        lastMouseY = mouseY;
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
