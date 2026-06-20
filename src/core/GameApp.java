package core;

import audio.AudioManager;
import config.GameConfig;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

/*
 * JavaFX bootstrap layer.
 * This class belongs to the core layer and only creates shared services before
 * handing control to the scene manager.
 * Teammates can extend this class later with global startup services such as save data
 * or settings loading.
 */
public class GameApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        /*
         * Read the actual screen bounds so every scene can scale to the full window.
         */
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        GameConfig gameConfig = new GameConfig(bounds.getWidth(), bounds.getHeight());
        AudioManager audioManager = new AudioManager();
        GameManager gameManager = new GameManager(gameConfig, audioManager);

        var stylesheetResource = getClass().getResource("/core/application.css");
        String stylesheet = stylesheetResource == null ? "" : stylesheetResource.toExternalForm();

        /*
         * SceneManager owns all top-level navigation.
         */
        SceneManager sceneManager = new SceneManager(primaryStage, gameManager, stylesheet);
        sceneManager.showMenu();
    }
}
