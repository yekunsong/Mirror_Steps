package level.level10;

import javafx.animation.AnimationTimer;
import javafx.scene.paint.ImagePattern;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import config.GameConfig;
import entity.SolidBlock;
import entity.Waterfall;
import level.BaseLevel;
import core.AppRouter;
import entity.Player;

/*
 * Level 10 — Waterfall Dungeon.
 *
 * A true implementation of the user's diagram featuring an intricate
 * cascading waterfall loop. The player spawns at the bottom right and must
 * traverse uniformly sized platforms (with two shorter exceptions) up to the
 * top left exit door, fighting the waterfall current along the way.
 */
public final class Level10 extends BaseLevel {

    //////// CONSTANTS ////////

    private static final String BACKGROUND_IMAGE = "Pictures/Backgrounds/brick_background.png";
    private static final String PLATFORM_IMAGE = "Pictures/Platforms/grey_wall.png";
    private static final String GOAL_IMAGE = "Pictures/Portal/door2.png";

    private static final double PLATFORM_TILE_WIDTH = 300.0;
    private static final double PLATFORM_TILE_HEIGHT = 169.0;
    private static final double BACKGROUND_TILE_SIZE = 256.0;

    private static final double FLOOR_Y = 660.0;
    private static final double FLOOR_H = 60.0;

    //////// FIELDS ////////

    private AnimationTimer timer;
    private StackPane pauseLayer;
    private boolean paused;

    private Waterfall waterfall;
    private long lastFrame = -1;

    private final Canvas darknessCanvas = new Canvas();

    //////// CONSTRUCTOR ////////

    public Level10(GameConfig config, AppRouter router) {
        super(config, router);
    }

    //////// ABSTRACT OVERRIDES ////////

    @Override
    protected String getLevelTitle() {
        return "Level 10";
    }

    @Override
    protected int getPreviousLevelId() {
        return 9;
    }

    @Override
    protected int getNextLevelId() {
        return 11;
    }

    //////// LEVEL LAYOUT ////////

    @Override
    protected void buildLevel() {
        setTiledBackground(BACKGROUND_IMAGE, BACKGROUND_TILE_SIZE, 144);

        // Ground floor — 4 uniform segments with 3 gaps (pits)
        addSolidBlockTiled(400, FLOOR_Y, 200, FLOOR_H, PLATFORM_IMAGE);
        addSolidBlockTiled(750, FLOOR_Y, 200, FLOOR_H, PLATFORM_IMAGE);
        addSolidBlockTiled(1100, FLOOR_Y, 120, FLOOR_H, PLATFORM_IMAGE);

        // Top stairs (left to right)
        addSolidBlockTiled(950, 208, 200, 24, PLATFORM_IMAGE);
        addSolidBlockTiled(750, 184, 200, 24, PLATFORM_IMAGE);
        addSolidBlockTiled(550, 160, 200, 24, PLATFORM_IMAGE);
        addSolidBlockTiled(350, 136, 200, 24, PLATFORM_IMAGE);
        addSolidBlockTiled(0, 112, 350, 24, PLATFORM_IMAGE);

        // Lower stairs (right to left)
        addSolidBlockTiled(100, 540, 300, 24, PLATFORM_IMAGE);
        addSolidBlockTiled(400, 420, 200, 24, PLATFORM_IMAGE);
        addSolidBlockTiled(600, 397, 200, 24, PLATFORM_IMAGE);
        addSolidBlockTiled(800, 374, 200, 24, PLATFORM_IMAGE);
        addSolidBlockTiled(1000, 351, 230, 24, PLATFORM_IMAGE);

        createLevelPlayer(1200, FLOOR_Y - config.getPlayerHeight());

        setGoal(50, 40);
        applyImageToGoal(GOAL_IMAGE);

        waterfall = new Waterfall(140, 0, Waterfall.STREAM_WIDTH, config.getWorldWidth(), config.getWorldHeight(),
                solidBlocks);

        root.getChildren().add(waterfall.getNode());
    }

    private void setTiledBackground(String imagePath, double tileWidth, double tileHeight) {
        try {
            Image tileImage = new Image(new java.io.File(imagePath).toURI().toString());
            ImagePattern pattern = new ImagePattern(
                    tileImage,
                    0, 0,
                    tileWidth, tileHeight,
                    false);

            Rectangle bgRect = new Rectangle(config.getWorldWidth(), config.getWorldHeight());
            bgRect.setFill(pattern);
            bgRect.setMouseTransparent(true);
            root.getChildren().add(0, bgRect);

        } catch (Exception e) {
            System.err.println("Failed to load tiled background: " + imagePath);
            System.err.println("Reason: " + e.getMessage());
        }
    }

    private void addSolidBlockTiled(double x, double y, double width, double height, String imagePath) {
        SolidBlock block = new SolidBlock(x, y, width, height, config.getBlockColor());

        try {
            Image tileImage = new Image(new java.io.File(imagePath).toURI().toString());
            ImagePattern pattern = new ImagePattern(
                    tileImage,
                    0, 0,
                    PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT,
                    false);
            ((Rectangle) block.getNode()).setFill(pattern);

        } catch (Exception e) {
            System.err.println("Failed to load tiled texture: " + e.getMessage());
        }

        solidBlocks.add(block);
        root.getChildren().add(block.getNode());
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

    private void createLevelPlayer(double x, double y) {
        player = new Player(x, y, config.getPlayerWidth(), config.getPlayerHeight(), config.getPlayerColor());
        root.getChildren().add(player.getNode());
        solidPreviousX = player.getX();
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
        configureDarknessLayer();
        createPauseLayer();

        StackPane container = new StackPane(root, pauseLayer);
        Scene scene = new Scene(container, config.getWorldWidth(), config.getWorldHeight());
        installInput(scene);
        startLoop();

        return scene;
    }

    //////// UPDATE LOOP ////////

    private void update(double deltaSeconds) {
        if (paused)
            return;

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

        double push1 = waterfall.horizontalPush(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        double push = Math.max(-1, Math.min(1, push1));

        if (push != 0) {
            player.moveBy(push * config.getMoveSpeed() * deltaSeconds, 0);
        }

        resolveSolidCollisions();
        clampSolidPlayer();
        checkGoalManual();
        updateDarknessLayer();
    }

    private void checkGoalManual() {
        if (goal == null)
            return;

        javafx.geometry.Bounds goalBounds = goal.getBoundsInParent();

        if (player.getBounds().intersects(goalBounds))
            onGoalReached();
    }

    //////// DARKNESS LAYER ////////

    private void configureDarknessLayer() {
        darknessCanvas.setWidth(config.getWorldWidth());
        darknessCanvas.setHeight(config.getWorldHeight());
        darknessCanvas.setMouseTransparent(true);
        root.getChildren().add(darknessCanvas);
        updateDarknessLayer();
    }

    private void updateDarknessLayer() {
        double width = darknessCanvas.getWidth();
        double height = darknessCanvas.getHeight();

        GraphicsContext gc = darknessCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        if (player == null)
            return;

        double centerX = player.getX() + player.getWidth() * 0.5;
        double centerY = player.getY() + player.getHeight() * 0.5;

        RadialGradient gradient = new RadialGradient(
                0, 0, centerX, centerY, 300, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(0, 0, 0, 0.0)),
                new Stop(0.45, Color.rgb(0, 0, 0, 0.0)),
                new Stop(0.62, Color.rgb(0, 0, 0, 0.55)),
                new Stop(1.0, Color.rgb(0, 0, 0, 1)));

        gc.setFill(gradient);
        gc.fillRect(0, 0, width, height);
    }

    //////// DEATH / RESPAWN ////////

    @Override
    protected void onSolidPlayerOutOfWorld() {
        player.resetToSpawn();
        solidPreviousX = player.getX();
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
        if (timer != null)
            timer.stop();
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
        pauseMenu = createStandardPauseMenu(
            () -> togglePause(false),
            this::switchToMenu,
            () -> switchToLevel(getPreviousLevelId()),
            this::onGoalReached,
            getPreviousLevelId() == 0,
            getNextLevelId() == 0
        );
        pauseMenu.setVisible(false);
        pauseMenu.setManaged(false);

        pauseLayer = createPauseOverlay(pauseMenu);
    }

    private void installInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                togglePause(!paused);
                return;
            }

            if (!paused)
                activeKeys.add(event.getCode());
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
