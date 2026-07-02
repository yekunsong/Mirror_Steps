package core;

import java.io.File;

import config.GameConfig;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
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
import level.level13.Level13;
import level.level14.Level14;
import ui.LevelsView;
import ui.MenuView;
import ui.SettingsView;

public final class AppRouter {

    private final Stage stage;
    private final GameConfig config;
    private final MenuView menuView = new MenuView();
    private final SettingsView settingsView = new SettingsView();
    private MediaPlayer mediaPlayer;

    public static String resourceUri(String relativePath) {
        return new File(relativePath).toURI().toString();
    }

    public AppRouter(Stage stage, GameConfig config) {
        this.stage = stage;
        this.config = config;
        this.stage.setResizable(false);
    }

    public void initMusic() {
        Media music = new Media(resourceUri("Media/music.mp3"));
        mediaPlayer = new MediaPlayer(music);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.play();
    }

    public void showMenu() {
        Scene scene = menuView.createScene(config, this);
        applyScene(scene, config.getTitle());
    }

    public void showSettings() {
        Scene scene = settingsView.createScene(config, this, mediaPlayer);
        applyScene(scene, config.getTitle() + " - Settings");
    }

    public void showLevels() {
        LevelsView levelsView = new LevelsView();
        Scene scene = levelsView.createScene(config, this);
        applyScene(scene, config.getTitle() + " - Levels");
    }

    public void showLevel(int levelId) {
        Scene scene = switch (levelId) {
            case 1  -> new Level1(config, this).createScene();
            case 2  -> new Level2(config, this).createScene();
            case 3  -> new Level3(config, this).createScene();
            case 4  -> new Level4(config, this).createScene();
            case 5  -> new Level5(config, this).createScene();
            case 6  -> new Level6(config, this).createScene();
            case 7  -> new Level7(config, this).createScene();
            case 8  -> new Level8(config, this).createScene();
            case 9  -> new Level9(config, this).createScene();
            case 10 -> new Level10(config, this).createScene();
            case 11 -> new Level11(config, this).createScene();
            case 12 -> new Level12(config, this).createScene();
            case 13 -> new Level13(config, this).createScene();
            case 14 -> new Level14(config, this).createScene();
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
        stage.setWidth(config.getWorldWidth());
        stage.setHeight(config.getWorldHeight());
        stage.centerOnScreen();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }
}
