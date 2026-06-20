package entity;

import javafx.scene.paint.Color;

/*
 * Key collectible implementation.
 * This entity-layer class is intentionally small so a level can create key items
 * with an identifier while the runtime decides how the key is used.
 */
public final class KeyItem extends Collectible {

    private final String keyId;

    public KeyItem(double x, double y, double width, double height, Color color, String keyId) {
        super(x, y, width, height, color);
        this.keyId = keyId;
    }

    public String getKeyId() {
        return keyId;
    }
}
