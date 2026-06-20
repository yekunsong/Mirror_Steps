package entity;

import javafx.scene.paint.Color;

/*
 * Base class for collectible world items.
 * This entity-layer class stores the collected state so keys, coins, and future
 * pickups can share the same hide-and-disable behavior.
 */
public abstract class Collectible extends GameObject {

    private boolean collected;

    protected Collectible(double x, double y, double width, double height, Color color) {
        super(x, y, width, height, color);
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
        getNode().setVisible(false);
    }
}
