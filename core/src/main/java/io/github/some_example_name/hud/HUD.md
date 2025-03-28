# HUD.java Documentation

The `HUD` class manages the 2D overlay interface that displays information to the player, such as FPS counter.

## Class Variables
- `INSTANCE`: Singleton instance of the HUD
- `camera`: An orthographic camera for 2D rendering
- `viewport`: Manages the 2D viewport scaling
- `batch`: A SpriteBatch for rendering 2D elements
- `minecraftFont`: Font used for text rendering
- `frameCounter`, `timeCounter`, `lastFps`: Used to calculate and display FPS
- `initialized`: Flag to prevent multiple initializations

## Methods

### Constructor
Sets up the basic components:
- Creates an orthographic camera
- Creates a viewport that fits the screen
- Creates a SpriteBatch for rendering
- Initializes FPS counter variables

### getInstance()
Returns the singleton instance of the HUD

### init()
Initializes the HUD components:
- Creates and configures the font
- Sets the initialized flag

### render(float delta)
Renders the HUD elements:
- Updates the FPS counter
- Renders the FPS text in the top-left corner

### resize(int width, int height)
Updates the viewport when the window is resized

### dispose()
Cleans up resources when the game closes:
- Disposes the SpriteBatch and font
- Resets the initialized flag
