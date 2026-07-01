package level.level9;

import config.GameConfig;
import core.AppRouter;
import entity.Player;
import entity.Waterfall;
import javafx.animation.AnimationTimer;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import level.BaseLevel;

/*
 * Level 9 — Waterfalls.
 *
 * The water itself is simulated by the shared entity.Waterfall mechanic: this level
 * only places the solid platforms and a single emitter at the top, and the cascade
 * is derived from the geometry.
 */
public final class Level9 extends BaseLevel {

    //////// CONSTANTS ////////

    private static final double EMITTER_X = 580.0;

    private static final String BACKGROUND_IMAGE = "Pictures/Backgrounds/jungle_background_with_flowers.png";
    private static final String PLATFORM_IMAGE = "Pictures/Platforms/green.png";
    private static final String GOAL_IMAGE = "Pictures/Portal/door2.png";
    private static final String KEY_IMAGE = "Pictures/Key/key1.png";

    //////// FIELDS ////////

    private AnimationTimer timer;
    private VBox pauseMenu;
    private StackPane pauseLayer;
    private boolean paused;
    private long lastFrame = -1;

    private Waterfall waterfall;
    private entity.Key levelKey;
    private boolean keyCollected = false;

    private double pitLeft;
    private double pitRight;

    //////// CONSTRUCTOR ////////

    public Level9(GameConfig config, AppRouter router) {
        super(config, router);
    }

    //////// SCENE CREATION ////////

    @Override
    public Scene createScene() {
        root.getChildren().clear();
        blocks.clear();
        movePlatforms.clear();
        solidBlocks.clear();
        traps.clear();
        activeKeys.clear();

        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        paused = false;

        buildLevel();

        createPauseLayer();

        StackPane container = new StackPane(root, pauseLayer);
        Scene scene = new Scene(container, config.getWorldWidth(), config.getWorldHeight());
        installInput(scene);
        startLoop();
        return scene;
    }

    //////// ABSTRACT OVERRIDES ////////

    @Override
    protected String getLevelTitle() {
        return "Level 9";
    }

    @Override
    protected int getPreviousLevelId() {
        return 8;
    }

    @Override
    protected int getNextLevelId() {
        return 10;
    }

    //////// LEVEL LAYOUT ////////

    @Override
    protected void buildLevel() {
        setBackgroundImage(BACKGROUND_IMAGE);

        // Spawn floor (lower left) and right floor (bottom right).
        // The bottomless pit is the wide gap between them.
        addSolidBlock(0, 660, 300, 60, PLATFORM_IMAGE);
        addSolidBlock(840, 660, config.getWorldWidth() - 840, 60, PLATFORM_IMAGE);

        // Platform 4 (Bottom Left)
        addSolidBlock(280, 520, 470, 24, PLATFORM_IMAGE);

        // Platform 3 (Middle Right)
        addSolidBlock(700, 360, 260, 24, PLATFORM_IMAGE);

        // Platform 2 (Middle Left)
        addSolidBlock(500, 210, 250, 24, PLATFORM_IMAGE);

        // Platform 1 (Top Right)
        addSolidBlock(530, 70, 100, 24, PLATFORM_IMAGE);

        // Goal Platform (Top Right-most)
        addSolidBlock(900, 140, 250, 24, PLATFORM_IMAGE);

        createLevelPlayer(70, 660 - config.getPlayerHeight());

        // Key in bottom right region (above rightmost platform)
        levelKey = new entity.Key(1200, 600, 32, KEY_IMAGE);
        root.getChildren().add(levelKey.getNode());
        keyCollected = false;

        goal = null;

        buildWaterfall();
    }

    private void applyImageToGoal(String imagePath) {
        if (goal == null || imagePath == null)
            return;
        try {
            Image img = new Image(new java.io.File(imagePath).toURI().toString());
            goal.setFill(new ImagePattern(img));
        } catch (Exception e) {
            System.err.println("Failed to load goal image: " + e.getMessage());
        }
    }

    private void buildWaterfall() {
        waterfall = new Waterfall(EMITTER_X, 0, Waterfall.STREAM_WIDTH, config.getWorldWidth(), config.getWorldHeight(),
                solidBlocks);
        root.getChildren().add(waterfall.getNode());
    }

    private void createLevelPlayer(double x, double y) {
        player = new Player(x, y, config.getPlayerWidth(), config.getPlayerHeight(), config.getPlayerColor());
        root.getChildren().add(player.getNode());
        solidPreviousX = player.getX();
    }

    //////// UPDATE LOOP ////////

    private void update(double deltaSeconds) {
        if (paused) {
            return;
        }

        solidPreviousX = player.getX();

        boolean inStream = waterfall.isInStream(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        if (inStream) {
            activeKeys.remove(KeyCode.W);
            activeKeys.remove(KeyCode.UP);
            activeKeys.remove(KeyCode.SPACE);
        }

        double moveSpeed = inStream ? config.getMoveSpeed() * 0.5 : config.getMoveSpeed();
        player.handleInput(activeKeys, config.getControlConfig(), moveSpeed, config.getJumpVelocity());
        player.applyPhysics(deltaSeconds, config.getGravity());

        double push = waterfall.horizontalPush(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        if (push != 0) {
            player.moveBy(push * config.getMoveSpeed() * deltaSeconds, 0);
        }

        resolveSolidCollisions();
        clampSolidPlayer();

        if (!keyCollected && player.getBounds().intersects(levelKey.getBounds())) {
            keyCollected = true;
            levelKey.setCollected(true);
            setGoal(1000, 140 - 72);
            applyImageToGoal(GOAL_IMAGE);
        }

        checkGoalManual();
    }

    private void checkGoalManual() {
        if (goal == null) {
            return;
        }

        Bounds goalBounds = goal.getBoundsInParent();
        if (player.getBounds().intersects(goalBounds)) {
            onGoalReached();
        }
    }

    //////// DEATH / RESPAWN ////////

    @Override
    protected void onSolidPlayerOutOfWorld() {
        player.resetToSpawn();
        solidPreviousX = player.getX();

        keyCollected = false;
        levelKey.setCollected(false);
        if (goal != null) {
            root.getChildren().remove(goal);
            goal = null;
        }
    }

    //////// ANIMATION TIMER ////////

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

    //////// PAUSE / NAVIGATION ////////

    @Override
    protected void switchToLevel(int level) {
        stopLoop();
        super.switchToLevel(level);
    }

    @Override
    protected void switchToMenu() {
        stopLoop();
        super.switchToMenu();
    }

    private void createPauseLayer() {
        Label title = new Label(getLevelTitle());
        title.getStyleClass().add("pause-title");

        Button resumeButton = new Button("Resume");
        resumeButton.getStyleClass().add("secondary-button");
        resumeButton.setOnAction(event -> togglePause(false));

        Button menuButton = new Button("Menu");
        menuButton.getStyleClass().add("secondary-button");
        menuButton.setOnAction(event -> switchToMenu());

        Button previousButton = new Button("Prev");
        previousButton.getStyleClass().add("secondary-button");
        previousButton.setDisable(getPreviousLevelId() == 0);
        previousButton.setOnAction(event -> switchToLevel(getPreviousLevelId()));

        Button nextButton = new Button("Next");
        nextButton.getStyleClass().add("primary-button");
        nextButton.setDisable(getNextLevelId() == 0);
        nextButton.setOnAction(event -> onGoalReached());

        pauseMenu = new VBox(14, title, resumeButton, menuButton, previousButton, nextButton);
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

        scene.setOnKeyReleased(event -> activeKeys.remove(event.getCode()));
    }

    private void togglePause(boolean newState) {
        paused = newState;
        pauseLayer.setVisible(newState);
        pauseLayer.setManaged(newState);
        pauseMenu.setVisible(newState);
        pauseMenu.setManaged(newState);

        if (newState) {
            activeKeys.clear();
        } else {
            lastFrame = -1;
        }
    }
}
