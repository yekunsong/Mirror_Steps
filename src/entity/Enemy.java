package entity;

import javafx.scene.paint.Color;

/*
 * Shared enemy base.
 * This entity-layer class gives future enemy types a common damage value and a
 * shared contact hook, while leaving the exact movement behavior to subclasses.
 */
public abstract class Enemy extends Character {

    private final int damage;

    protected Enemy(double x, double y, double width, double height, Color color, int damage) {
        super(x, y, width, height, color);
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    public void onPlayerContact(Player player) {
    }
}
