package core;

import config.GameConfig;
import javafx.scene.Scene;
import javafx.stage.Stage;
import level.level1.Level1;
import level.level2.Level2;
import level.level3.Level3;
import level.level4.Level4;
import level.level5.Level5;
import level.level6.Level6;
import level.level7.Level7;
import level.level8.Level8;
import level.level9.Level9;
import level.level10.Level10;
import level.level11.Level11;
import level.level12.Level12;
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
            case 4 -> new Level4(config, this).createScene();
            case 5 -> new Level5(config, this).createScene();
            case 6 -> new Level6(config, this).createScene();
            case 7 -> new Level7(config, this).createScene();
            case 8 -> new Level8(config, this).createScene();
            case 9 -> new Level9(config, this).createScene();
            case 10 -> new Level10(config, this).createScene();
            case 11 -> new Level11(config, this).createScene();
            case 12 -> new Level12(config, this).createScene();
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
