package level.level3;

import config.GameConfig;
import core.AppRouter;
import entity.Block;
import entity.Player;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import level.BaseLevel;

/*
 * Level 3 uses its own scene loop because this level needs extra rules:
 * - Light World charges energy
 * - Dark World consumes energy and shows a light around the player
 * - the player must find a key in Dark World and open the door in Light World
 */
public final class Level3 extends BaseLevel {

    private static final double FLOOR_HEIGHT = 40;
    private static final double PLAYER_SPAWN_X = 60;
    private static final double PLAYER_SPAWN_Y = 420;
    private static final double ENERGY_MAX = 100;
    private static final double ENERGY_START = 50;
    private static final double ENERGY_DANGER_THRESHOLD = 92;
    private static final double ENERGY_LIGHT_CHARGE_RATE = 17;
    private static final double ENERGY_DARK_DRAIN_RATE = 15;
    private static final double ENERGY_EMPTY_RETURN = 8;
    private static final double LIGHT_RADIUS = 130;
    private static final double LEVEL_MOVE_SPEED = 300;
    private static final double LEVEL_JUMP_SPEED = -440;
    private static final double LEVEL_GRAVITY = 900;
    private static final double DOOR_WIDTH = 52;
    private static final double DOOR_HEIGHT = 86;
    private static final double KEY_SIZE = 20;

    private final Rectangle energyTrack = new Rectangle();
    private final Rectangle energyBar = new Rectangle();
    private final Rectangle door = new Rectangle(DOOR_WIDTH, DOOR_HEIGHT);
    private final Rectangle key = new Rectangle(KEY_SIZE, KEY_SIZE);
    private final Label keyLabel = new Label("KEY");
    private final Pane darkLayer = new Pane();

    private Player playerInLevel3;
    private AnimationTimer timer;
    private VBox pauseMenu;
    private StackPane pauseLayer;
    private boolean paused;
    private boolean finished;
    private boolean darkWorld;
    private boolean hasKey;
    private boolean shiftPressedLastFrame;
    private double energy = ENERGY_START;
    private long lastFrame = -1;

    public Level3(GameConfig config, AppRouter router) {
        super(config, router);
    }

    @Override
    public Scene createScene() {
        root.getChildren().clear();
        blocks.clear();
        activeKeys.clear();

        paused = false;
        finished = false;
        darkWorld = false;
        hasKey = false;
        shiftPressedLastFrame = false;
        energy = ENERGY_START;
        lastFrame = -1;

        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        root.setBackground(new Background(new BackgroundFill(Color.web("#f8fafc"), CornerRadii.EMPTY, Insets.EMPTY)));

        createPlayer();
        buildLevel();
        buildWorldObjects();
        createEnergyBar();
        createPauseLayer();
        updateView();

        StackPane container = new StackPane(root, darkLayer, pauseLayer);
        StackPane.setAlignment(pauseMenu, Pos.CENTER);

        Scene scene = new Scene(container, config.getWorldWidth(), config.getWorldHeight());
        installInput(scene);
        startLoop();
        return scene;
    }

    @Override
    protected String getLevelTitle() {
        return "Level 3";
    }

    @Override
    protected void buildLevel() {
        addBlock(0, config.getWorldHeight() - FLOOR_HEIGHT, 220, FLOOR_HEIGHT);
        addBlock(300, config.getWorldHeight() - FLOOR_HEIGHT, 240, FLOOR_HEIGHT);
        addBlock(620, config.getWorldHeight() - FLOOR_HEIGHT, config.getWorldWidth() - 620, FLOOR_HEIGHT);
        addBlock(180, 360, 150, 24);
        addBlock(520, 280, 160, 24);
        addBlock(780, 220, 110, 24);
        setGoal(config.getWorldWidth() - 110, 164);
        goal.setVisible(false);
    }

    @Override
    protected int getPreviousLevelId() {
        return 2;
    }

    @Override
    protected int getNextLevelId() {
        return -1;
    }

    @Override
    protected String getNextButtonText() {
        return "Finish";
    }

    @Override
    protected void onGoalReached() {
        if (!finished) {
            return;
        }
        stopLoop();
        switchToMenu();
    }

    private void createPlayer() {
        playerInLevel3 = new Player(
            PLAYER_SPAWN_X,
            PLAYER_SPAWN_Y,
            config.getPlayerWidth(),
            config.getPlayerHeight(),
            config.getPlayerColor()
        );
        player = playerInLevel3;
        root.getChildren().add(playerInLevel3.getNode());
    }

    private void buildWorldObjects() {
        door.setArcWidth(14);
        door.setArcHeight(14);
        door.setLayoutX(config.getWorldWidth() - 94);
        door.setLayoutY(122);
        door.setStrokeWidth(3);

        key.setArcWidth(10);
        key.setArcHeight(10);
        key.setLayoutX(560);
        key.setLayoutY(246);

        keyLabel.setLayoutX(key.getLayoutX() - 8);
        keyLabel.setLayoutY(key.getLayoutY() - 22);
        keyLabel.setTextFill(Color.web("#fde68a"));
        keyLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        keyLabel.setMouseTransparent(true);

        root.getChildren().addAll(door, key, keyLabel);
    }

    private void createEnergyBar() {
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

        Shape firstMask = createDarkMask(
            PLAYER_SPAWN_X + config.getPlayerWidth() * 0.5,
            PLAYER_SPAWN_Y + config.getPlayerHeight() * 0.5
        );
        darkLayer.getChildren().setAll(firstMask);
        darkLayer.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        darkLayer.setMouseTransparent(true);

        root.getChildren().addAll(energyTrack, energyBar);
    }

    private void createPauseLayer() {
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

        Button finishButton = new Button(getNextButtonText());
        finishButton.getStyleClass().add("primary-button");
        finishButton.setDisable(true);

        pauseMenu = new VBox(14, title, resumeButton, menuButton, previousButton, finishButton);
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setPadding(new Insets(28));
        pauseMenu.setMaxWidth(260);
        pauseMenu.getStyleClass().add("pause-panel");
        pauseMenu.setVisible(false);
        pauseMenu.setManaged(false);

        pauseLayer = new StackPane(pauseMenu);
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
            if (event.getCode() == KeyCode.SHIFT) {
                shiftPressedLastFrame = false;
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

        handleShift();
        playerInLevel3.handleInput(activeKeys, config.getControlConfig(), LEVEL_MOVE_SPEED, LEVEL_JUMP_SPEED);
        playerInLevel3.applyPhysics(deltaSeconds, LEVEL_GRAVITY);
        resolveCollisions();
        clampPlayer();
        updateEnergy(deltaSeconds);
        checkKey();
        updateView();
        checkDoor();
    }

    private void handleShift() {
        boolean shiftPressed = activeKeys.contains(KeyCode.SHIFT);
        if (shiftPressed && !shiftPressedLastFrame) {
            if (darkWorld) {
                darkWorld = false;
            } else if (energy > ENERGY_EMPTY_RETURN) {
                darkWorld = true;
            }
            shiftPressedLastFrame = true;
        }
    }

    private void resolveCollisions() {
        playerInLevel3.setOnGround(false);

        for (Block block : blocks) {
            if (!playerInLevel3.getBounds().intersects(block.getBounds())) {
                continue;
            }

            double previousBottom = playerInLevel3.getPreviousY() + playerInLevel3.getHeight();
            if (playerInLevel3.getVelocityY() >= 0 && previousBottom <= block.getY() + 16) {
                playerInLevel3.landOn(block.getY());
            }
        }
    }

    private void clampPlayer() {
        if (playerInLevel3.getX() < 0) {
            playerInLevel3.setPosition(0, playerInLevel3.getY());
        }

        if (playerInLevel3.getX() > config.getWorldWidth() - playerInLevel3.getWidth()) {
            playerInLevel3.setPosition(config.getWorldWidth() - playerInLevel3.getWidth(), playerInLevel3.getY());
        }

        if (playerInLevel3.getY() > config.getWorldHeight() + 100) {
            resetPlayer(false);
        }
    }

    private void updateEnergy(double deltaSeconds) {
        if (darkWorld) {
            energy -= ENERGY_DARK_DRAIN_RATE * deltaSeconds;
            if (energy <= 0) {
                energy = ENERGY_EMPTY_RETURN;
                darkWorld = false;
            }
        } else {
            energy += ENERGY_LIGHT_CHARGE_RATE * deltaSeconds;
            if (energy >= ENERGY_DANGER_THRESHOLD) {
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
        keyLabel.setVisible(keyVisible);

        if (!hasKey && darkWorld && playerInLevel3.getBounds().intersects(key.getBoundsInParent())) {
            hasKey = true;
            key.setVisible(false);
            keyLabel.setVisible(false);
        }
    }

    private void updateView() {
        Color background = darkWorld ? Color.web("#0f172a") : Color.web("#f8fafc");
        root.setBackground(new Background(new BackgroundFill(background, CornerRadii.EMPTY, Insets.EMPTY)));

        darkLayer.setVisible(darkWorld);
        if (darkWorld) {
            double centerX = playerInLevel3.getX() + playerInLevel3.getWidth() * 0.5;
            double centerY = playerInLevel3.getY() + playerInLevel3.getHeight() * 0.5;
            darkLayer.getChildren().setAll(createDarkMask(centerX, centerY));
        }

        for (Block block : blocks) {
            block.getNode().setOpacity(darkWorld ? 0.9 : 1.0);
        }

        door.setFill(hasKey ? Color.web("#84cc16") : Color.web("#7c3aed"));
        door.setStroke(darkWorld ? Color.web("#c4b5fd") : Color.web("#ede9fe"));
        door.setOpacity(darkWorld ? 0.28 : 1.0);

        key.setFill(Color.web("#facc15"));
        key.setStroke(Color.web("#fef08a"));
        key.setOpacity(1.0);

        double fillHeight = 74 * (energy / ENERGY_MAX);
        double barX = playerInLevel3.getX() + playerInLevel3.getWidth() + 8;
        double barY = playerInLevel3.getY() - 4;
        energyTrack.setLayoutX(barX);
        energyTrack.setLayoutY(barY);
        energyBar.setHeight(fillHeight * (54.0 / 74.0));
        energyBar.setLayoutX(barX + 2);
        energyBar.setLayoutY(barY + 2 + (50 - energyBar.getHeight()));
        energyBar.setFill(getEnergyColor());
        energyTrack.setVisible(true);
        energyBar.setVisible(true);
    }

    private void checkDoor() {
        if (darkWorld || !hasKey) {
            return;
        }

        if (playerInLevel3.getBounds().intersects(door.getBoundsInParent())) {
            finished = true;
            stopLoop();
            switchToMenu();
        }
    }

    private void resetPlayer(boolean loseKey) {
        playerInLevel3.resetToSpawn();
        energy = ENERGY_START;
        darkWorld = false;
        shiftPressedLastFrame = false;

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
        pauseMenu.setVisible(newState);
        pauseMenu.setManaged(newState);

        if (newState) {
            activeKeys.clear();
            
            shiftPressedLastFrame = false;
        } else {
            lastFrame = -1;
        }
    }

    private Color getEnergyColor() {
        if (energy >= ENERGY_DANGER_THRESHOLD - 8) {
            return Color.web("#ef4444");
        }
        if (energy >= 60) {
            return Color.web("#f59e0b");
        }
        return Color.web("#22c55e");
    }

    private Shape createDarkMask(double centerX, double centerY) {
        Rectangle darkBackground = new Rectangle(config.getWorldWidth(), config.getWorldHeight());
        Circle lightHole = new Circle(centerX, centerY, LIGHT_RADIUS);
        Shape mask = Shape.subtract(darkBackground, lightHole);
        mask.setFill(Color.rgb(3, 7, 18, 0.88));
        return mask;
    }
}
