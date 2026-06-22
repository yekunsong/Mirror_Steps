package core;

import config.GameConfig;
import javafx.application.Application;
import javafx.stage.Stage;

/*
 * JavaFX application entry point.
 *
 * Architectural role:
 * - This class is the only startup class that JavaFX needs to launch the project.
 * - Its responsibility is intentionally narrow: create shared startup objects and
 *   transfer control to the scene router.
 *
 * Call sequence:
 * 1. JavaFX calls `start(Stage stage)`.
 * 2. The application creates a single shared GameConfig instance.
 * 3. The application creates a single AppRouter instance.
 * 4. The router opens the initial Menu scene.
 *
 * Relationship note:
 * - Main does not contain gameplay logic and does not manage scene switching itself.
 * - Main is not a parent of AppRouter; it only creates the router and passes the
 *   JavaFX Stage into it.
 *
 * Extension guidance:
 * - If startup behavior must be expanded later, this is the correct place to add
 *   tasks such as icon loading, save file loading, or splash screen handling.
 */
public final class Main extends Application {

    private static final double FIXED_STAGE_WIDTH = 1280;
    private static final double FIXED_STAGE_HEIGHT = 720;

    public static void main(String[] args) {
        launch(args);
        int a=1;
    }

    @Override
    public void start(Stage stage) {
        GameConfig config = new GameConfig(FIXED_STAGE_WIDTH, FIXED_STAGE_HEIGHT);
        AppRouter router = new AppRouter(stage, config);
        router.showMenu();
    }
}
