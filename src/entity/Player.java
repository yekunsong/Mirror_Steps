package entity;

import config.GameConfig;
import java.io.File;
import java.util.Set;

import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;

/*
 * Playable character object shared by all levels.
 *
 * Inheritance structure:
 * - Player extends GameObject.
 * - The previous Character layer was intentionally removed and merged into this class
 *   so that the movement model is easier for students to read and modify.
 *
 * Current responsibilities:
 * - store spawn position
 * - store horizontal and vertical velocity
 * - interpret movement-related input
 * - apply gravity-based motion
 * - reset the player when necessary
 *
 * Maintenance guidance:
 * - Modify this class when the team wants to change player-wide movement behavior.
 * - Do not put level-specific rules here; those rules should remain inside the level
 *   classes.
 *
 * Extension guidance:
 * - Future features such as animations, directional sprites, double jump, wall jump,
 *   or checkpoint-based respawn can be added here without changing the inheritance
 *   model again.
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
        
        // Uncommited if you want to set player image
        // initVisuals();
    }
    
    private void initVisuals() {
    	// change to your own downloaded position(only accept "jpg" or "jfif"
        String localPath = new File("D:\\Study\\CST_sem6\\JAVA\\Projects\\Pictures\\Player.jpg").toURI().toString();
        
        Image image = new Image(localPath);
        
        ImagePattern imagePattern = new ImagePattern(image);
        this.getView().setFill(imagePattern);

    }

    /*
     * Converts the current keyboard state into movement intent.
     *
     * This method does not move the Player directly. Instead, it updates velocity
     * fields, which are later applied by `applyPhysics(...)`.
     */
    public void handleInput(Set<KeyCode> activeKeys, GameConfig.ControlConfig controls, double moveSpeed, double jumpVelocity) {
        boolean left = activeKeys.stream().anyMatch(controls::isMoveLeft);
        boolean right = activeKeys.stream().anyMatch(controls::isMoveRight);
        boolean jump = activeKeys.stream().anyMatch(controls::isJump);
        boolean run = activeKeys.stream().anyMatch(controls::isRunning);
        

        velocityX = 0;
        if (run == true) {
        	moveSpeed = 250;
        }
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
     * Applies one frame of simple physics integration.
     *
     * Responsibility split:
     * - Player owns velocity and gravity response.
     * - Level classes own collision resolution and world rules.
     */
    public void applyPhysics(double deltaSeconds, double gravity) {
        previousY = getY();
        moveBy(velocityX * deltaSeconds, velocityY * deltaSeconds);
        velocityY += gravity * deltaSeconds;
    }

    /*
     * Places the Player on top of a solid surface and clears downward velocity.
     */
    public void landOn(double platformY) {
        setPosition(getX(), platformY - getHeight());
        velocityY = 0;
        onGround = true;
    }

    public void stopVerticalMovement() {
        velocityY = 0;
    }

    public void hitCeiling(double blockBottomY) {
        setPosition(getX(), blockBottomY + 0.0001);
        velocityY = 0;
        onGround = false;
    }

    /*
     * Resets the Player to the recorded spawn point for the current level.
     *
     * If checkpoint support is added later, this method is an appropriate extension
     * point because it already centralizes respawn behavior.
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
