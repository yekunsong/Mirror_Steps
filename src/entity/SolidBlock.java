package entity;

import javafx.scene.paint.Color;

/*
 * SolidBlock is used by levels that need full wall collision.
 * It keeps the same simple rectangle shape as Block.
 */
public final class SolidBlock extends GameObject {

    public SolidBlock(double x, double y, double width, double height, Color color) {
        super(x, y, width, height, color);
        getView().setArcWidth(0);
        getView().setArcHeight(0);
    }
}
