package entity;

import javafx.scene.paint.Color;

/*
 * Floor button that becomes invisible when pressed.
 */
public final class FloorButton extends GameObject {

    private static final Color UNPRESSED_COLOR = Color.web("#ef4444");

    private final double originalY;
    private final double originalHeight;
    private boolean isPressed = false;

    // Standard constructor (No image)
    public FloorButton(double x, double y, double width, double height) {
        super(x, y, width, height, UNPRESSED_COLOR);
        this.originalY = y;
        this.originalHeight = height;
        getView().setArcWidth(8);
        getView().setArcHeight(8);
    }

    // New Constructor (With Image)
    public FloorButton(double x, double y, double width, double height, String imagePath) {
        this(x, y, width, height);
        setBackgroundImage(imagePath);
    }

    public void setPressed(boolean pressed) {
        if (this.isPressed == pressed) return;

        this.isPressed = pressed;

        if (pressed) {
            // Make button invisible when pressed
            getView().setVisible(false);
        } else {
            // Make button visible when unpressed
            getView().setVisible(true);
        }
    }

    public boolean isPressed() {
        return isPressed;
    }
}