package core;

import config.GameConfig;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import level.LevelFactory;
import level.LevelModule;
import level.LevelRuntime;
import ui.LevelSelectView;
import ui.MenuView;

/*
 * Central scene switching controller.
 * This class belongs to the core layer and owns all top-level navigation between
 * the menu, level select screen, and active gameplay scenes.
 * Teammates can extend it later with extra screens such as credits or settings.
 */
public final class SceneManager {

    private final Stage stage;
    private final GameManager gameManager;
    private final String stylesheetUrl;
    private final MenuView menuView = new MenuView();
    private final LevelSelectView levelSelectView = new LevelSelectView();

    public SceneManager(Stage stage, GameManager gameManager, String stylesheetUrl) {
        this.stage = stage;
        this.gameManager = gameManager;
        this.stylesheetUrl = stylesheetUrl;
        this.stage.setResizable(true);
        this.stage.setFullScreenExitHint("");
        this.stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    }

    public void showMenu() {
        stopCurrentLevel();
        gameManager.setState(GameManager.GameState.MENU);
        gameManager.getAudioManager().playMenuMusic();

        GameConfig config = gameManager.getGameConfig();
        Scene scene = menuView.createScene(
                config,
                () -> showLevel(LevelFactory.getFirstLevelId()),
                this::showLevelSelect,
                stage::close
        );
        applyStyles(scene);

        stage.setTitle(config.getTitle());
        stage.setScene(scene);
        stage.show();
        stage.setFullScreen(true);
    }

    public void showLevelSelect() {
        stopCurrentLevel();
        gameManager.setState(GameManager.GameState.LEVEL_SELECT);
        gameManager.getAudioManager().playMenuMusic();

        GameConfig config = gameManager.getGameConfig();
        Scene scene = levelSelectView.createScene(
                config,
                LevelFactory.getAvailableModules(),
                this::showLevel,
                this::showMenu
        );
        applyStyles(scene);

        stage.setTitle(config.getTitle());
        stage.setScene(scene);
        stage.show();
        stage.setFullScreen(true);
    }

    public void showLevel(int levelId) {
        stopCurrentLevel();
        gameManager.setState(GameManager.GameState.PLAYING);

        LevelModule module = LevelFactory.createLevel(levelId);
        LevelRuntime level = new LevelRuntime(gameManager, module);
        gameManager.setCurrentLevel(level);
        gameManager.getAudioManager().playLevelMusic(level.getMusicTrackId());

        Scene scene = level.createScene(this::showLevel, this::showLevelSelect);
        applyStyles(scene);

        stage.setTitle(level.getTitle());
        stage.setScene(scene);
        stage.show();
        stage.setFullScreen(true);
        level.refreshViewport(stage.getWidth(), stage.getHeight());
        level.start();
    }

    private void stopCurrentLevel() {
        LevelRuntime currentLevel = gameManager.getCurrentLevel();
        if (currentLevel != null) {
            currentLevel.stop();
            gameManager.setCurrentLevel(null);
        }
    }

    private void applyStyles(Scene scene) {
        if (stylesheetUrl != null && !stylesheetUrl.isBlank()) {
            scene.getStylesheets().add(stylesheetUrl);
        }
    }
}
