package level.level1;

import config.GameConfig;
import core.AppRouter;
import level.BaseLevel;

/*
 * Concrete scene implementation for Level 1.
 *
 * Architectural role:
 * - This class contains the full implementation of the first playable stage.
 * - It is intentionally self-contained so that one contributor can maintain the
 *   level without needing to understand an additional runtime layer.
 *
 * Responsibilities:
 * - build the level layout
 * - create the player and goal
 * - maintain the per-frame update loop
 * - handle collision resolution for this level
 * - request scene transitions through AppRouter
 *
 * Relationship note:
 * - Level1, Level2, and Level3 are sibling classes. None of them inherits from the
 *   others, because the current design favors clarity over extra abstraction.
 *
 * Modification guidance:
 * - Edit this file when changing Level 1 only.
 * - Edit shared files only when the intended change affects all levels.
 */
public final class Level1 extends BaseLevel {

    public Level1(GameConfig config, AppRouter router) {
        super(config, router);
    }

    @Override
    protected String getLevelTitle() {
        return "Level 1";
    }

    @Override
    protected void buildLevel() {
        addBlock(0, config.getWorldHeight() - 40, config.getWorldWidth(), 40);
        addBlock(160, 430, 180, 24);
        addBlock(390, 360, 160, 24);
        addBlock(610, 290, 160, 24);
        setGoal(config.getWorldWidth() - 90, 218);
    }

    @Override
    protected int getPreviousLevelId() {
        return 0;
    }

    @Override
    protected int getNextLevelId() {
        return 2;
    }
}
