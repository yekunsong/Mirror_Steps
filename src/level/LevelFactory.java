package level;

import java.util.List;
import level.level1.Level1;
import level.level2.Level2;
import level.level3.Level3;

/*
 * Registry for all available level modules.
 * This file belongs to the flattened level wiring layer and is the only place that
 * needs updates when the team adds or removes a level.
 */
public final class LevelFactory {

    private LevelFactory() {
    }

    public static LevelModule createLevel(int levelId) {
        return switch (levelId) {
            case 1 -> new Level1();
            case 2 -> new Level2();
            case 3 -> new Level3();
            default -> new Level1();
        };
    }

    public static int getFirstLevelId() {
        return 1;
    }

    public static List<LevelModule> getAvailableModules() {
        return List.of(
                new Level1(),
                new Level2(),
                new Level3()
        );
    }
}
