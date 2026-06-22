package level.level12;

import config.GameConfig;
import core.AppRouter;
import level.BaseLevel;

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
public final class Level12 extends BaseLevel {

    public Level12(GameConfig config, AppRouter router) {
        super(config, router);
    }

    @Override
    protected String getLevelTitle() {
        return "Level 12";
    }

    @Override
    protected void buildLevel() {
        addBlock(0, config.getWorldHeight() - 40, 220, 40);
        addBlock(300, config.getWorldHeight() - 40, 240, 40);
        addBlock(620, config.getWorldHeight() - 40, config.getWorldWidth() - 620, 40);
        addBlock(180, 360, 150, 24);
        addBlock(520, 280, 160, 24);
        setGoal(config.getWorldWidth() - 90, 208);
    }

    @Override
    protected int getPreviousLevelId() {
        return 11;
    }

    @Override
    protected int getNextLevelId() {
        return -1;
    }

    @Override
    protected String getNextButtonText() {
        return "Finish";
    }

    @Override
    protected void onGoalReached() {
        switchToMenu();
    }
}
