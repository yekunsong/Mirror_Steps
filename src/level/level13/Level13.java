package level.level13;

import config.GameConfig;
import core.AppRouter;
import entity.Door;
import entity.Key;
import entity.MovePlatform;
import entity.SolidBlock;
import entity.Trap;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.CycleMethod;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import level.BaseLevel;

/*
 * Level 13 is a teaching stage for the Light World / Dark World mechanic.
 */
public final class Level13 extends BaseLevel {

    private static final double MOVE_SPEED = 300;
    private static final double JUMP_SPEED = -600;
    private static final double GRAVITY = 900;

    private static final double ENERGY_MAX = 100;
    private static final double ENERGY_START = 50;
    private static final double ENERGY_DANGER = 92;
    private static final double LIGHT_CHARGE = 17;
    private static final double DARK_DRAIN = 15;
    private static final double MIN_DARK_ENERGY = 8;

    private static final KeyCode WORLD_SWITCH_KEY = KeyCode.Q;
    private static final double MIN_LIGHT_RADIUS = 75;
    private static final double MAX_LIGHT_RADIUS = 130;
    private static final double LIGHT_CORE_RATIO = 0.2;
    private static final double LIGHT_MID_RATIO = 0.58;
    private static final double DOOR_WIDTH = 52;
    private static final double DOOR_HEIGHT = 86;
    private static final double KEY_SIZE = 20;
    private static final double COLLISION_PADDING = 16;
    private static final Color PLATFORM_COLOR = Color.web("#38bdf8");
    private static final Color TRAP_COLOR = Color.web("#ef4444");
    private static final String SOLID_BLOCK_IMAGE = "Pictures/Platforms/gold.png";
    private static final String PLATFORM_IMAGE = "Pictures/Platforms/blue.png";
    private static final String TRAP_IMAGE = "Pictures/Platforms/red.png";
    private static final String DOOR_IMAGE = "Pictures/Portal/portal_left.png";
    
    private final Rectangle energyTrack = new Rectangle();
    private final Rectangle energyBar = new Rectangle();
    private final Door door = new Door(0, 0, DOOR_WIDTH, DOOR_HEIGHT, DOOR_IMAGE);
    private final Key key = new Key(0, 0, KEY_SIZE);
    private final Pane darkLayer = new Pane();

    private AnimationTimer timer;
    private VBox levelPauseMenu;
    private StackPane pauseLayer;
    private boolean paused;
    private boolean finished;
    private boolean darkWorld;
    private boolean hasKey;
    private boolean worldSwitchLocked;
    private double energy;
    private long lastFrame = -1;
    private MovePlatform liftPlatform;
    private MovePlatform activeMovePlatform;

    public Level13(GameConfig config, AppRouter router) {
        super(config, router);
    }

    @Override
    public Scene createScene() {
        resetLevelState();
        configureSceneRoot();

        createLevelPlayer();
        buildLevel();
        buildObjects();
        buildEnergyBar();
        buildPauseLayer();
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
        return "Level 13";
    }

    @Override
    protected void buildLevel() {
        addSolidBlock(100, 600, 100, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(250, 450, 100, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(400, 300, 100, 24, SOLID_BLOCK_IMAGE);
        addSolidBlock(550, 150, 100, 24, SOLID_BLOCK_IMAGE);
        liftPlatform = new MovePlatform(700, 300, 100, 24, PLATFORM_COLOR, PLATFORM_IMAGE, MovePlatform.Direction.VERTICAL, 300, 450, 80);
        movePlatforms.add(liftPlatform);
        root.getChildren().add(liftPlatform.getNode());
        addTrap(850, 400, 100, 24, TRAP_COLOR, TRAP_IMAGE);
    }

    @Override
    protected int getPreviousLevelId() {
        return 12;
    }

    @Override
    protected int getNextLevelId() {
        return 14;
    }

    @Override
    protected void onGoalReached() {
        stopLoop();
        switchToLevel(14);
    }

    @Override
    protected void onSolidPlayerOutOfWorld() {
        resetPlayer(false);
    }

    private void createLevelPlayer() {
        player = new entity.Player(60, 420, config.getPlayerWidth(), config.getPlayerHeight(), config.getPlayerColor());
        root.getChildren().add(player.getNode());
        solidPreviousX = player.getX();
    }

    private void resetLevelState() {
        root.getChildren().clear();
        blocks.clear();
        movePlatforms.clear();
        solidBlocks.clear();
        traps.clear();
        activeKeys.clear();

        paused = false;
        finished = false;
        darkWorld = false;
        hasKey = false;
        worldSwitchLocked = false;
        energy = ENERGY_START;
        lastFrame = -1;
        liftPlatform = null;
        activeMovePlatform = null;
    }

    private void configureSceneRoot() {
        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        root.setBackground(new Background(new BackgroundFill(Color.web("#f8fafc"), CornerRadii.EMPTY, Insets.EMPTY)));
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

    private void updateMovePlatforms(double deltaSeconds) {
        for (MovePlatform platform : movePlatforms) {
            platform.update(deltaSeconds);
        }
    }

    private void resolveMovePlatformCollisions() {
        activeMovePlatform = null;

        for (MovePlatform platform : movePlatforms) {
            if (!player.getBounds().intersects(platform.getBounds())) {
                continue;
            }

            double platformTop = platform.getY();
            double platformBottom = platformTop + platform.getHeight();
            double previousTop = player.getPreviousY();
            double previousBottom = previousTop + player.getHeight();
            double playerLeft = player.getX();
            double playerRight = playerLeft + player.getWidth();
            double playerTop = player.getY();
            double playerBottom = playerTop + player.getHeight();
            double platformLeft = platform.getX();
            double platformRight = platformLeft + platform.getWidth();

            if (player.getVelocityY() >= 0 && previousBottom <= platformTop + COLLISION_PADDING) {
                player.landOn(platformTop);
                activeMovePlatform = platform;
                continue;
            }

            if (player.getVelocityY() < 0 && previousTop >= platformBottom - COLLISION_PADDING) {
                dropFromMovePlatform(platform);
                continue;
            }

            double overlapLeft = playerRight - platformLeft;
            double overlapRight = platformRight - playerLeft;
            double overlapTop = playerBottom - platformTop;
            double overlapBottom = platformBottom - playerTop;
            double smallest = Math.min(Math.min(overlapLeft, overlapRight), Math.min(overlapTop, overlapBottom));

            if (smallest == overlapBottom) {
                dropFromMovePlatform(platform);
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

    private void dropFromMovePlatform(MovePlatform platform) {
    	double platformBottom = platform.getY() + platform.getHeight();
        player.hitCeiling(platformBottom);
        if (platform.getDeltaY() > 0) {
            player.moveBy(0, platform.getDeltaY());
        }
    }

    private void buildObjects() {
        door.setPosition(config.getWorldWidth() - 165, 154);
        key.setPosition(505, 241);
        root.getChildren().addAll(door.getNode(), key.getNode());
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

        if (checkTrapCollision()) {
            return;
        }

        solidPreviousX = player.getX();
        player.handleInput(activeKeys, config.getControlConfig(), MOVE_SPEED, JUMP_SPEED);
        player.applyPhysics(deltaSeconds, GRAVITY);
        resolveSolidCollisions();
        resolveMovePlatformCollisions();
        clampSolidPlayer();
        if (checkTrapCollision()) {
            return;
        }
        updateEnergy(deltaSeconds);
        checkKey();
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

    private void updateEnergy(double deltaSeconds) {
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
                return;
            }
        }

        if (energy < 0) {
            energy = 0;
        }
        if (energy > ENERGY_MAX) {
            energy = ENERGY_MAX;
        }
    }

    private void checkKey() {
        boolean keyVisible = darkWorld && !hasKey;
        key.setVisible(keyVisible);

        if (!hasKey && keyVisible && player.getBounds().intersects(key.getBounds())) {
            hasKey = true;
            key.setCollected(true);
        }
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
            block.getNode().setOpacity(darkWorld ? 0.9 : 1.0);
        }

        door.refreshStyle(hasKey, darkWorld);
        door.setPosition(config.getWorldWidth() - 165, 154);

        key.refreshStyle();
        key.setPosition(505, 241);
        key.setVisible(darkWorld && !hasKey);

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
        if (darkWorld || !hasKey) {
            return;
        }

        if (player.getBounds().intersects(door.getBounds())) {
            finished = true;
            onGoalReached();
        }
    }

    private void resetPlayer(boolean loseKey) {
        player.resetToSpawn();
        solidPreviousX = player.getX();
        darkWorld = false;
        worldSwitchLocked = false;
        energy = ENERGY_START;

        if (loseKey) {
            hasKey = false;
        }

        checkKey();
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
