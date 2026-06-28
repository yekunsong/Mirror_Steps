package entity;

import javafx.scene.paint.Color;

/*
 * Goal door that unlocks after the required keys are collected.
 */
public final class Door extends GameObject {

    private static final Color LOCKED_FILL = Color.web("#7c3aed");
    private static final Color UNLOCKED_FILL = Color.web("#84cc16");
    private static final Color LIGHT_STROKE = Color.web("#ede9fe");
    private static final Color DARK_STROKE = Color.web("#c4b5fd");

    public Door(double x, double y, double width, double height) {
        super(x, y, width, height, LOCKED_FILL);
        getView().setArcWidth(14);
        getView().setArcHeight(14);
        getView().setStrokeWidth(3);
    }

    public Door(double x, double y, double width, double height, String imagePath) {
        this(x, y, width, height);
        setBackgroundImage(imagePath);
    }
    
    public void refreshStyle(boolean unlocked, boolean darkWorld) {
        setFill(unlocked ? UNLOCKED_FILL : LOCKED_FILL);
        getView().setStroke(darkWorld ? DARK_STROKE : LIGHT_STROKE);
        getView().setOpacity(darkWorld ? 0.35 : 1.0);
    }
}