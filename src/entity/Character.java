package entity;

import javafx.scene.paint.Color;

/*
 * Shared character base for movable entities.
 * This entity-layer class adds spawn data, velocity, and basic physics state on top
 * of GameObject so both the player and future enemy types can reuse it.
 */
public abstract class Character extends GameObject {

    private final double spawnX;
    private final double spawnY;
    private double previousX;
    private double previousY;
    private double velocityX;
    private double velocityY;
    private boolean onGround;

    protected Character(double x, double y, double width, double height, Color color) {
        super(x, y, width, height, color);
        this.spawnX = x;
        this.spawnY = y;
        this.previousX = x;
        this.previousY = y;
    }

    @Override
    public void update(double deltaSeconds) {
    }

    public void applyPhysics(double deltaSeconds, double gravity) {
        previousX = getX();
        previousY = getY();
        moveBy(velocityX * deltaSeconds, velocityY * deltaSeconds);
        velocityY += gravity * deltaSeconds;
    }

    public void landOn(double platformY) {
        setPosition(getX(), platformY - getHeight());
        velocityY = 0;
        onGround = true;
    }

    public void resetToSpawn() {
        setPosition(spawnX, spawnY);
        previousX = spawnX;
        previousY = spawnY;
        velocityX = 0;
        velocityY = 0;
        onGround = false;
    }

    public void setHorizontalVelocity(double velocityX) {
        this.velocityX = velocityX;
    }

    public void setVerticalVelocity(double velocityY) {
        this.velocityY = velocityY;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public double getPreviousX() {
        return previousX;
    }

    public double getPreviousY() {
        return previousY;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }
}
