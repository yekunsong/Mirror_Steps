package entity;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/*
 * Parent class for basic drawable game objects.
 *
 * Parent-child relationship:
 * - GameObject is the parent class
 * - Player and Block are child classes
 *
 * This inheritance exists only to share the repeated data that both child classes need:
 * - x/y position
 * - width/height
 * - one JavaFX Rectangle node
 * - helper methods for moving and syncing the node
 *
 * Why this class is still useful:
 * - it avoids copy-pasting the same position/render code into Player and Block
 * - it keeps child classes focused on their own behavior
 *
 * Future extension directions:
 * - add rotation support
 * - add visibility toggles
 * - add image-based rendering instead of plain rectangles
 *
 * If your team wants a new simple object later, such as a checkpoint or hazard,
 * it can extend GameObject in the same way as Block currently does.
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

    /*
     * Returns the JavaFX node that levels add into their Pane.
     */
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

    /*
     * Keeps logical position and on-screen node position consistent.
     */
    private void syncView() {
        view.setLayoutX(x);
        view.setLayoutY(y);
    }
}
