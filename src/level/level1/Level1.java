package level.level1;

import config.LevelConfig;
import javafx.scene.paint.Color;
import level.LevelContext;
import level.LevelModule;

/*
 * Level 1 owner file.
 * This file belongs to the Level 1 teammate and contains the full configuration,
 * layout, and level-specific extension hooks for the first stage.
 */
public final class Level1 implements LevelModule {

    private static final LevelConfig CONFIG = LevelConfig.builder(1, "Level 1 - Forest Trial")
            .subtitle("Basic movement and first jump spacing.")
            .description("This level introduces the player to movement, key pickup, and door unlocking.")
            .mechanicLabel("Static terrain and a single locked exit door")
            .worldSize(960, 540)
            .gravity(720)
            .moveSpeed(220)
            .jumpVelocity(-360)
            .previousLevelId(0)
            .nextLevelId(2)
            .backgroundColor(Color.web("#0f172a"))
            .panelColor(Color.web("#1e293b"))
            .playerColor(Color.web("#22c55e"))
            .platformColor(Color.web("#64748b"))
            .accentColor(Color.web("#38bdf8"))
            .doorColor(Color.web("#f59e0b"))
            .musicTrackId("forest-trial")
            .build();

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public LevelConfig getConfig() {
        return CONFIG;
    }

    @Override
    public void build(LevelContext context) {
        context.createTerrainBlock(0, context.getConfig().getWorldHeight() - 32, context.getConfig().getWorldWidth(), 32);
        context.createTerrainBlock(160, 430, 180, 24);
        context.createTerrainBlock(390, 360, 160, 24);
        context.createTerrainBlock(610, 290, 160, 24);
        context.createKeyItem(260, 390, 28, 28, "forest-key");
        context.createDoor(context.getConfig().getWorldWidth() - 90, 220, 48, 90, "forest-key", context.getConfig().getNextLevelId());
        context.createPlayer(72, context.getConfig().getWorldHeight() - 92);
    }
}
