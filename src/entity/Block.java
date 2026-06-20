package entity;

import javafx.scene.paint.Color;

/*
 * Simple platform or terrain block.
 *
 * Parent-child relationship:
 * - Block extends GameObject
 * - Block does not add new behavior yet
 *
 * Why keep this as a separate child class even though it is small:
 * - the class name is clearer than reusing GameObject directly in level files
 * - future shared block behavior can be added here without touching all levels
 *
 * Future extension directions:
 * - different block colors per level
 * - slippery blocks
 * - disappearing blocks
 * - spike or hazard subclasses if the game grows later
 *
 * Right now, if you want to add a platform in a level, edit that level file and call
 * the local `addBlock(...)` helper there.
 */
public final class Block extends GameObject {

    public Block(double x, double y, double width, double height, Color color) {
        super(x, y, width, height, color);
    }
}
