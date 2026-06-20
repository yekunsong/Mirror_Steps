package entity;

import config.GameConfig;
import java.util.Set;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

/*
 * Player object used by all three levels.
 *
 * Parent-child relationship:
 * - Player extends GameObject
 * - Character was intentionally merged into Player to keep the project simpler
 *
 * That means this class now owns:
 * - rendering inherited from GameObject
 * - movement speed state
 * - jump and gravity state
 * - spawn point data
 * - reset behavior
 *
 * This file is the main place to edit if your team wants to change:
 * - how fast the player moves
 * - jump behavior
 * - respawn logic
 * - future animation state
 *
 * Future extension directions:
 * - add facing direction
 * - add double jump
 * - add wall jump
 * - add sprite/image rendering
 */
public final class Player extends GameObject {

    private final double spawnX;
    private final double spawnY;
    private double previousY;
    private double velocityX;
    private double velocityY;
    private boolean onGround;

    public Player(double x, double y, double width, double height, Color color) {
        super(x, y, width, height, color);
        this.spawnX = x;
        this.spawnY = y;
        this.previousY = y;
    }

    /*
     * Reads shared key state and converts it into horizontal and jump motion.
     * Levels do not need to duplicate this logic.
     */
    public void handleInput(Set<KeyCode> activeKeys, GameConfig.ControlConfig controls, double moveSpeed, double jumpVelocity) {
        boolean left = activeKeys.stream().anyMatch(controls::isMoveLeft);
        boolean right = activeKeys.stream().anyMatch(controls::isMoveRight);
        boolean jump = activeKeys.stream().anyMatch(controls::isJump);

        velocityX = 0;
        if (left && !right) {
            velocityX = -moveSpeed;
        } else if (right && !left) {
            velocityX = moveSpeed;
        }

        if (jump && onGround) {
            velocityY = jumpVelocity;
            onGround = false;
        }
    }

    /*
     * Applies simple platformer physics.
     * The level owns collision detection, while Player owns its motion state.
     */
    public void applyPhysics(double deltaSeconds, double gravity) {
        previousY = getY();
        moveBy(velocityX * deltaSeconds, velocityY * deltaSeconds);
        velocityY += gravity * deltaSeconds;
    }

    /*
     * Called by a level when collision says the player has landed on top of a block.
     */
    public void landOn(double platformY) {
        setPosition(getX(), platformY - getHeight());
        velocityY = 0;
        onGround = true;
    }

    /*
     * Resets the player to the original spawn point for that level.
     * If your team later wants checkpoints, this method is a good place to extend.
     */
    public void resetToSpawn() {
        setPosition(spawnX, spawnY);
        previousY = spawnY;
        velocityX = 0;
        velocityY = 0;
        onGround = false;
    }

    public double getPreviousY() {
        return previousY;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }
}
