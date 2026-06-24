package config;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

/*
 * Central configuration object for the simplified JavaFX game framework.
 *
 * Architectural role:
 * - This class stores configuration values that must remain consistent across the
 *   entire application, including menu screens, settings screens, and all levels.
 * - It replaces the need for multiple small configuration files so that the project
 *   remains easy to understand for a student team.
 *
 * Main categories of data stored here:
 * - application title
 * - fixed window width and height
 * - logical game world width and height
 * - default player size and physics constants
 * - shared colors used by multiple scenes
 * - keyboard mappings
 *
 * Maintenance rule:
 * - Add a value here only if that value is intended to affect the entire project.
 * - If a value is specific to one level only, keep that value inside the level file
 *   rather than moving it into this shared configuration class.
 *
 * Extension guidance:
 * - A future version can add audio defaults, accessibility options, difficulty
 *   presets, or multiple display profiles here without changing the overall design.
 */
public final class GameConfig {

    private final String title;
    private final double worldWidth;
    private final double worldHeight;
    private final double playerWidth = 36;
    private final double playerHeight = 48;
    private final double moveSpeed = 150;
    private final double jumpVelocity = -600;
    private final double gravity = 1000;
    private final Color blockColor = Color.web("#64748b");
    private final Color playerColor = Color.web("#22c55e");
    private final Color goalColor = Color.web("#f59e0b");
    private final ControlConfig controlConfig = new ControlConfig();

    /*
     * Creates one immutable configuration object for the application session.
     *
     * The window size is now fixed at startup instead of being derived from the
     * monitor size. This decision keeps all scenes visually consistent and removes
     * the previous mismatch between menu screens and gameplay scenes.
     */
    public GameConfig(double width, double height) {
        this.title = "Mirror Steps";
        this.worldWidth = width;
        this.worldHeight = height;
    }

    public String getTitle() {
        return title;
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
     * Keyboard mapping configuration shared by all scenes.
     *
     * Design note:
     * - This is a nested helper type, not a gameplay class and not part of the
     *   inheritance hierarchy.
     * - It remains inside GameConfig because the key mapping is application-wide
     *   configuration rather than level-specific logic.
     *
     * Modification guidance:
     * - If the team wants to change movement keys, update the relevant methods here.
     * - If a new action is introduced later, add a new method and then call that
     *   method from Player or the relevant level scene.
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
        
        public boolean isRunning(KeyCode keyCode) {
        	return keyCode == KeyCode.SHIFT;
        }
    }
}
