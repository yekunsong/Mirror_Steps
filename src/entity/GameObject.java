package entity;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/*
 * Shared base class for simple on-screen game objects.
 *
 * Inheritance structure:
 * - GameObject is the parent class.
 * - Player and Block are concrete child classes.
 *
 * Purpose of this abstraction:
 * - centralize position data
 * - centralize width and height storage
 * - centralize synchronization between logical coordinates and the JavaFX node
 *
 * Reason this class remains in the project:
 * - Without it, Player and Block would duplicate the same rendering and coordinate
 *   management code.
 * - Keeping this single parent class is a reasonable compromise between simplicity
 *   and code reuse.
 *
 * Extension guidance:
 * - If a future version introduces checkpoints, hazards, decorations, or collectible
 *   objects again, they can extend this class if they share the same rectangular
 *   rendering model.
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
     * Returns the JavaFX node used for rendering inside a level pane.
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
     * Synchronizes the logical coordinates stored in the object with the JavaFX
     * rectangle used for rendering.
     */
    private void syncView() {
        view.setLayoutX(x);
        view.setLayoutY(y);
    }
}
