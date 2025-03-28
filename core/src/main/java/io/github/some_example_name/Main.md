# Main.java Documentation

The `Main` class is the entry point of the application and extends LibGDX's `ApplicationAdapter`. It orchestrates the initialization, rendering, and disposal of all game components.

## Class Variables
- `modelBatch`: A LibGDX `ModelBatch` used for rendering 3D models
- `environment`: A LibGDX `Environment` that defines lighting conditions for the 3D world
- `voxelEngine`: The engine responsible for managing the voxel world

## Methods

### create()
Initializes the game when it starts:
- Initializes the camera singleton
- Disables VSync and sets maximum FPS for better performance
- Sets the game to fullscreen mode
- Creates a `ModelBatch` for rendering 3D models
- Sets up the lighting environment with ambient light and directional light
- Loads block textures using LibGDX's `AssetManager`
- Initializes the voxel engine with specified world size and render distance
- Initializes the HUD (Heads-Up Display)

### render()
Called every frame to update and render the game:
- Gets the time elapsed since the last frame
- Updates camera position based on player input
- Clears the screen with a sky blue color
- Renders the voxel world using the camera and environment settings
- Renders the HUD with FPS information

### resize(int width, int height)
Handles window resize events:
- Updates the camera's viewport
- Updates the HUD's viewport

### dispose()
Cleans up resources when the game closes: - not necessary but helpful
- Disposes the model batch
- Disposes the voxel engine - not called 
- Disposes block type textures
- Disposes the HUD
