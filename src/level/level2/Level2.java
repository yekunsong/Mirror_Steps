package level.level2;

import config.GameConfig;
import core.AppRouter;
import level.BaseLevel;

/*
 * Concrete scene implementation for Level 2.
 *
 * Architectural role:
 * - This class contains the second playable stage.
 * - Its structure intentionally mirrors Level1 and Level3 so that different team
 *   members can work on separate levels with minimal cross-dependency.
 *
 * Responsibilities:
 * - define the Level 2 terrain layout
 * - create and update the player
 * - handle collision resolution
 * - request navigation through AppRouter
 *
 * Modification guidance:
 * - Edit this file for Level 2-specific changes only.
 * - Preserve the overall method structure unless the same change must also be made in
 *   Level1 and Level3.
 */
public final class Level2 extends BaseLevel {

    public Level2(GameConfig config, AppRouter router) {
        super(config, router);
    }

    @Override
    protected String getLevelTitle() {
        return "Level 2";
    }

    @Override
    protected void buildLevel() {
        addBlock(0, config.getWorldHeight() - 40, 300, 40);
        addBlock(400, config.getWorldHeight() - 40, config.getWorldWidth() - 400, 40);
        addBlock(120, 400, 140, 24);
        addBlock(310, 320, 140, 24);
        addBlock(600, 250, 160, 24);
        setGoal(config.getWorldWidth() - 90, 178);
    }

    @Override
    protected int getPreviousLevelId() {
        return 1;
    }

    @Override
    protected int getNextLevelId() {
        return 3;
    }
}
