package level.level14;

import config.GameConfig;
import core.AppRouter;
import entity.Door;
import entity.Key;
import entity.MovePlatform;
import entity.SolidBlock;
import entity.Trap;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import level.BaseLevel;

/*
 * Level 14 keeps the same world-switch mechanic but uses three keys.
 */
public final class Level14 extends BaseLevel {

    private static final double MOVE_SPEED = 300;
    private static final double JUMP_SPEED = -500;
    private static final double GRAVITY = 900;

    private static final double ENERGY_MAX = 100;
    private static final double ENERGY_START = 30;
    private static final double ENERGY_DANGER = 92;
    private static final double LIGHT_CHARGE = 17;
    private static final double DARK_DRAIN = 15;
    private static final double MIN_DARK_ENERGY = 8;

    private static final KeyCode WORLD_SWITCH_KEY = KeyCode.Q;
    private static final double MIN_LIGHT_RADIUS = 75;
    private static final double MAX_LIGHT_RADIUS = 130;
    private static final double LIGHT_CORE_RATIO = 0.2;
    private static final double LIGHT_MID_RATIO = 0.58;
    private static final double COLLISION_PADDING = 16;

    private static final int TOTAL_KEYS = 3;
    private static final double DOOR_WIDTH = 60;
    private static final double DOOR_HEIGHT = 86;
    private static final double KEY_WIDTH = 30;
    private static final double KEY_HEIGHT = 15;

    private static final double SPAWN_X = 60;
    private static final double SPAWN_Y = 592;

    private static final Color PLATFORM_COLOR = Color.web("#38bdf8");
    private static final Color TRAP_COLOR = Color.web("#ef4444");

    private static final String SOLID_BLOCK_IMAGE = "Pictures/Platforms/gold.png";
    private static final String SOLID_BLOCKV_IMAGE = "Pictures/Platforms/goldV.png";
    private static final String PLATFORM_IMAGE = "Pictures/Platforms/blue.png";
    private static final String TRAP_IMAGE = "Pictures/Platforms/red.png";
    private static final String KEY_IMAGE = "Pictures/Key/key2.png";
    private static final String DOOR_IMAGE_CLOSED = "Pictures/Portal/door1.png";
    private static final String DOOR_IMAGE_OPEN = "Pictures/Portal/door2.png";
    private static final String BACKGROUND_IMAGE = "Pictures/Backgrounds/blue_sky.png";

    private final Rectangle energyTrack = new Rectangle();
    private final Rectangle energyBar = new Rectangle();
    private final Pane darkLayer = new Pane();
    private final Door door = new Door(0, 0, DOOR_WIDTH, DOOR_HEIGHT, DOOR_IMAGE_CLOSED);
    private final List<Key> keys = new ArrayList<>();
    private final List<AttachedNode> attachedNodes = new ArrayList<>();
    private final boolean[] collectedKeys = new boolean[TOTAL_KEYS];

    private AnimationTimer timer;
    private VBox pauseMenu;
    private StackPane pauseLayer;
    private MovePlatform centerDoorPlatform;
    private MovePlatform rightPitPlatform;
    private MovePlatform centerPitPlatform;

    private boolean paused;
    private boolean finished;
    private boolean darkWorld;
    private boolean worldSwitchLocked;
    private double energy;
    private long lastFrame = -1;

    private static final class AttachedNode {
        private final MovePlatform anchor;
        private final Node node;
        private final double offsetX;
        private final double offsetY;

        private AttachedNode(MovePlatform anchor, Node node, double offsetX, double offsetY) {
            this.anchor = anchor;
            this.node = node;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        private void sync() {
            node.setLayoutX(anchor.getX() + offsetX);
            node.setLayoutY(anchor.getY() + offsetY);
        }
    }

    public Level14(GameConfig config, AppRouter router) {
        super(config, router);
    }

    @Override
    public Scene createScene() {
        resetLevelState();
        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        setBackgroundImage(BACKGROUND_IMAGE);

        player = new entity.Player(
            SPAWN_X,
            SPAWN_Y,
            config.getPlayerWidth(),
            config.getPlayerHeight(),
            config.getPlayerColor()
        );
        root.getChildren().add(player.getNode());
        solidPreviousX = player.getX();

        buildLevel();
        buildObjects();
        root.getChildren().addAll(energyTrack, energyBar);

        energyTrack.setWidth(12);
        energyTrack.setHeight(54);
        energyTrack.setArcWidth(10);
        energyTrack.setArcHeight(10);
        energyTrack.setFill(Color.rgb(15, 23, 42, 0.75));
        energyTrack.setStroke(Color.rgb(226, 232, 240, 0.45));
        energyTrack.setMouseTransparent(true);

        energyBar.setWidth(8);
        energyBar.setArcWidth(8);
        energyBar.setArcHeight(8);
        energyBar.setMouseTransparent(true);

        pauseMenu = createStandardPauseMenu(
            () -> togglePause(false),
            () -> {
                stopLoop();
                switchToMenu();
            },
            () -> {
                stopLoop();
                switchToLevel(getPreviousLevelId());
            },
            () -> {
                stopLoop();
                onGoalReached();
            },
            getPreviousLevelId() == 0,
            getNextLevelId() == 0
        );
        pauseMenu.setVisible(false);
        pauseMenu.setManaged(false);

        darkLayer.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        darkLayer.setMouseTransparent(true);

        pauseLayer = createPauseOverlay(pauseMenu);

        refreshView();

        StackPane container = new StackPane(root, darkLayer, pauseLayer);
        StackPane.setAlignment(pauseMenu, Pos.CENTER);

        Scene scene = new Scene(container, config.getWorldWidth(), config.getWorldHeight());
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                togglePause(!paused);
                return;
            }

            if (!paused) {
                activeKeys.add(event.getCode());
            }
        });

        scene.setOnKeyReleased(event -> {
            activeKeys.remove(event.getCode());
            if (event.getCode() == WORLD_SWITCH_KEY) {
                worldSwitchLocked = false;
            }
        });

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastFrame < 0) {
                    lastFrame = now;
                    return;
                }

                double deltaSeconds = (now - lastFrame) / 1_000_000_000.0;
                lastFrame = now;
                update(deltaSeconds);
            }
        };
        timer.start();
        return scene;
    }

    @Override
    protected String getLevelTitle() {
        return "Level 14";
    }

    @Override
    protected void buildLevel() {
        addGroundBlocks();
        addLeftRoomBlocks();
        addCenterAndRightBlocks();
        addMovingPlatforms();
        addTraps();
    }

    @Override
    protected int getPreviousLevelId() {
        return 13;
    }

    @Override
    protected int getNextLevelId() {
        return -1;
    }

    @Override
    protected void onGoalReached() {
        stopLoop();
        switchToMenu();
    }
    
    @Override
    protected void onSolidPlayerOutOfWorld() {
        resetPlayer();
    }

    private void resetLevelState() {
        root.getChildren().clear();
        blocks.clear();
        movePlatforms.clear();
        solidBlocks.clear();
        traps.clear();
        activeKeys.clear();
        keys.clear();
        attachedNodes.clear();

        paused = false;
        finished = false;
        darkWorld = false;
        worldSwitchLocked = false;
        energy = ENERGY_START;
        lastFrame = -1;
        activeMovePlatform = null;
        centerDoorPlatform = null;
        rightPitPlatform = null;
        centerPitPlatform = null;

        for (int index = 0; index < TOTAL_KEYS; index++) {
            collectedKeys[index] = false;
        }
    }

    private void addGroundBlocks() {
        addSolidBlock(0, 660, 80, 30, SOLID_BLOCK_IMAGE);
        addSolidBlock(0, 690, 80, 30, SOLID_BLOCK_IMAGE);
        addSolidBlock(80, 660, 80, 30, SOLID_BLOCK_IMAGE);
        addSolidBlock(80, 690, 80, 30, SOLID_BLOCK_IMAGE);
        addSolidBlock(160, 660, 80, 30, SOLID_BLOCK_IMAGE);
        addSolidBlock(160, 690, 80, 30, SOLID_BLOCK_IMAGE);
        addSolidBlock(560, 660, 80, 30, SOLID_BLOCK_IMAGE);
        addSolidBlock(560, 690, 80, 30, SOLID_BLOCK_IMAGE);
        addSolidBlock(640, 660, 80, 30, SOLID_BLOCK_IMAGE);
        addSolidBlock(640, 690, 80, 30, SOLID_BLOCK_IMAGE);
        addSolidBlock(720, 660, 80, 30, SOLID_BLOCK_IMAGE);
        addSolidBlock(720, 690, 80, 30, SOLID_BLOCK_IMAGE);
        addSolidBlock(800, 660, 80, 30, SOLID_BLOCK_IMAGE);
        addSolidBlock(800, 690, 80, 30, SOLID_BLOCK_IMAGE);
    }

    private void addLeftRoomBlocks() {
        addSolidBlock(0, 240, 80, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(0, 264, 80, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(188, 264, 80, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(268, 264, 80, 24, SOLID_BLOCK_IMAGE);

        addSolidBlock(0, 480, 120, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(100, 456, 120, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(200, 432, 120, 24, SOLID_BLOCK_IMAGE);

        addSolidBlock(0, 0, 80, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(80, 0, 80, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(160, 0, 80, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(240, 0, 80, 24, SOLID_BLOCK_IMAGE);

        addSolidBlock(300, 0, 20, 74, SOLID_BLOCKV_IMAGE);
        addSolidBlock(300, 74, 20, 74, SOLID_BLOCKV_IMAGE);
        addSolidBlock(120, 124, 90, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(210, 124, 90, 24, SOLID_BLOCK_IMAGE);
    }

    private void addCenterAndRightBlocks() {
        addSolidBlock(880, 0, 80, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(960, 0, 80, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(1040, 0, 80, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(1120, 0, 80, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(1200, 0, 80, 24, SOLID_BLOCK_IMAGE);

        addSolidBlock(560, 480, 100, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(660, 480, 100, 24, SOLID_BLOCK_IMAGE);

        addSolidBlock(1020, 288, 88, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(1108, 288, 88, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(1196, 288, 88, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(1284, 288, 88, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(1072, 288, 88, 24, SOLID_BLOCK_IMAGE);

        addSolidBlock(1020, 160, 40, 128, SOLID_BLOCKV_IMAGE);
        addSolidBlock(1060, 160, 40, 128, SOLID_BLOCKV_IMAGE);

        addSolidBlock(1100, 200, 60, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(860, 580, 30, 120, SOLID_BLOCKV_IMAGE);
        addSolidBlock(890, 580, 30, 120, SOLID_BLOCKV_IMAGE);
        addSolidBlock(1160, 624, 120, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(860, 408, 80, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(920, 456, 80, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(980, 504, 80, 24, SOLID_BLOCK_IMAGE);
    }

    private void addMovingPlatforms() {
        centerDoorPlatform = new MovePlatform(
            480, 120, 180, 24,
            PLATFORM_COLOR, PLATFORM_IMAGE,
            MovePlatform.Direction.HORIZONTAL,
            480, 600, 68
        );
        centerPitPlatform = new MovePlatform(
            420, 300, 140, 24,
            PLATFORM_COLOR, PLATFORM_IMAGE,
            MovePlatform.Direction.VERTICAL,
            300, 372, 80
        );
        rightPitPlatform = new MovePlatform(
            1038, 560, 96, 24,
            PLATFORM_COLOR, PLATFORM_IMAGE,
            MovePlatform.Direction.HORIZONTAL,
            1038, 1100, 68
        );

        MovePlatform upperRightPlatform = new MovePlatform(
            800, 204, 120, 24,
            PLATFORM_COLOR, PLATFORM_IMAGE,
            MovePlatform.Direction.VERTICAL,
            204, 276, 80
        );
        MovePlatform lowerCenterPlatform = new MovePlatform(
            240, 576, 160, 24,
            PLATFORM_COLOR, PLATFORM_IMAGE,
            MovePlatform.Direction.HORIZONTAL,
            240, 360, 80
        );

        movePlatforms.add(centerDoorPlatform);
        movePlatforms.add(centerPitPlatform);
        movePlatforms.add(rightPitPlatform);
        movePlatforms.add(upperRightPlatform);
        movePlatforms.add(lowerCenterPlatform);

        for (MovePlatform platform : movePlatforms) {
            root.getChildren().add(platform.getNode());
        }
    }

    private void addTraps() {
        Trap leftTrap = new Trap(80, 264, 108, 24, TRAP_COLOR, TRAP_IMAGE);
        Trap bottomTrap = new Trap(240, 660, 320, 60, TRAP_COLOR, TRAP_IMAGE);
        Trap rightBottomLeftTrap = new Trap(744, 690, 62, 24, TRAP_COLOR, TRAP_IMAGE);
        Trap rightBottomMiddleTrap = new Trap(806, 690, 62, 24, TRAP_COLOR, TRAP_IMAGE);
        Trap rightBottomRightTrap = new Trap(868, 690, 62, 24, TRAP_COLOR, TRAP_IMAGE);
        Trap centerTrap = new Trap(560, 504, 200, 24, TRAP_COLOR, TRAP_IMAGE);
        Trap upperTrap = new Trap(1020, 136, 80, 24, TRAP_COLOR, TRAP_IMAGE);
        Trap movingTrap = new Trap(rightPitPlatform.getX(), rightPitPlatform.getY(), 102, 24, TRAP_COLOR, TRAP_IMAGE);

        traps.add(leftTrap);
        traps.add(bottomTrap);
        traps.add(rightBottomLeftTrap);
        traps.add(rightBottomMiddleTrap);
        traps.add(rightBottomRightTrap);
        traps.add(centerTrap);
        traps.add(upperTrap);
        traps.add(movingTrap);

        for (Trap trap : traps) {
            root.getChildren().add(trap.getNode());
        }

        attachedNodes.add(new AttachedNode(rightPitPlatform, movingTrap.getNode(), 0, 0));
    }

    private void buildObjects() {
        root.getChildren().add(door.getNode());
        AttachedNode attachedDoor = new AttachedNode(
            centerDoorPlatform,
            door.getNode(),
            (centerDoorPlatform.getWidth() - DOOR_WIDTH) * 0.5,
            -DOOR_HEIGHT
        );
        attachedNodes.add(attachedDoor);
        attachedDoor.sync();

        Key topLeftKey = new Key(252, 64, KEY_WIDTH, KEY_IMAGE);
        topLeftKey.setSize(KEY_WIDTH, KEY_HEIGHT);
        keys.add(topLeftKey);
        root.getChildren().add(topLeftKey.getNode());

        Key upperRightKey = new Key(1192, 246, KEY_WIDTH, KEY_IMAGE);
        upperRightKey.setSize(KEY_WIDTH, KEY_HEIGHT);
        keys.add(upperRightKey);
        root.getChildren().add(upperRightKey.getNode());

        Key lowerRightKey = new Key(1200, 536, KEY_WIDTH, KEY_IMAGE);
        lowerRightKey.setSize(KEY_WIDTH, KEY_HEIGHT);
        keys.add(lowerRightKey);
        root.getChildren().add(lowerRightKey.getNode());
    }

    private void stopLoop() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void update(double deltaSeconds) {
        // Stop updating when the level is paused or already completed.
        if (paused || finished) {
            return;
        }

        // Handle the light/dark world switch and prevent repeated toggles from one key press.
        if (activeKeys.contains(WORLD_SWITCH_KEY) && !worldSwitchLocked) {
            if (darkWorld) {
                darkWorld = false;
            } else if (energy > MIN_DARK_ENERGY) {
                darkWorld = true;
            }
            worldSwitchLocked = true;
        }

        // Update moving platforms first, then sync objects attached to those platforms.
        for (MovePlatform platform : movePlatforms) {
            platform.update(deltaSeconds);
        }
        for (AttachedNode attachedNode : attachedNodes) {
            attachedNode.sync();
        }

        // Move the player together with the platform they are currently standing on.
        moveWithPlatform();

        // Check hazards after the environment has moved.
        if (checkTrapCollision()) {
            return;
        }

        // Update player input, physics and collision resolution.
        updatePlayer(deltaSeconds);

        // Check hazards again after the player has moved.
        if (checkTrapCollision()) {
            return;
        }

        // Update the energy mechanic based on the current world state.
        if (darkWorld) {
            energy -= DARK_DRAIN * deltaSeconds;
            if (energy <= 0) {
                energy = MIN_DARK_ENERGY;
                darkWorld = false;
            }
        } else {
            energy += LIGHT_CHARGE * deltaSeconds;
            if (energy >= ENERGY_DANGER) {
                resetPlayer();
                return;
            }
        }

        if (energy < 0) {
            energy = 0;
        }
        if (energy > ENERGY_MAX) {
            energy = ENERGY_MAX;
        }

        // Keys are only visible in the dark world and can be collected on contact.
        for (int index = 0; index < TOTAL_KEYS; index++) {
            Key key = keys.get(index);
            boolean keyVisible = darkWorld && !collectedKeys[index];
            key.setVisible(keyVisible);

            if (!collectedKeys[index] && keyVisible && player.getBounds().intersects(key.getBounds())) {
                collectedKeys[index] = true;
                key.setCollected(true);
            }
        }

        // Count collected keys for door state updates and win-condition checks.
        int collectedKeyCount = 0;
        for (boolean collectedKey : collectedKeys) {
            if (collectedKey) {
                collectedKeyCount++;
            }
        }

        // Refresh UI and object visuals to match the latest game state.
        refreshView();

        // Finish the level only after all keys are collected and the player reaches the door.
        if (!darkWorld && collectedKeyCount == TOTAL_KEYS && player.getBounds().intersects(door.getBounds())) {
            finished = true;
            onGoalReached();
        }
    }

    private void moveWithPlatform() {
        if (activeMovePlatform != null) {
            player.moveBy(activeMovePlatform.getDeltaX(), activeMovePlatform.getDeltaY());
        }
    }

    private void updatePlayer(double deltaSeconds) {
        solidPreviousX = player.getX();
        player.handleInput(activeKeys, config.getControlConfig(), MOVE_SPEED, JUMP_SPEED);
        player.applyPhysics(deltaSeconds, GRAVITY);
        resolveLevelCollisions();
        clampSolidPlayer();
    }

    private void resolveLevelCollisions() {
        player.setOnGround(false);
        activeMovePlatform = null;
        resolveSolidCollisions();

        for (MovePlatform platform : movePlatforms) {
            if (!player.getBounds().intersects(platform.getBounds())) {
                continue;
            }

            double playerLeft = player.getX();
            double playerRight = player.getX() + player.getWidth();
            double playerTop = player.getY();
            double playerBottom = player.getY() + player.getHeight();
            double platformLeft = platform.getX();
            double platformRight = platform.getX() + platform.getWidth();
            double platformTop = platform.getY();
            double platformBottom = platform.getY() + platform.getHeight();
            double previousTop = player.getPreviousY();
            double previousBottom = player.getPreviousY() + player.getHeight();

            if (player.getVelocityY() >= 0 && previousBottom <= platformTop + COLLISION_PADDING) {
                player.landOn(platformTop);
                activeMovePlatform = platform;
                continue;
            }

            if (player.getVelocityY() < 0 && previousTop >= platformBottom - COLLISION_PADDING) {
                player.hitCeiling(platformBottom);
                if (platform.getDeltaY() > 0) {
                    player.moveBy(0, platform.getDeltaY());
                }
                continue;
            }

            double overlapLeft = playerRight - platformLeft;
            double overlapRight = platformRight - playerLeft;
            double overlapTop = playerBottom - platformTop;
            double overlapBottom = platformBottom - playerTop;
            double smallest = Math.min(Math.min(overlapLeft, overlapRight), Math.min(overlapTop, overlapBottom));

            if (smallest == overlapBottom) {
                player.hitCeiling(platformBottom);
                if (platform.getDeltaY() > 0) {
                    player.moveBy(0, platform.getDeltaY());
                }
            } else if (smallest == overlapTop) {
                player.landOn(platformTop);
                activeMovePlatform = platform;
            } else if (smallest == overlapLeft) {
                player.setPosition(platformLeft - player.getWidth(), player.getY());
            } else {
                player.setPosition(platformRight, player.getY());
            }
        }
    }

    private boolean checkTrapCollision() {
        for (Trap trap : traps) {
            if (player.getBounds().intersects(trap.getBounds())) {
                resetPlayer();
                return true;
            }
        }
        return false;
    }

    private void refreshView() {
        if (!darkWorld) {
            darkLayer.getChildren().clear();
            darkLayer.setVisible(false);
        } else {
            double energyRatio = energy / ENERGY_MAX;
            double lightRadius = MIN_LIGHT_RADIUS + (MAX_LIGHT_RADIUS - MIN_LIGHT_RADIUS) * energyRatio;
            double centerX = player.getX() + player.getWidth() * 0.5;
            double centerY = player.getY() + player.getHeight() * 0.5;
            darkLayer.getChildren().setAll(createDarkOverlay(centerX, centerY, lightRadius));
            darkLayer.setVisible(true);
        }

        for (SolidBlock block : solidBlocks) {
            block.getNode().setOpacity(darkWorld ? 0.88 : 1.0);
        }
        for (MovePlatform platform : movePlatforms) {
            platform.getNode().setOpacity(darkWorld ? 0.95 : 1.0);
        }
        for (Trap trap : traps) {
            trap.getNode().setOpacity(darkWorld ? 0.9 : 1.0);
        }

        int collectedKeyCount = 0;
        for (boolean collectedKey : collectedKeys) {
            if (collectedKey) {
                collectedKeyCount++;
            }
        }
        boolean unlocked = collectedKeyCount == TOTAL_KEYS;
        door.refreshStyle(unlocked, darkWorld);
        door.setSize(DOOR_WIDTH, DOOR_HEIGHT);
        door.setImage(unlocked ? DOOR_IMAGE_OPEN : DOOR_IMAGE_CLOSED);

        for (int index = 0; index < keys.size(); index++) {
            Key key = keys.get(index);
            key.refreshStyle();
            key.setSize(KEY_WIDTH, KEY_HEIGHT);
            key.setImage(KEY_IMAGE);
            key.setVisible(darkWorld && !collectedKeys[index]);
        }

        double energyHeight = 50 * (energy / ENERGY_MAX);
        double barX = player.getX() + player.getWidth() + 8;
        double barY = player.getY() - 4;
        Color energyColor;
        if (energy >= ENERGY_DANGER - 8) {
            energyColor = Color.web("#ef4444");
        } else if (energy >= 60) {
            energyColor = Color.web("#f59e0b");
        } else {
            energyColor = Color.web("#22c55e");
        }

        energyTrack.setLayoutX(barX);
        energyTrack.setLayoutY(barY);
        energyBar.setLayoutX(barX + 2);
        energyBar.setLayoutY(barY + 2 + (50 - energyHeight));
        energyBar.setHeight(energyHeight);
        energyBar.setFill(energyColor);
    }

    private void resetPlayer() {
        player.resetToSpawn();
        solidPreviousX = player.getX();
        darkWorld = false;
        worldSwitchLocked = false;
        activeMovePlatform = null;
        energy = ENERGY_START;
        refreshView();
    }

    private void togglePause(boolean newState) {
        paused = newState;
        pauseLayer.setVisible(newState);
        pauseLayer.setManaged(newState);
        pauseMenu.setVisible(newState);
        pauseMenu.setManaged(newState);

        if (newState) {
            activeKeys.clear();
            worldSwitchLocked = false;
        } else {
            lastFrame = -1;
        }
    }

    private Rectangle createDarkOverlay(double playerX, double playerY, double radius) {
        Rectangle darkOverlay = new Rectangle(config.getWorldWidth(), config.getWorldHeight());
        darkOverlay.setFill(new RadialGradient(
            0,
            0,
            playerX,
            playerY,
            radius,
            false,
            CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(3, 7, 18, 0.06)),
            new Stop(LIGHT_CORE_RATIO, Color.rgb(3, 7, 18, 0.18)),
            new Stop(LIGHT_MID_RATIO, Color.rgb(3, 7, 18, 0.72)),
            new Stop(1, Color.rgb(0, 0, 0, 1.0))
        ));
        darkOverlay.setMouseTransparent(true);
        return darkOverlay;
    }
}
