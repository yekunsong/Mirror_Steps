package entity;

import config.GameConfig;
import java.util.Set;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

/*
 * Player entity implementation.
 * This entity-layer class handles player-only input interpretation while the shared
 * level runtime keeps the global update loop and collision handling.
 * Teammates can extend this class later with animation, inventory, or combat behavior.
 */
public class Player extends Character {

    public Player(double x, double y, double width, double height, Color color) {
        super(x, y, width, height, color);
    }

    @Override
    public void update(double deltaSeconds) {
    }

    public void handleInput(Set<KeyCode> activeKeys, GameConfig.ControlConfig controls, double moveSpeed, double jumpVelocity) {
        boolean left = activeKeys.stream().anyMatch(controls::isMoveLeft);
        boolean right = activeKeys.stream().anyMatch(controls::isMoveRight);
        boolean jump = activeKeys.stream().anyMatch(controls::isJump);

        setHorizontalVelocity(0);
        if (left && !right) {
            setHorizontalVelocity(-moveSpeed);
        } else if (right && !left) {
            setHorizontalVelocity(moveSpeed);
        }

        if (jump && isOnGround()) {
            setVerticalVelocity(jumpVelocity);
            setOnGround(false);
        }
    }
}
