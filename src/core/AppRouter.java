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
 * Simple scene router for the project.
 *
 * This class is not a gameplay class and does not contain physics or level rules.
 * Its only job is to switch between screens while keeping the Stage configuration
 * consistent.
 *
 * Relationship notes:
 * - AppRouter creates MenuView and SettingsView directly
 * - AppRouter creates Level1/2/3 directly
 * - Levels call back into AppRouter when they want to switch scene
 *
 * Why keep this file:
 * - it removes scene-switching code from UI and level files
 * - it keeps startup and navigation simple without reintroducing old managers
 *
 * Future extension directions:
 * - add a Credits page
 * - add a Game Complete page
 * - add a simple loading scene if the project grows later
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
     * Opens the main menu with the same fixed size as every other screen.
     */
    public void showMenu() {
        Scene scene = menuView.createScene(config, () -> showLevel(1), this::showSettings, stage::close);
        applyScene(scene, config.getTitle());
    }

    /*
     * Opens the simplified Settings page.
     * This page currently acts as a shared UI placeholder and documentation page.
     */
    public void showSettings() {
        Scene scene = settingsView.createScene(config, this::showMenu);
        applyScene(scene, config.getTitle() + " - Settings");
    }

    /*
     * Creates one of the three level scenes.
     * If an invalid level id is given, we safely return to Level 1.
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

    private void applyScene(Scene scene, String title) {
        var stylesheetResource = getClass().getResource("/core/application.css");
        if (stylesheetResource != null) {
            scene.getStylesheets().add(stylesheetResource.toExternalForm());
        }

        /*
         * Force every scene to use the same window size.
         * This is the main part of the fixed-size refactor.
         */
        stage.setWidth(config.getStageWidth());
        stage.setHeight(config.getStageHeight());
        stage.centerOnScreen();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }
}
