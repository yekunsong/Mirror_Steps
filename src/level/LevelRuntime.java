package level;

import config.GameConfig;
import config.LevelConfig;
import core.GameManager;
import entity.Collectible;
import entity.Door;
import entity.Enemy;
import entity.GameObject;
import entity.KeyItem;
import entity.MovingPlatform;
import entity.Player;
import entity.TerrainBlock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ui.PauseView;

/*
 * Shared runtime engine for one active level.
 * This file belongs to the flattened level runtime layer and owns world creation,
 * input handling, physics, collisions, pause overlay behavior, and transition requests.
 * Level owners should keep their custom layout inside their module build method.
 */
public final class LevelRuntime implements LevelContext {

    private final GameManager gameManager;
    private final LevelModule module;
    private final LevelConfig config;
    private final Pane worldPane = new Pane();
    private final List<GameObject> objects = new ArrayList<>();
    private final List<TerrainBlock> terrainBlocks = new ArrayList<>();
    private final List<MovingPlatform> movingPlatforms = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Collectible> collectibles = new ArrayList<>();
    private final Set<String> collectedKeyIds = new HashSet<>();
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private final PauseView pauseView = new PauseView();
    private final Label statusLabel = new Label();
    private Player player;
    private Door door;
    private AnimationTimer timer;
    private VBox pauseOverlay;
    private boolean paused;
    private boolean transitionTriggered;
    private IntConsumer levelSwitcher;
    private Runnable backToLevelSelect;

    public LevelRuntime(GameManager gameManager, LevelModule module) {
        this.gameManager = gameManager;
        this.module = module;
        this.config = module.getConfig();
    }

    public String getTitle() {
        return config.getTitle();
    }

    public String getMusicTrackId() {
        return config.getMusicTrackId();
    }

    public int getId() {
        return module.getId();
    }

    public Scene createScene(IntConsumer levelSwitcher, Runnable backToLevelSelect) {
        stop();
        clearState();
        this.levelSwitcher = levelSwitcher;
        this.backToLevelSelect = backToLevelSelect;

        buildWorld();
        module.build(this);
        buildPauseOverlay();

        StackPane root = new StackPane(worldPane, statusLabel, pauseOverlay);
        StackPane.setAlignment(worldPane, Pos.TOP_LEFT);
        StackPane.setAlignment(statusLabel, Pos.TOP_LEFT);
        StackPane.setAlignment(pauseOverlay, Pos.CENTER);

        Scene scene = new Scene(root, gameManager.getGameConfig().getStageWidth(), gameManager.getGameConfig().getStageHeight());
        scene.widthProperty().addListener((obs, oldValue, newValue) -> refreshViewport(scene.getWidth(), scene.getHeight()));
        scene.heightProperty().addListener((obs, oldValue, newValue) -> refreshViewport(scene.getWidth(), scene.getHeight()));
        refreshViewport(scene.getWidth(), scene.getHeight());
        installInputHandlers(scene);
        return scene;
    }

    public void start() {
        if (timer != null) {
            timer.start();
        }
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void buildWorld() {
        worldPane.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        worldPane.setBackground(new Background(new BackgroundFill(config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY)));
    }

    /*
     * Keep the logical level world fully visible inside the actual screen bounds.
     * The runtime uses a uniform scale so every level fills the screen without
     * stretching or clipping when the aspect ratio changes.
     */
    public void refreshViewport(double sceneWidth, double sceneHeight) {
        if (sceneWidth <= 0 || sceneHeight <= 0 || config.getWorldWidth() <= 0 || config.getWorldHeight() <= 0) {
            return;
        }

        double scaleX = sceneWidth / config.getWorldWidth();
        double scaleY = sceneHeight / config.getWorldHeight();
        double scale = Math.min(scaleX, scaleY);
        double scaledWidth = config.getWorldWidth() * scale;
        double scaledHeight = config.getWorldHeight() * scale;

        worldPane.setScaleX(scale);
        worldPane.setScaleY(scale);
        worldPane.setTranslateX((sceneWidth - scaledWidth) / 2.0);
        worldPane.setTranslateY((sceneHeight - scaledHeight) / 2.0);
    }

    private void buildPauseOverlay() {
        /*
         * ESC opens a compact in-level menu instead of switching away immediately.
         */
        pauseOverlay = pauseView.createOverlay(
                gameManager.getGameConfig().getStageWidth(),
                gameManager.getGameConfig().getStageHeight(),
                () -> togglePause(false),
                () -> {
                    togglePause(false);
                    levelSwitcher.accept(config.getId());
                },
                () -> {
                    togglePause(false);
                    if (config.getNextLevelId() > 0) {
                        levelSwitcher.accept(config.getNextLevelId());
                    }
                },
                () -> {
                    togglePause(false);
                    if (config.getPreviousLevelId() > 0) {
                        levelSwitcher.accept(config.getPreviousLevelId());
                    }
                },
                () -> {
                    togglePause(false);
                    backToLevelSelect.run();
                },
                config.getPreviousLevelId() > 0,
                config.getNextLevelId() > 0
        );
        pauseOverlay.setVisible(false);
        pauseOverlay.setManaged(false);
    }

    private void installInputHandlers(Scene scene) {
        scene.setOnKeyPressed(event -> {
            activeKeys.add(event.getCode());
            var controls = gameManager.getGameConfig().getControlConfig();
            if (controls.isBack(event.getCode())) {
                togglePause(!paused);
            } else if (paused) {
                activeKeys.clear();
            } else {
                module.onKeyPressed(this, event.getCode());
            }
        });

        scene.setOnKeyReleased(event -> {
            activeKeys.remove(event.getCode());
            module.onKeyReleased(this, event.getCode());
        });

        timer = new AnimationTimer() {
            private long lastFrame = -1;

            @Override
            public void handle(long now) {
                if (lastFrame < 0) {
                    lastFrame = now;
                    return;
                }

                double deltaSeconds = (now - lastFrame) / 1_000_000_000.0;
                lastFrame = now;
                if (!paused) {
                    update(deltaSeconds);
                }
            }
        };
    }

    private void togglePause(boolean newPausedState) {
        paused = newPausedState;
        pauseOverlay.setVisible(paused);
        pauseOverlay.setManaged(paused);
        if (paused) {
            stop();
        } else {
            start();
        }
    }

    private void update(double deltaSeconds) {
        for (GameObject object : objects) {
            object.update(deltaSeconds);
        }

        module.update(this, deltaSeconds);

        if (player == null) {
            return;
        }

        player.handleInput(activeKeys, gameManager.getGameConfig().getControlConfig(), config.getMoveSpeed(), config.getJumpVelocity());
        player.applyPhysics(deltaSeconds, config.getGravity());

        clampPlayerToWorld();
        resolveTerrainCollisions();
        resolveCollectibles();
        resolveEnemyCollisions();
        resolveDoorCollision();
    }

    private void clampPlayerToWorld() {
        double maxX = config.getWorldWidth() - player.getWidth();
        double maxY = config.getWorldHeight() + 120;

        if (player.getX() < 0) {
            player.setPosition(0, player.getY());
        } else if (player.getX() > maxX) {
            player.setPosition(maxX, player.getY());
        }

        if (player.getY() > maxY) {
            player.resetToSpawn();
            setStatus("Fell off the level. Reset to spawn.");
        }
    }

    private void resolveTerrainCollisions() {
        player.setOnGround(false);

        for (TerrainBlock terrainBlock : terrainBlocks) {
            if (!player.getBounds().intersects(terrainBlock.getBounds())) {
                continue;
            }

            double previousBottom = player.getPreviousY() + player.getHeight();
            double terrainTop = terrainBlock.getY();

            if (player.getVelocityY() >= 0 && previousBottom <= terrainTop + 18) {
                player.landOn(terrainTop);
                if (terrainBlock instanceof MovingPlatform movingPlatform) {
                    player.moveBy(movingPlatform.getDeltaX(), 0);
                }
            }
        }
    }

    private void resolveCollectibles() {
        for (Collectible collectible : collectibles) {
            if (!collectible.isCollected() && player.getBounds().intersects(collectible.getBounds())) {
                collectible.collect();
                if (collectible instanceof KeyItem keyItem) {
                    collectedKeyIds.add(keyItem.getKeyId());
                    setStatus("Collected key: " + keyItem.getKeyId());
                    unlockDoorIfPossible();
                } else {
                    setStatus("Collected an item.");
                }
            }
        }
    }

    private void resolveEnemyCollisions() {
        for (Enemy enemy : enemies) {
            if (player.getBounds().intersects(enemy.getBounds())) {
                enemy.onPlayerContact(player);
                player.resetToSpawn();
                setStatus("Hit by an enemy. Reset to spawn.");
                return;
            }
        }
    }

    private void resolveDoorCollision() {
        if (door == null || transitionTriggered || !player.getBounds().intersects(door.getBounds())) {
            return;
        }

        if (!door.isUnlocked()) {
            if (hasCollectedKey(door.getRequiredKeyId()) && module.canEnterDoor(this)) {
                unlockDoorIfPossible();
            } else {
                setStatus("Door is locked. Find key: " + door.getRequiredKeyId());
                return;
            }
        }

        if (!door.isUnlocked()) {
            return;
        }

        transitionTriggered = true;
        int targetLevelId = door.getTargetLevelId();
        if (targetLevelId > 0) {
            setStatus("Door opened. Loading next level...");
            stop();
            levelSwitcher.accept(targetLevelId);
        } else {
            setStatus("Door opened. Level complete.");
            stop();
            backToLevelSelect.run();
        }
    }

    private void unlockDoorIfPossible() {
        if (door != null && !door.isUnlocked() && hasCollectedKey(door.getRequiredKeyId()) && module.canEnterDoor(this)) {
            door.setUnlocked(true, config.getDoorColor());
            setStatus("Door unlocked.");
        }
    }

    private void clearState() {
        objects.clear();
        terrainBlocks.clear();
        movingPlatforms.clear();
        enemies.clear();
        collectibles.clear();
        collectedKeyIds.clear();
        activeKeys.clear();
        player = null;
        door = null;
        transitionTriggered = false;
        paused = false;
        statusLabel.setText("");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        worldPane.getChildren().clear();
        worldPane.setScaleX(1);
        worldPane.setScaleY(1);
        worldPane.setTranslateX(0);
        worldPane.setTranslateY(0);
        pauseOverlay = null;
    }

    private void addObject(GameObject object) {
        objects.add(object);
        worldPane.getChildren().add(object.getNode());
    }

    @Override
    public GameManager getGameManager() {
        return gameManager;
    }

    @Override
    public GameConfig getGameConfig() {
        return gameManager.getGameConfig();
    }

    @Override
    public LevelConfig getConfig() {
        return config;
    }

    @Override
    public audio.AudioManager getAudioManager() {
        return gameManager.getAudioManager();
    }

    @Override
    public Player createPlayer(double x, double y) {
        Player newPlayer = new Player(x, y, 36, 48, config.getPlayerColor());
        player = newPlayer;
        addObject(newPlayer);
        return newPlayer;
    }

    @Override
    public TerrainBlock createTerrainBlock(double x, double y, double width, double height) {
        TerrainBlock terrainBlock = new TerrainBlock(x, y, width, height, config.getPlatformColor());
        terrainBlocks.add(terrainBlock);
        addObject(terrainBlock);
        return terrainBlock;
    }

    @Override
    public MovingPlatform createMovingPlatform(double x, double y, double width, double height, double travelDistance, double speed) {
        MovingPlatform movingPlatform = new MovingPlatform(x, y, width, height, config.getAccentColor(), travelDistance, speed);
        movingPlatforms.add(movingPlatform);
        terrainBlocks.add(movingPlatform);
        addObject(movingPlatform);
        return movingPlatform;
    }

    @Override
    public Door createDoor(double x, double y, double width, double height, String requiredKeyId, int targetLevelId) {
        Door newDoor = new Door(x, y, width, height, config.getDoorColor(), requiredKeyId, targetLevelId);
        door = newDoor;
        addObject(newDoor);
        return newDoor;
    }

    @Override
    public KeyItem createKeyItem(double x, double y, double width, double height, String keyId) {
        KeyItem keyItem = new KeyItem(x, y, width, height, config.getAccentColor(), keyId);
        collectibles.add(keyItem);
        addObject(keyItem);
        return keyItem;
    }

    @Override
    public Enemy createEnemy(Enemy enemy) {
        enemies.add(enemy);
        addObject(enemy);
        return enemy;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Door getDoor() {
        return door;
    }

    @Override
    public List<TerrainBlock> getTerrainBlocks() {
        return List.copyOf(terrainBlocks);
    }

    @Override
    public List<MovingPlatform> getMovingPlatforms() {
        return List.copyOf(movingPlatforms);
    }

    @Override
    public List<Enemy> getEnemies() {
        return List.copyOf(enemies);
    }

    @Override
    public List<Collectible> getCollectibles() {
        return List.copyOf(collectibles);
    }

    @Override
    public boolean hasCollectedKey(String keyId) {
        return keyId != null && !keyId.isBlank() && collectedKeyIds.contains(keyId);
    }

    @Override
    public void setStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }
}
