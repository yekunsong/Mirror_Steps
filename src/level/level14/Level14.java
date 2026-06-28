package level.level14;

import config.GameConfig;
import core.AppRouter;
import entity.MovePlatform;
import entity.SolidBlock;
import entity.Trap;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
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
 * Level 14 expands the world-switch mechanic into a larger three-key dungeon.
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

    private static final int TOTAL_KEYS = 3;
    private static final double DOOR_WIDTH = 52;
    private static final double DOOR_HEIGHT = 86;
    private static final double KEY_SIZE = 20;

    private static final double SPAWN_X = 60;
    private static final double SPAWN_Y = 592;

    private static final Color PLATFORM_COLOR = Color.web("#38bdf8");
    private static final Color TRAP_COLOR = Color.web("#ef4444");

    private final Rectangle energyTrack = new Rectangle();
    private final Rectangle energyBar = new Rectangle();
    private final Rectangle door = new Rectangle(DOOR_WIDTH, DOOR_HEIGHT);
    private final Label doorLabel = new Label("DOOR");
    private final Label doorStatusLabel = new Label();
    private final Pane darkLayer = new Pane();
    private final List<Rectangle> keyNodes = new ArrayList<>();
    private final List<Label> keyLabels = new ArrayList<>();
    private final List<AttachedNode> attachedNodes = new ArrayList<>();
    private final boolean[] collectedKeys = new boolean[TOTAL_KEYS];

    private AnimationTimer timer;
    private VBox levelPauseMenu;
    private StackPane pauseLayer;
    private boolean paused;
    private boolean finished;
    private boolean darkWorld;
    private boolean worldSwitchLocked;
    private double energy;
    private long lastFrame = -1;
    private MovePlatform centerDoorPlatform;
    private MovePlatform rightPitPlatform;
    private MovePlatform centerPitPlatform;

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
        root.getChildren().clear();
        blocks.clear();
        movePlatforms.clear();
        solidBlocks.clear();
        traps.clear();
        activeKeys.clear();
        keyNodes.clear();
        keyLabels.clear();
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

        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        root.setBackground(new Background(new BackgroundFill(Color.web("#f8fafc"), CornerRadii.EMPTY, Insets.EMPTY)));

        createLevelPlayer();
        buildLevel();
        buildObjects();
        buildEnergyBar();
        buildPauseLayer();
        checkKeys();
        updateView();

        StackPane container = new StackPane(root, darkLayer, pauseLayer);
        StackPane.setAlignment(levelPauseMenu, Pos.CENTER);

        Scene scene = new Scene(container, config.getWorldWidth(), config.getWorldHeight());
        installInput(scene);
        startLoop();
        return scene;
    }

    @Override
    protected String getLevelTitle() {
        return "Level 14";
    }

    @Override
    protected void buildLevel() {
        // Base floor and safe landing segments.
        addSolidBlock(0, 660, 240, 60);
        addSolidBlock(560, 660, 300, 60);
        
        // Left-side climb and room shell.
        addSolidBlock(0, 240, 80, 24);
        addSolidBlock(0, 264, 360, 24);
        addSolidBlock(0, 480, 120, 24);
        addSolidBlock(100, 456, 120, 24);
        addSolidBlock(200, 432, 120, 24);
        addSolidBlock(0, 0, 320, 24);
        addSolidBlock(300, 0, 20, 148);
        addSolidBlock(120, 124, 180, 24);

        // Central and right-side fixed structures.
        addSolidBlock(880, 0, 400, 24);
        addSolidBlock(560, 480, 200, 24);
        addSolidBlock(1020, 288, 440, 24);
        addSolidBlock(1020, 160, 80, 128);
        addSolidBlock(1100, 200, 60, 24);
        addSolidBlock(860, 580, 60, 120);
        addSolidBlock(1160, 624, 120, 24);
        addSolidBlock(860, 408, 80, 24);
        addSolidBlock(920, 456, 80, 24);
        addSolidBlock(980, 504, 80, 24);
        
        
        // Move platforms are placed after all static collision surfaces.
        centerDoorPlatform = addLevelMovePlatform(480, 120, 180, 24, PLATFORM_COLOR, MovePlatform.Direction.HORIZONTAL, 480, 600, 68);
        centerPitPlatform = addLevelMovePlatform(420, 300, 140, 24, PLATFORM_COLOR, MovePlatform.Direction.VERTICAL, 300, 372, 80);
        
        addLevelMovePlatform(800, 204, 120, 24, PLATFORM_COLOR, MovePlatform.Direction.VERTICAL, 204, 276, 80);
        rightPitPlatform = addLevelMovePlatform(1038, 560, 96, 24, PLATFORM_COLOR, MovePlatform.Direction.HORIZONTAL, 1038, 1100, 68);
        addLevelMovePlatform(240, 576, 160, 24, PLATFORM_COLOR, MovePlatform.Direction.HORIZONTAL, 240, 360, 80);

        // Trap strips translated from the concept art.
        addTrap(80, 264, 108, 24, TRAP_COLOR);
        addTrap(100, 456, 100, 24, TRAP_COLOR);
        addTrap(240, 660, 320, 60, TRAP_COLOR);
        addTrap(744, 690, 186, 24, TRAP_COLOR);
        addTrap(560, 504, 200, 24, TRAP_COLOR);
        addTrap(1020, 136, 80, 24, TRAP_COLOR);
        addAttachedTrap(rightPitPlatform, 0, 0, 102, 24, TRAP_COLOR);
//        addTrap(960, 660, 344, 60, TRAP_COLOR);

        setGoal(660, 92);
        goal.setVisible(false);
    }
    
    @Override
    protected int getPreviousLevelId() {
        return 13;
    }

    @Override
    protected int getNextLevelId() {
        return 15;
    }

    @Override
    protected void onGoalReached() {
        stopLoop();
        switchToLevel(15);
    }

    @Override
    protected void onSolidPlayerOutOfWorld() {
        resetPlayer(false);
    }

    private void createLevelPlayer() {
        player = new entity.Player(SPAWN_X, SPAWN_Y, config.getPlayerWidth(), config.getPlayerHeight(), config.getPlayerColor());
        root.getChildren().add(player.getNode());
        solidPreviousX = player.getX();
    }

    private MovePlatform addLevelMovePlatform(
            double x,
            double y,
            double width,
            double height,
            Color color,
            MovePlatform.Direction direction,
            double minBound,
            double maxBound,
            double speed) {
        MovePlatform platform = new MovePlatform(x, y, width, height, color, direction, minBound, maxBound, speed);
        movePlatforms.add(platform);
        root.getChildren().add(platform.getNode());
        return platform;
    }

    private void addAttachedTrap(
            MovePlatform anchor,
            double offsetX,
            double offsetY,
            double width,
            double height,
            Color color) {
        Trap trap = new Trap(anchor.getX() + offsetX, anchor.getY() + offsetY, width, height, color);
        traps.add(trap);
        root.getChildren().add(trap.getNode());
        attachedNodes.add(new AttachedNode(anchor, trap.getNode(), offsetX, offsetY));
    }

    private void buildObjects() {
        door.setArcWidth(14);
        door.setArcHeight(14);
        door.setStrokeWidth(3);
        root.getChildren().addAll(door, doorLabel, doorStatusLabel);

        attachNode(centerDoorPlatform, door, (centerDoorPlatform.getWidth() - DOOR_WIDTH) * 0.5, -DOOR_HEIGHT);

        styleFloatingLabel(doorLabel, Color.web("#fde68a"));
        styleFloatingLabel(doorStatusLabel, Color.web("#e2e8f0"));
        attachNode(centerDoorPlatform, doorLabel, (centerDoorPlatform.getWidth() - DOOR_WIDTH) * 0.5 - 4, -DOOR_HEIGHT - 36);
        attachNode(centerDoorPlatform, doorStatusLabel, (centerDoorPlatform.getWidth() - 102) * 0.5, -DOOR_HEIGHT - 22);

        addKeyObject(252, 64, "KEY 1");
        addKeyObject(1192, 246, "KEY 2");
        addKeyObject(1200, 536, "KEY 3");
    }

    private void attachNode(MovePlatform anchor, Node node, double offsetX, double offsetY) {
        AttachedNode attachedNode = new AttachedNode(anchor, node, offsetX, offsetY);
        attachedNodes.add(attachedNode);
        attachedNode.sync();
    }
    
    private void addKeyObject(double x, double y, String labelText) {
        Rectangle keyNode = new Rectangle(KEY_SIZE, KEY_SIZE);
        keyNode.setArcWidth(10);
        keyNode.setArcHeight(10);
        keyNode.setLayoutX(x);
        keyNode.setLayoutY(y);

        Label label = new Label(labelText);
        styleFloatingLabel(label, Color.web("#fde68a"));
        label.setLayoutX(x - 6);
        label.setLayoutY(y - 22);

        keyNodes.add(keyNode);
        keyLabels.add(label);
        root.getChildren().addAll(keyNode, label);
    }

    private void styleFloatingLabel(Label label, Color textColor) {
        label.setTextFill(textColor);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        label.setMouseTransparent(true);
    }

    private void buildEnergyBar() {
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

        darkLayer.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        darkLayer.setMouseTransparent(true);

        root.getChildren().addAll(energyTrack, energyBar);
    }

    private void buildPauseLayer() {
        Label title = new Label(getLevelTitle());
        title.getStyleClass().add("pause-title");

        Button resumeButton = new Button("Resume");
        resumeButton.getStyleClass().add("secondary-button");
        resumeButton.setOnAction(event -> togglePause(false));

        Button menuButton = new Button("Menu");
        menuButton.getStyleClass().add("secondary-button");
        menuButton.setOnAction(event -> {
            stopLoop();
            switchToMenu();
        });

        Button previousButton = new Button("Prev");
        previousButton.getStyleClass().add("secondary-button");
        previousButton.setOnAction(event -> {
            stopLoop();
            switchToLevel(getPreviousLevelId());
        });

        Button nextButton = new Button("Next");
        nextButton.getStyleClass().add("primary-button");
        nextButton.setOnAction(event -> {
            stopLoop();
            onGoalReached();
        });
        
        levelPauseMenu = new VBox(14, title, resumeButton, menuButton, previousButton, nextButton);
        levelPauseMenu.setAlignment(Pos.CENTER);
        levelPauseMenu.setPadding(new Insets(28));
        levelPauseMenu.setMaxWidth(260);
        levelPauseMenu.getStyleClass().add("pause-panel");
        levelPauseMenu.setVisible(false);
        levelPauseMenu.setManaged(false);

        pauseLayer = new StackPane(levelPauseMenu);
        pauseLayer.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        pauseLayer.setMinSize(config.getWorldWidth(), config.getWorldHeight());
        pauseLayer.setVisible(false);
        pauseLayer.setManaged(false);
        pauseLayer.getStyleClass().add("overlay-backdrop");
    }

    private void installInput(Scene scene) {
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
    }

    private void startLoop() {
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
    }
    
    private void stopLoop() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void update(double deltaSeconds) {
        if (paused || finished) {
            return;
        }

        handleWorldSwitch();
        updateMovePlatforms(deltaSeconds);

        if (activeMovePlatform != null) {
            player.moveBy(activeMovePlatform.getDeltaX(), activeMovePlatform.getDeltaY());
        }

        solidPreviousX = player.getX();
        player.handleInput(activeKeys, config.getControlConfig(), MOVE_SPEED, JUMP_SPEED);
        player.applyPhysics(deltaSeconds, GRAVITY);
        resolveLevelCollisions();
        clampSolidPlayer();

        if (checkTrapCollision()) {
            return;
        }

        if (updateEnergy(deltaSeconds)) {
            return;
        }

        checkKeys();
        updateView();
        checkDoor();
    }

    private void handleWorldSwitch() {
        boolean switchPressed = activeKeys.contains(WORLD_SWITCH_KEY);
        if (switchPressed && !worldSwitchLocked) {
            if (darkWorld) {
                darkWorld = false;
            } else if (energy > MIN_DARK_ENERGY) {
                darkWorld = true;
            }
            worldSwitchLocked = true;
        }
    }

    private void updateMovePlatforms(double deltaSeconds) {
        for (MovePlatform platform : movePlatforms) {
            platform.update(deltaSeconds);
        }
        syncAttachedNodes();
    }

    private void syncAttachedNodes() {
        for (AttachedNode attachedNode : attachedNodes) {
            attachedNode.sync();
        }
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

            double blockLeft = platform.getX();
            double blockRight = platform.getX() + platform.getWidth();
            double blockTop = platform.getY();
            double blockBottom = platform.getY() + platform.getHeight();

            double previousLeft = playerLeft;
            double previousRight = playerRight;
            if (platform.getDirection() == MovePlatform.Direction.HORIZONTAL) {
                previousLeft -= platform.getDeltaX();
                previousRight -= platform.getDeltaX();
            }

            double previousTop = player.getPreviousY();
            double previousBottom = player.getPreviousY() + player.getHeight();

            if (player.getVelocityY() >= 0 && previousBottom <= blockTop + 16) {
                player.landOn(blockTop);
                activeMovePlatform = platform;
                continue;
            }

            if (player.getVelocityY() < 0 && previousTop >= blockBottom - 16) {
                player.hitCeiling(blockBottom);
                continue;
            }

            if (previousRight <= blockLeft) {
                player.setPosition(blockLeft - player.getWidth(), player.getY());
                continue;
            }

            if (previousLeft >= blockRight) {
                player.setPosition(blockRight, player.getY());
                continue;
            }

            double overlapLeft = playerRight - blockLeft;
            double overlapRight = blockRight - playerLeft;
            double overlapTop = playerBottom - blockTop;
            double overlapBottom = blockBottom - playerTop;
            double smallest = Math.min(Math.min(overlapLeft, overlapRight), Math.min(overlapTop, overlapBottom));

            if (smallest == overlapTop) {
                player.landOn(blockTop);
                activeMovePlatform = platform;
            } else if (smallest == overlapBottom) {
                player.hitCeiling(blockBottom);
            } else if (smallest == overlapLeft) {
                player.setPosition(blockLeft - player.getWidth(), player.getY());
            } else {
                player.setPosition(blockRight, player.getY());
            }
        }
    }

    private boolean checkTrapCollision() {
        for (Trap trap : traps) {
            if (player.getBounds().intersects(trap.getBounds())) {
                resetPlayer(false);
                return true;
            }
        }
        return false;
    }

    private boolean updateEnergy(double deltaSeconds) {
        if (darkWorld) {
            energy -= DARK_DRAIN * deltaSeconds;
            if (energy <= 0) {
                energy = MIN_DARK_ENERGY;
                darkWorld = false;
            }
        } else {
            energy += LIGHT_CHARGE * deltaSeconds;
            if (energy >= ENERGY_DANGER) {
                resetPlayer(true);
                return true;
            }
        }

        if (energy < 0) {
            energy = 0;
        }
        if (energy > ENERGY_MAX) {
            energy = ENERGY_MAX;
        }
        return false;
    }
    
    private void checkKeys() {
        for (int index = 0; index < TOTAL_KEYS; index++) {
            Rectangle keyNode = keyNodes.get(index);
            Label label = keyLabels.get(index);
            boolean keyVisible = darkWorld && !collectedKeys[index];
            keyNode.setVisible(keyVisible);
            label.setVisible(keyVisible);

            if (!collectedKeys[index] && darkWorld && player.getBounds().intersects(keyNode.getBoundsInParent())) {
                collectedKeys[index] = true;
                keyNode.setVisible(false);
                label.setVisible(false);
            }
        }

        doorStatusLabel.setText(getCollectedKeyCount() + " / " + TOTAL_KEYS + " KEYS");
    }

    private void updateView() {
        if (darkWorld) {
            root.setBackground(new Background(new BackgroundFill(Color.web("#0f172a"), CornerRadii.EMPTY, Insets.EMPTY)));
            double centerX = player.getX() + player.getWidth() * 0.5;
            double centerY = player.getY() + player.getHeight() * 0.5;
            darkLayer.getChildren().setAll(createDarkOverlay(centerX, centerY, getCurrentLightRadius()));
        } else {
            root.setBackground(new Background(new BackgroundFill(Color.web("#f8fafc"), CornerRadii.EMPTY, Insets.EMPTY)));
            darkLayer.getChildren().clear();
        }
        darkLayer.setVisible(darkWorld);

        for (SolidBlock block : solidBlocks) {
            block.getNode().setOpacity(darkWorld ? 0.88 : 1.0);
        }
        for (MovePlatform platform : movePlatforms) {
            platform.getNode().setOpacity(darkWorld ? 0.95 : 1.0);
        }
        for (Trap trap : traps) {
            trap.getNode().setOpacity(darkWorld ? 0.9 : 1.0);
        }

        int keyCount = getCollectedKeyCount();
        boolean doorUnlocked = keyCount == TOTAL_KEYS;

        door.setFill(doorUnlocked ? Color.web("#84cc16") : Color.web("#7c3aed"));
        door.setStroke(darkWorld ? Color.web("#c4b5fd") : Color.web("#ede9fe"));
        door.setOpacity(darkWorld ? 0.35 : 1.0);

        doorStatusLabel.setTextFill(doorUnlocked ? Color.web("#bef264") : Color.web("#e2e8f0"));

        for (Rectangle keyNode : keyNodes) {
            keyNode.setFill(Color.web("#facc15"));
            keyNode.setStroke(Color.web("#fef08a"));
        }

        double energyHeight = 50 * (energy / ENERGY_MAX);
        double barX = player.getX() + player.getWidth() + 8;
        double barY = player.getY() - 4;
        energyTrack.setLayoutX(barX);
        energyTrack.setLayoutY(barY);
        energyBar.setLayoutX(barX + 2);
        energyBar.setLayoutY(barY + 2 + (50 - energyHeight));
        energyBar.setHeight(energyHeight);
        energyBar.setFill(getEnergyColor());
    }

    private void checkDoor() {
        if (darkWorld || getCollectedKeyCount() < TOTAL_KEYS) {
            return;
        }

        if (player.getBounds().intersects(door.getBoundsInParent())) {
            finished = true;
            onGoalReached();
        }
    }

    private int getCollectedKeyCount() {
        int count = 0;
        for (boolean collectedKey : collectedKeys) {
            if (collectedKey) {
                count++;
            }
        }
        return count;
    }
    
    private void resetPlayer(boolean loseKeys) {
        player.resetToSpawn();
        solidPreviousX = player.getX();
        darkWorld = false;
        worldSwitchLocked = false;
        activeMovePlatform = null;
        energy = ENERGY_START;

        if (loseKeys) {
            for (int index = 0; index < TOTAL_KEYS; index++) {
                collectedKeys[index] = false;
            }
        }

        checkKeys();
        updateView();
    }

    private void togglePause(boolean newState) {
        paused = newState;
        pauseLayer.setVisible(newState);
        pauseLayer.setManaged(newState);
        levelPauseMenu.setVisible(newState);
        levelPauseMenu.setManaged(newState);

        if (newState) {
            activeKeys.clear();
            worldSwitchLocked = false;
        } else {
            lastFrame = -1;
        }
    }

    private Color getEnergyColor() {
        if (energy >= ENERGY_DANGER - 8) {
            return Color.web("#ef4444");
        }
        if (energy >= 60) {
            return Color.web("#f59e0b");
        }
        return Color.web("#22c55e");
    }

    private double getCurrentLightRadius() {
        double energyRatio = energy / ENERGY_MAX;
        return MIN_LIGHT_RADIUS + (MAX_LIGHT_RADIUS - MIN_LIGHT_RADIUS) * energyRatio;
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