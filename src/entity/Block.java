package entity;

import javafx.scene.paint.Color;

/*
 * Concrete terrain object representing a platform or solid block.
 *
 * Inheritance structure:
 * - Block extends GameObject.
 * - Block currently adds no additional state because the simplified framework only
 *   needs a static collision surface.
 *
 * Reason this class exists as a separate type:
 * - The name `Block` is clearer in level code than instantiating `GameObject`
 *   directly.
 * - If a later revision adds special block behavior, that behavior can be added here
 *   without rewriting the level files that already depend on this type.
 *
 * Typical modification point:
 * - To change where a block appears, edit the relevant level file.
 * - To change how all blocks behave, edit this class or GameConfig.
 */
public final class Block extends GameObject {

    public Block(double x, double y, double width, double height, Color color) {
        super(x, y, width, height, color);
    }
}
