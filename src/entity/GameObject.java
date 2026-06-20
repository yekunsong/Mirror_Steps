package entity;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/*
 * Base class for every renderable world object.
 * This entity-layer class keeps position, size, and JavaFX node synchronization in one place.
 * Subclasses should add only generic behavior that is useful across the entity hierarchy.
 */
public abstract class GameObject {

    private final Rectangle view;
    private double x;
    private double y;
    private final double width;
    private final double height;

    protected GameObject(double x, double y, double width, double height, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.view = new Rectangle(width, height, color);
        this.view.setArcWidth(12);
        this.view.setArcHeight(12);
        syncView();
    }

    public void update(double deltaSeconds) {
    }

    public Node getNode() {
        return view;
    }

    public Bounds getBounds() {
        return view.getBoundsInParent();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        syncView();
    }

    public void moveBy(double deltaX, double deltaY) {
        setPosition(x + deltaX, y + deltaY);
    }

    protected void setFill(Color color) {
        view.setFill(color);
    }

    protected Rectangle getView() {
        return view;
    }

    private void syncView() {
        view.setLayoutX(x);
        view.setLayoutY(y);
    }
}
