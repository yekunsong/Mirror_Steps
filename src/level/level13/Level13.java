package level.level13;

import config.GameConfig;
import core.AppRouter;
import entity.SolidBlock;
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

    private static final double LIGHT_RADIUS = 130;
    private static final double DOOR_WIDTH = 52;
    private static final double DOOR_HEIGHT = 86;
    private static final double KEY_SIZE = 20;

    private final Rectangle energyTrack = new Rectangle();
    private final Rectangle energyBar = new Rectangle();
    private final Rectangle door = new Rectangle(DOOR_WIDTH, DOOR_HEIGHT);
    private final Rectangle key = new Rectangle(KEY_SIZE, KEY_SIZE);
    private final Label keyLabel = new Label("KEY");
    private final Pane darkLayer = new Pane();

    private AnimationTimer timer;
    private VBox levelPauseMenu;
    private StackPane pauseLayer;
    private boolean paused;
    private boolean finished;
    private boolean darkWorld;
    private boolean hasKey;
    private boolean shiftLocked;
    private double energy;
    private long lastFrame = -1;

    public Level13(GameConfig config, AppRouter router) {
        super(config, router);
    }

    @Override
    public Scene createScene() {
        root.getChildren().clear();
        blocks.clear();
        solidBlocks.clear();
        activeKeys.clear();

        paused = false;
        finished = false;
        darkWorld = false;
        hasKey = false;
        shiftLocked = false;
        energy = ENERGY_START;
        lastFrame = -1;

        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        root.setBackground(new Background(new BackgroundFill(Color.web("#f8fafc"), CornerRadii.EMPTY, Insets.EMPTY)));

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
        double blockW = config.getWorldWidth() / 5;
        double blockH = config.getWorldHeight() / 5;

        addSolidBlock(0, config.getWorldHeight() - blockH, blockW * 5, blockH);
        addSolidBlock(blockW, config.getWorldHeight() - blockH * 2, blockW * 4, blockH);
        addSolidBlock(blockW * 2, config.getWorldHeight() - blockH * 3, blockW * 3, blockH);

        addSolidBlock(0, blockH * 2, blockW * 0.5, blockH);
        addSolidBlock(0, blockH, blockW * 1.5, blockH);
        addSolidBlock(0, 0, blockW * 5, blockH);

        setGoal(config.getWorldWidth() - 115, 154);
        goal.setVisible(false);
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

    private void buildObjects() {
        door.setArcWidth(14);
        door.setArcHeight(14);
        door.setLayoutX(config.getWorldWidth() - 165);
        door.setLayoutY(154);
        door.setStrokeWidth(3);

        key.setArcWidth(10);
        key.setArcHeight(10);
        key.setLayoutX(505);
        key.setLayoutY(241);

        keyLabel.setLayoutX(key.getLayoutX() - 8);
        keyLabel.setLayoutY(key.getLayoutY() - 22);
        keyLabel.setTextFill(Color.web("#fde68a"));
        keyLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        keyLabel.setMouseTransparent(true);

        root.getChildren().addAll(door, key, keyLabel);
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
            if (event.getCode() == KeyCode.SHIFT) {
                shiftLocked = false;
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
        solidPreviousX = player.getX();
        player.handleInput(activeKeys, config.getControlConfig(), MOVE_SPEED, JUMP_SPEED);
        player.applyPhysics(deltaSeconds, GRAVITY);
        resolveSolidCollisions();
        clampSolidPlayer();
        updateEnergy(deltaSeconds);
        checkKey();
        updateView();
        checkDoor();
    }

    private void handleShift() {
        boolean shiftPressed = activeKeys.contains(KeyCode.R);
        if (shiftPressed && !shiftLocked) {
            if (darkWorld) {
                darkWorld = false;
            } else if (energy > MIN_DARK_ENERGY) {
                darkWorld = true;
            }
            shiftLocked = true;
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
        keyLabel.setVisible(keyVisible);

        if (!hasKey && darkWorld && player.getBounds().intersects(key.getBoundsInParent())) {
            hasKey = true;
            key.setVisible(false);
            keyLabel.setVisible(false);
        }
    }

    private void updateView() {
        if (darkWorld) {
            root.setBackground(new Background(new BackgroundFill(Color.web("#0f172a"), CornerRadii.EMPTY, Insets.EMPTY)));
            double centerX = player.getX() + player.getWidth() * 0.5;
            double centerY = player.getY() + player.getHeight() * 0.5;
            darkLayer.getChildren().setAll(createDarkMask(centerX, centerY));
        } else {
            root.setBackground(new Background(new BackgroundFill(Color.web("#f8fafc"), CornerRadii.EMPTY, Insets.EMPTY)));
        }
        darkLayer.setVisible(darkWorld);

        for (SolidBlock block : solidBlocks) {
            block.getNode().setOpacity(darkWorld ? 0.9 : 1.0);
        }

        door.setFill(hasKey ? Color.web("#84cc16") : Color.web("#7c3aed"));
        door.setStroke(darkWorld ? Color.web("#c4b5fd") : Color.web("#ede9fe"));
        door.setOpacity(darkWorld ? 0.28 : 1.0);

        key.setFill(Color.web("#facc15"));
        key.setStroke(Color.web("#fef08a"));

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

        if (player.getBounds().intersects(door.getBoundsInParent())) {
            finished = true;
            onGoalReached();
        }
    }

    private void resetPlayer(boolean loseKey) {
        player.resetToSpawn();
        solidPreviousX = player.getX();
        darkWorld = false;
        shiftLocked = false;
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
            shiftLocked = false;
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

    private Shape createDarkMask(double playerX, double playerY) {
        Rectangle darkBackground = new Rectangle(config.getWorldWidth(), config.getWorldHeight());
        Circle hole = new Circle(playerX, playerY, LIGHT_RADIUS);
        Shape mask = Shape.subtract(darkBackground, hole);
        mask.setFill(Color.rgb(3, 7, 18, 0.88));
        return mask;
    }
}
