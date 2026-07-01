package entity;

import javafx.scene.paint.Color;

/*
 * Collectible key used by lock-and-door levels.
 */
public final class Key extends GameObject {

    private static final Color KEY_FILL = Color.web("#facc15");

    public Key(double x, double y, double width, double height) {
        super(x, y, width, height, KEY_FILL);
        getView().setArcWidth(10);
        getView().setArcHeight(10);
        getView().setStroke(null);
    }

    public Key(double x, double y, double size) {
        this(x, y, size, size);
    }

    public void setCollected(boolean collected) {
        getView().setVisible(!collected);
    }
    
    public Key(double x, double y, double size, String imagePath) {
        this(x, y, size, size);
        setBackgroundImage(imagePath);
    }

    public Key(double x, double y, double width, double height, String imagePath) {
        this(x, y, width, height);
        setBackgroundImage(imagePath);
    }

    public void setSize(double size) {
        setSize(size, size);
    }

    public void setSize(double width, double height) {
        getView().setWidth(width);
        getView().setHeight(height);
    }

    public void setImage(String imagePath) {
        setBackgroundImage(imagePath);
    }

    public void setVisible(boolean visible) {
        getView().setVisible(visible);
    }

    public void refreshStyle() {
        setFill(KEY_FILL);
        getView().setStroke(null);
    }
}
