package entity;

import javafx.scene.paint.Color;

/*
 * Moving platform entity.
 * This reusable entity layer class provides a simple sinusoidal movement pattern
 * that level owners can use or replace with their own platform motion rules.
 */
public class MovingPlatform extends TerrainBlock {

    private final double baseX;
    private final double baseY;
    private final double travelDistance;
    private final double speed;
    private double time;
    private double deltaX;

    public MovingPlatform(double x, double y, double width, double height, Color color, double travelDistance, double speed) {
        super(x, y, width, height, color);
        this.baseX = x;
        this.baseY = y;
        this.travelDistance = travelDistance;
        this.speed = speed;
    }

    @Override
    public void update(double deltaSeconds) {
        double previousX = getX();
        time += deltaSeconds * speed;
        setPosition(baseX + Math.sin(time) * travelDistance, baseY);
        deltaX = getX() - previousX;
    }

    public double getDeltaX() {
        return deltaX;
    }

    public void resetMotion() {
        time = 0;
        deltaX = 0;
        setPosition(baseX, baseY);
    }
}
