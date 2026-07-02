package entity;

import javafx.scene.paint.Color;

/*
 * Moving platform that travels back and forth on one axis between two bounds.
 */
public final class MovePlatform extends GameObject {

    public enum Direction {
        HORIZONTAL,
        VERTICAL
    }

    private final Direction direction;
    private final double minBound;
    private final double maxBound;
    private final double speed;
    private double previousX;
    private double previousY;
    private double velocitySign = 1;
    private boolean manualControl = false;

    public MovePlatform(
            double x,
            double y,
            double width,
            double height,
            Color color,
            Direction direction,
            double minBound,
            double maxBound,
            double speed) {
        super(x, y, width, height, color);
        this.direction = direction;
        this.minBound = minBound;
        this.maxBound = maxBound;
        this.speed = speed;
        this.previousX = x;
        this.previousY = y;
    }

    public MovePlatform(
            double x,
            double y,
            double width,
            double height,
            Color color,
            String imagePath,
            Direction direction,
            double minBound,
            double maxBound,
            double speed) {
        this(x, y, width, height, color, direction, minBound, maxBound, speed);
        setBackgroundImage(imagePath);
    }
    
    public void setManualControl(boolean manualControl) {
        this.manualControl = manualControl;
    }

    @Override
    public void update(double deltaSeconds) {
        previousX = getX();
        previousY = getY();
        
        if (manualControl) {
            return;
        }

        double distance = speed * deltaSeconds * velocitySign;

        if (direction == Direction.HORIZONTAL) {
            double nextX = getX() + distance;
            if (nextX < minBound) {
                nextX = minBound;
                velocitySign = 1;
            } else if (nextX > maxBound) {
                nextX = maxBound;
                velocitySign = -1;
            }
            setPosition(nextX, getY());
            return;
        }

        double nextY = getY() + distance;
        if (nextY < minBound) {
            nextY = minBound;
            velocitySign = 1;
        } else if (nextY > maxBound) {
            nextY = maxBound;
            velocitySign = -1;
        }
        setPosition(getX(), nextY);
    }
    
    /*
     * Moves this platform smoothly toward a specific Y position.
     * This is used by Level 2 when the floor button is pressed.
     */
    public void moveToY(double targetY, double deltaSeconds) {
        previousX = getX();
        previousY = getY();

        double currentY = getY();
        double step = speed * deltaSeconds;

        if (Math.abs(targetY - currentY) <= step) {
            setPosition(getX(), targetY);
        } else if (targetY < currentY) {
            setPosition(getX(), currentY - step);
        } else {
            setPosition(getX(), currentY + step);
        }
    }

    public void moveToX(double targetX, double deltaSeconds) {
        previousX = getX();
        previousY = getY();

        double currentX = getX();
        double step = speed * deltaSeconds;

        if (Math.abs(targetX - currentX) <= step) {
            setPosition(targetX, getY());
        } else if (targetX < currentX) {
            setPosition(currentX - step, getY());
        } else {
            setPosition(currentX + step, getY());
        }
    }



    public Direction getDirection() {
        return direction;
    }

    public double getMinBound() {
        return minBound;
    }

    public double getMaxBound() {
        return maxBound;
    }

    public double getSpeed() {
        return speed;
    }

    public double getPreviousX() {
        return previousX;
    }

    public double getPreviousY() {
        return previousY;
    }

    public double getDeltaX() {
        return getX() - previousX;
    }

    public double getDeltaY() {
        return getY() - previousY;
    }
}
