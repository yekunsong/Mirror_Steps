package entity;

import javafx.scene.paint.Color;

/*
 * Collectible key used by lock-and-door levels.
 */
public final class Key extends GameObject {

    private static final Color KEY_FILL = Color.web("#facc15");
    private static final Color KEY_STROKE = Color.web("#fef08a");

    public Key(double x, double y, double size) {
        super(x, y, size, size, KEY_FILL);
        getView().setArcWidth(10);
        getView().setArcHeight(10);
        getView().setStroke(KEY_STROKE);
    }

    public void setCollected(boolean collected) {
        getView().setVisible(!collected);
    }
    
    public Key(double x, double y, double size, String imagePath) {
        this(x, y, size);
        setBackgroundImage(imagePath);
    }

    public void setVisible(boolean visible) {
        getView().setVisible(visible);
    }

    public void refreshStyle() {
        setFill(KEY_FILL);
        getView().setStroke(KEY_STROKE);
    }
}