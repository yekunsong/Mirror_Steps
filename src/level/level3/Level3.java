package level.level3;

import config.LevelConfig;
import javafx.scene.paint.Color;
import level.LevelContext;
import level.LevelModule;

/*
 * Level 3 owner file.
 * This file belongs to the Level 3 teammate and contains the full configuration,
 * layout, and level-specific extension hooks for the third stage.
 */
public final class Level3 implements LevelModule {

    private static final LevelConfig CONFIG = LevelConfig.builder(3, "Level 3 - Mirror Hall")
            .subtitle("A darker stage with a slightly different feel.")
            .description("This level is reserved for the mirror-world or light-dark switch mechanic.")
            .mechanicLabel("Mirror-world placeholder with key and door")
            .worldSize(960, 540)
            .gravity(640)
            .moveSpeed(240)
            .jumpVelocity(-400)
            .previousLevelId(2)
            .nextLevelId(0)
            .backgroundColor(Color.web("#1f1b4b"))
            .panelColor(Color.web("#312e81"))
            .playerColor(Color.web("#f97316"))
            .platformColor(Color.web("#cbd5e1"))
            .accentColor(Color.web("#a78bfa"))
            .doorColor(Color.web("#22d3ee"))
            .musicTrackId("mirror-hall")
            .build();

    @Override
    public int getId() {
        return 3;
    }

    @Override
    public LevelConfig getConfig() {
        return CONFIG;
    }

    @Override
    public void build(LevelContext context) {
        context.createTerrainBlock(0, context.getConfig().getWorldHeight() - 32, 220, 32);
        context.createTerrainBlock(300, context.getConfig().getWorldHeight() - 32, 240, 32);
        context.createTerrainBlock(620, context.getConfig().getWorldHeight() - 32, 340, 32);
        context.createMovingPlatform(180, 360, 150, 24, 90, 1.8);
        context.createMovingPlatform(520, 280, 160, 24, 110, 2.0);
        context.createKeyItem(620, 240, 28, 28, "mirror-key");
        context.createDoor(context.getConfig().getWorldWidth() - 90, 180, 48, 90, "mirror-key", context.getConfig().getNextLevelId());
        context.createPlayer(72, context.getConfig().getWorldHeight() - 92);
    }
}
