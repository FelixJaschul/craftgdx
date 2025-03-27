package io.github.some_example_name.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import imgui.*;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

public class ImGuiManager {
    private static InputProcessor tmpProcessor;

    private static final ImGuiManager INSTANCE = new ImGuiManager();

    private static ImGuiImplGlfw imGuiGlfw;
    private static ImGuiImplGl3 imGuiGl3;

    private boolean initialized = false;

    private ImGuiManager() {}

    public static ImGuiManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        if (initialized) return;

        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();

        // Initialize ImGui
        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard); // Enable keyboard controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable); // Enable docking
        io.setConfigViewportsNoTaskBarIcon(true);

        io.getFonts().addFontDefault();
        io.getFonts().build();

        // Initialize LWJGL3 implementation
        long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 120");

        initialized = true;
    }

    public void beginFrame() {
        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGlfw.newFrame();
        ImGui.newFrame();
    }

    public void endFrame() {
        imGuiGl3 = new ImGuiImplGl3();
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void dispose() {
        if (!initialized) return;

        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();

        initialized = false;
    }

    public void resize(int width, int height) {
        if (!initialized) return;

        ImGuiIO io = ImGui.getIO();
        io.setDisplaySize(width, height);
    }
}
