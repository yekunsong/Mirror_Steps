package core;

import config.GameConfig;
import javafx.scene.Scene;
import javafx.stage.Stage;
import level.level1.Level1;
import level.level2.Level2;
import level.level3.Level3;
import ui.MenuView;
import ui.SettingsView;

/*
 * Central scene router for the simplified framework.
 *
 * Architectural role:
 * - This class owns all top-level scene switching for the application.
 * - It prevents navigation code from being scattered across UI classes and level
 *   classes, which makes maintenance much easier for a student team.
 *
 * Objects created by this class:
 * - MenuView
 * - SettingsView
 * - Level1
 * - Level2
 * - Level3
 *
 * Dependency direction:
 * - UI and level classes do not create the Stage directly.
 * - Instead, they call back into AppRouter when a scene transition is required.
 *
 * Design constraint:
 * - This class must remain focused on navigation and stage configuration only.
 * - Gameplay physics, input interpretation, collision logic, and level layout must
 *   remain outside this class.
 *
 * Extension guidance:
 * - A future version can add credits, an ending scene, or a loading scene here
 *   without changing the rest of the architecture.
 */
public final class AppRouter {

    private final Stage stage;
    private final GameConfig config;
    private final MenuView menuView = new MenuView();
    private final SettingsView settingsView = new SettingsView();

    public AppRouter(Stage stage, GameConfig config) {
        this.stage = stage;
        this.config = config;
        this.stage.setResizable(false);
    }

    /*
     * Opens the main menu scene.
     *
     * The scene is always shown with the same fixed stage size as the other scenes.
     */
    public void showMenu() {
        Scene scene = menuView.createScene(config, this);
        applyScene(scene, config.getTitle());
    }

    /*
     * Opens the settings scene.
     *
     * In the current simplified version, this scene acts as a shared information and
     * future-extension page rather than a full settings system.
     */
    public void showSettings() {
        Scene scene = settingsView.createScene(config, this);
        applyScene(scene, config.getTitle() + " - Settings");
    }

    /*
     * Opens one of the three playable levels.
     *
     * Invalid level identifiers are resolved safely by returning to Level 1, which
     * avoids application failure caused by an incorrect routing call.
     */
    public void showLevel(int levelId) {
        Scene scene = switch (levelId) {
            case 1 -> new Level1(config, this).createScene();
            case 2 -> new Level2(config, this).createScene();
            case 3 -> new Level3(config, this).createScene();
            default -> new Level1(config, this).createScene();
        };
        applyScene(scene, config.getTitle() + " - Level " + levelId);
    }

    public void closeApp() {
        stage.close();
    }

    private void applyScene(Scene scene, String title) {
        var stylesheetResource = getClass().getResource("/core/application.css");
        if (stylesheetResource != null) {
            scene.getStylesheets().add(stylesheetResource.toExternalForm());
        }

        /*
         * Apply the shared fixed-size window policy before showing the scene.
         *
         * This ensures that menu screens, settings screens, and gameplay scenes are
         * displayed with the same visible dimensions.
         */
        stage.setWidth(config.getWorldWidth());
        stage.setHeight(config.getWorldHeight());
        stage.centerOnScreen();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }
}
