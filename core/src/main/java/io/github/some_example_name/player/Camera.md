# Camera.java Documentation

The `Camera` class manages the player's view in the 3D world, implementing a first-person camera with mouse look and keyboard movement controls.

## Class Variables
- `camera`: A LibGDX `PerspectiveCamera` that defines the player's view
- `cameraDirection` and `tempVector`: Temporary vectors used for movement calculations
- `lastMouseX` and `lastMouseY`: Track the previous mouse position for calculating movement
- `currentPitch` and `currentYaw`: Store the camera's rotation angles
- `MAX_PITCH`: Constant defining the maximum up/down angle (prevents over-rotation)
- `INSTANCE`: Singleton instance of the Camera

## Methods

### init()
Initializes the camera:
- Creates a perspective camera with 70° field of view
- Sets initial position to (16, 10, 16)
- Sets near and far clipping planes
- Captures the mouse cursor for FPS-style controls

### handleCameraMovement()
Updates camera position based on keyboard input:
- Calculates movement speed based on frame time
- Handles WASD keys for forward/backward/strafe movement
- Handles Space/Shift for up/down movement
- Calls `handleMouseLook()` to process mouse input for rotation
- Updates the camera matrices

### handleMouseLook()
Processes mouse movement to rotate the camera:
- Calculates mouse movement delta since last frame
- Updates yaw (left/right) and pitch (up/down) angles
- Clamps pitch to prevent flipping
- Applies rotation to the camera direction

### startFrame(ModelBatch) and endFrame(ModelBatch)
Helper methods to begin and end a rendering frame with the camera's perspective

### resize(int width, int height)
Updates the camera when the window is resized

### getInstance()
Returns the singleton instance of the Camera

### getPosition()
Returns the camera's current position vector
