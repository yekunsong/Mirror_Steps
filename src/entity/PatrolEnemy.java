package entity;

import javafx.scene.paint.Color;

/*
 * Simple patrol enemy implementation.
 * This class belongs to the reusable entity layer and shows how a level can spawn
 * an enemy without rewriting the shared movement pattern.
 * A level can change the patrol bounds and speed without touching the runtime engine.
 */
public class PatrolEnemy extends Enemy {

    private final double leftBoundary;
    private final double rightBoundary;
    private final double speed;
    private double direction = 1.0;

    public PatrolEnemy(double x, double y, double width, double height, Color color, int damage,
                       double leftBoundary, double rightBoundary, double speed) {
        super(x, y, width, height, color, damage);
        this.leftBoundary = leftBoundary;
        this.rightBoundary = rightBoundary;
        this.speed = speed;
    }

    @Override
    public void update(double deltaSeconds) {
        double nextX = getX() + direction * speed * deltaSeconds;
        if (nextX <= leftBoundary || nextX + getWidth() >= rightBoundary) {
            direction = -direction;
        } else {
            moveBy(direction * speed * deltaSeconds, 0);
        }
    }
}
