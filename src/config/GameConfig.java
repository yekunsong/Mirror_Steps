package config;

import javafx.scene.input.KeyCode;

/*
 * Global application settings.
 * This config object belongs to the shared config layer and stores values used by
 * the bootstrap, scene manager, and menu screens.
 * Teammates can extend it later with additional global options such as sound
 * defaults or accessibility flags.
 */
public final class GameConfig {

    private final String title;
    private final double stageWidth;
    private final double stageHeight;
    private final ControlConfig controlConfig = new ControlConfig();

    public GameConfig(double stageWidth, double stageHeight) {
        this.title = "Mirror Steps";
        this.stageWidth = stageWidth;
        this.stageHeight = stageHeight;
    }

    public String getTitle() {
        return title;
    }

    public double getStageWidth() {
        return stageWidth;
    }

    public double getStageHeight() {
        return stageHeight;
    }

    public ControlConfig getControlConfig() {
        return controlConfig;
    }

    /*
     * Shared input mapping for the whole game.
     * This nested config keeps the key binding data close to the global config so
     * the project does not need a separate tiny file for a single responsibility.
     */
    public static final class ControlConfig {

        public boolean isMoveLeft(KeyCode keyCode) {
            return keyCode == KeyCode.A || keyCode == KeyCode.LEFT;
        }

        public boolean isMoveRight(KeyCode keyCode) {
            return keyCode == KeyCode.D || keyCode == KeyCode.RIGHT;
        }

        public boolean isJump(KeyCode keyCode) {
            return keyCode == KeyCode.W || keyCode == KeyCode.UP || keyCode == KeyCode.SPACE;
        }

        public boolean isRestart(KeyCode keyCode) {
            return keyCode == KeyCode.R;
        }

        public boolean isBack(KeyCode keyCode) {
            return keyCode == KeyCode.ESCAPE;
        }
    }
}
