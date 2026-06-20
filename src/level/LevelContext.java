package level;

import audio.AudioManager;
import config.GameConfig;
import config.LevelConfig;
import core.GameManager;
import entity.Collectible;
import entity.Door;
import entity.Enemy;
import entity.KeyItem;
import entity.MovingPlatform;
import entity.Player;
import entity.TerrainBlock;
import java.util.List;

/*
 * Shared bridge between the runtime and a level module.
 * This file belongs to the flattened level contract layer and is the main surface
 * level owners use to create their own content without changing the runtime engine.
 */
public interface LevelContext {

    GameManager getGameManager();

    GameConfig getGameConfig();

    LevelConfig getConfig();

    AudioManager getAudioManager();

    Player createPlayer(double x, double y);

    TerrainBlock createTerrainBlock(double x, double y, double width, double height);

    MovingPlatform createMovingPlatform(double x, double y, double width, double height, double travelDistance, double speed);

    KeyItem createKeyItem(double x, double y, double width, double height, String keyId);

    Door createDoor(double x, double y, double width, double height, String requiredKeyId, int targetLevelId);

    Enemy createEnemy(Enemy enemy);

    Player getPlayer();

    Door getDoor();

    List<TerrainBlock> getTerrainBlocks();

    List<MovingPlatform> getMovingPlatforms();

    List<Enemy> getEnemies();

    List<Collectible> getCollectibles();

    boolean hasCollectedKey(String keyId);

    void setStatus(String message);
}
