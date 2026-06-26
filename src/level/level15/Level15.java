package level.level15;

import config.GameConfig;
import core.AppRouter;
import entity.MovePlatform;
import level.BaseLevel;
import javafx.scene.paint.Color;

/*
 * Concrete scene implementation for Level 3.
 *
 * Architectural role:
 * - This class implements the final stage in the simplified three-level structure.
 * - It follows the same pattern as Level1 and Level2 while changing the final
 *   transition behavior to return to the menu.
 *
 * Responsibilities:
 * - define the Level 3 layout
 * - maintain player interaction and collision logic
 * - provide the final completion route back to the menu
 *
 * Extension guidance:
 * - A future revision can replace the return-to-menu behavior with a dedicated ending
 *   scene if required.
 */
public final class Level15 extends BaseLevel {

    public Level15(GameConfig config, AppRouter router) {
        super(config, router);
    }

    @Override
    protected String getLevelTitle() {
        return "Level 15";
    }

    @Override
    protected void buildLevel() {
        addBlock(0, config.getWorldHeight() - 40, 220, 40);
        addBlock(300, config.getWorldHeight() - 40, 240, 40);
        addBlock(620, config.getWorldHeight() - 40, config.getWorldWidth() - 620, 40);
        addBlock(180, 360, 150, 24);
        addBlock(520, 280, 160, 24);
        addMovePlatform(220, 660, 90, 20, Color.web("#0ea5e9"), MovePlatform.Direction.HORIZONTAL, 220, 470, 90);
        addTrap(220, config.getWorldHeight() - 100, 80, 20, Color.web("#ef4444"));
        addTrap(540, config.getWorldHeight() - 100, 80, 20, Color.web("#ef4444"));
        setGoal(config.getWorldWidth() - 90, 208);
    }

    @Override
    protected int getPreviousLevelId() {
        return 14;
    }

    @Override
    protected int getNextLevelId() {
        return 16;
    }

}
