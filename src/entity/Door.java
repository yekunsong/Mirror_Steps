package entity;

import javafx.scene.paint.Color;

/*
 * Exit door entity.
 * This entity-layer class stores the required key id and target level id so the
 * shared runtime can decide when the player is allowed to advance.
 */
public class Door extends GameObject {

    private final String requiredKeyId;
    private final int targetLevelId;
    private boolean unlocked;

    public Door(double x, double y, double width, double height, Color color, String requiredKeyId, int targetLevelId) {
        super(x, y, width, height, color);
        this.requiredKeyId = requiredKeyId;
        this.targetLevelId = targetLevelId;
    }

    public String getRequiredKeyId() {
        return requiredKeyId;
    }

    public int getTargetLevelId() {
        return targetLevelId;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked, Color unlockedColor) {
        this.unlocked = unlocked;
        setFill(unlockedColor);
    }
}
