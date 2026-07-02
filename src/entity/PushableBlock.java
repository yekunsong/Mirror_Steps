package entity;

import javafx.scene.paint.Color;

public final class PushableBlock extends GameObject {
    private double previousX;
    private double previousY;
    private double velocityX = 0;
    private double velocityY = 0;
    private boolean onGround = false;

    public PushableBlock(double x, double y, double width, double height, Color color) {
        super(x, y, width, height, color);
        this.previousX = x;
        this.previousY = y;
    }

    public PushableBlock(double x, double y, double width, double height, Color color, String imagePath) {
        this(x, y, width, height, color);
        setBackgroundImage(imagePath);
    }

    public void applyPhysics(double deltaSeconds, double gravity) {
        previousX = getX();
        previousY = getY();

        moveBy(velocityX * deltaSeconds, velocityY * deltaSeconds);
        velocityY += gravity * deltaSeconds;

        // slight friction
        velocityX *= 0.85;
        if (Math.abs(velocityX) < 1) {
            velocityX = 0;
        }

        onGround = false;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public void stopHorizontalMovement() {
        velocityX = 0;
    }

    public void landOn(double platformY) {
        setPosition(getX(), platformY - getHeight());
        velocityY = 0;
        onGround = true;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
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
}