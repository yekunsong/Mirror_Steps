package entity;

import javafx.scene.paint.Color;

/*
 * Generic solid terrain block.
 * This entity-layer class represents any fixed-size collision block that a level
 * owner wants to place without giving it special movement behavior.
 */
public class TerrainBlock extends GameObject {

    public TerrainBlock(double x, double y, double width, double height, Color color) {
        super(x, y, width, height, color);
    }
}
