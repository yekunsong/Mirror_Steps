package level;

import config.LevelConfig;
import javafx.scene.input.KeyCode;

/*
 * Contract for a playable level module.
 * This file belongs to the flattened level layer and combines the id, config,
 * and build logic used by the shared runtime.
 */
public interface LevelModule {

    int getId();

    LevelConfig getConfig();

    void build(LevelContext context);

    default void update(LevelContext context, double deltaSeconds) {
    }

    default void onKeyPressed(LevelContext context, KeyCode keyCode) {
    }

    default void onKeyReleased(LevelContext context, KeyCode keyCode) {
    }

    default boolean canEnterDoor(LevelContext context) {
        return true;
    }
}
