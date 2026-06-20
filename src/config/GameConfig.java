package config;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

/*
 * Global application settings for the whole project.
 *
 * This class is intentionally kept as the single shared configuration entry point
 * so teammates do not need to search across many files to find constants.
 *
 * Current responsibilities:
 * - fixed window size used by every screen
 * - shared world size used by every level
 * - player movement constants
 * - shared colors
 * - key bindings
 *
 * Team rule:
 * - if a value affects every level or every screen, add it here
 * - if a value only affects one specific level, keep it inside that level file
 *
 * Future extension directions:
 * - add volume defaults
 * - add theme colors for more screens
 * - add difficulty presets
 * - add a different fixed window size for presentation/demo mode
 */
public final class GameConfig {

    private final String title;
    private final double stageWidth;
    private final double stageHeight;
    private final double worldWidth;
    private final double worldHeight;
    private final double playerWidth = 36;
    private final double playerHeight = 48;
    private final double moveSpeed = 220;
    private final double jumpVelocity = -360;
    private final double gravity = 720;
    private final Color blockColor = Color.web("#64748b");
    private final Color playerColor = Color.web("#22c55e");
    private final Color goalColor = Color.web("#f59e0b");
    private final ControlConfig controlConfig = new ControlConfig();

    /*
     * The constructor now receives one fixed width and one fixed height.
     * Unlike the old full-screen version, we do not read the monitor size anymore.
     * This keeps Menu, Settings, and all Level scenes consistent.
     */
    public GameConfig(double stageWidth, double stageHeight) {
        this.title = "Mirror Steps";
        this.stageWidth = stageWidth;
        this.stageHeight = stageHeight;
        this.worldWidth = stageWidth;
        this.worldHeight = stageHeight;
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

    public double getWorldWidth() {
        return worldWidth;
    }

    public double getWorldHeight() {
        return worldHeight;
    }

    public double getPlayerWidth() {
        return playerWidth;
    }

    public double getPlayerHeight() {
        return playerHeight;
    }

    public double getMoveSpeed() {
        return moveSpeed;
    }

    public double getJumpVelocity() {
        return jumpVelocity;
    }

    public double getGravity() {
        return gravity;
    }

    public Color getBlockColor() {
        return blockColor;
    }

    public Color getPlayerColor() {
        return playerColor;
    }

    public Color getGoalColor() {
        return goalColor;
    }

    public ControlConfig getControlConfig() {
        return controlConfig;
    }

    /*
     * Shared input mapping for the whole game.
     *
     * This nested class is not a subclass relationship with any gameplay class.
     * It is only grouped here because key bindings are global configuration data.
     *
     * If your team wants to change controls later, this is the first place to edit.
     * If you need more actions in the future, add more `is...` methods here and then
     * call them from Player or a level file.
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
