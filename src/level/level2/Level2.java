package level.level2;

import config.LevelConfig;
import javafx.scene.paint.Color;
import level.LevelContext;
import level.LevelModule;

/*
 * Level 2 owner file.
 * This file belongs to the Level 2 teammate and contains the full configuration,
 * layout, and level-specific extension hooks for the second stage.
 */
public final class Level2 implements LevelModule {

    private static final LevelConfig CONFIG = LevelConfig.builder(2, "Level 2 - Moving Bridge")
            .subtitle("A level focused on moving platforms and timing.")
            .description("This level introduces moving platforms, generic terrain blocks, and a locked door.")
            .mechanicLabel("Moving bridge challenge with key and door")
            .worldSize(960, 540)
            .gravity(760)
            .moveSpeed(230)
            .jumpVelocity(-380)
            .previousLevelId(1)
            .nextLevelId(3)
            .backgroundColor(Color.web("#111827"))
            .panelColor(Color.web("#1e293b"))
            .playerColor(Color.web("#facc15"))
            .platformColor(Color.web("#94a3b8"))
            .accentColor(Color.web("#60a5fa"))
            .doorColor(Color.web("#fb7185"))
            .musicTrackId("moving-bridge")
            .build();

    @Override
    public int getId() {
        return 2;
    }

    @Override
    public LevelConfig getConfig() {
        return CONFIG;
    }

    @Override
    public void build(LevelContext context) {
        context.createTerrainBlock(0, context.getConfig().getWorldHeight() - 32, 300, 32);
        context.createTerrainBlock(400, context.getConfig().getWorldHeight() - 32, 560, 32);
        context.createTerrainBlock(120, 400, 140, 24);
        context.createMovingPlatform(310, 320, 140, 24, 120, 2.2);
        context.createTerrainBlock(600, 250, 160, 24);
        context.createKeyItem(210, 368, 28, 28, "bridge-key");
        context.createDoor(context.getConfig().getWorldWidth() - 92, 150, 48, 90, "bridge-key", context.getConfig().getNextLevelId());
        context.createPlayer(72, context.getConfig().getWorldHeight() - 92);
    }
}
