package entity;

import javafx.scene.paint.Color;

/*
 * Trap area that resets the player on contact.
 */
public final class Trap extends GameObject {

    public Trap(double x, double y, double width, double height, Color color) {
        super(x, y, width, height, color);
        getView().setArcWidth(0);
        getView().setArcHeight(0);
    }

    public Trap(double x, double y, double width, double height, Color color, String imagePath) {
        this(x, y, width, height, color);
        setBackgroundImage(imagePath);
    }
}
