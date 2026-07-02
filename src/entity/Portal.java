package entity;

import javafx.scene.paint.Color;

/*
 * A portal that teleports the player and pushable blocks to its linked portal.
 *
 * Usage:
 * - Create two portals.
 * - Link them with portalA.linkTo(portalB).
 * - When an object enters one portal, it appears at the linked portal.
 */
public final class Portal extends GameObject {

    private Portal linkedPortal;
    private double cooldown = 0;

    public Portal(double x, double y, double width, double height, String imagePath) {
        super(x, y, width, height, Color.web("#7c3aed"));
        setBackgroundImage(imagePath);
    }

    public void linkTo(Portal other) {
        this.linkedPortal = other;
    }

    public Portal getLinkedPortal() {
        return linkedPortal;
    }

    /*
     * Returns the X position where a teleported object should appear,
     * centered horizontally on the linked portal.
     */
    public double getExitX(double objectWidth) {
        return getX() + (getWidth() - objectWidth) / 2.0;
    }

    /*
     * Returns the Y position where a teleported object should appear,
     * centered vertically on the linked portal.
     */
    public double getExitY(double objectHeight) {
        return getY() + (getHeight() - objectHeight) / 2.0;
    }

    public void tickCooldown(double deltaSeconds) {
        if (cooldown > 0) {
            cooldown -= deltaSeconds;
        }
    }

    public boolean isOnCooldown() {
        return cooldown > 0;
    }

    public void startCooldown() {
        cooldown = 2.0; // seconds before this portal can teleport again
    }
}