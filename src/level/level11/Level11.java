package level.level11;

import java.util.ArrayList;
import java.util.List;

import config.GameConfig;
import core.AppRouter;
import entity.Player;
import entity.Portal;
import entity.SolidBlock;
import entity.Waterfall;
import javafx.animation.AnimationTimer;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.shape.Rectangle;
import level.BaseLevel;

/*
 * Level 11 — The Portal Waterfall Chamber.
 *
 * Implements a complex visual layout with an inner chamber, dual waterfalls,
 * and teleporting portals (blue and red pairs), complete with the darkness layer
 * from Level 10.
 */
public final class Level11 extends BaseLevel {

    //////// CONSTANTS ////////

    private static final String BACKGROUND_IMAGE = "Pictures/Backgrounds/brick_background.png";
    private static final String PLATFORM_IMAGE = "Pictures/Platforms/grey_wall.png";
    private static final String GOAL_IMAGE = "Pictures/Portal/door2.png";
    private static final String PORTAL_BLUE_IMAGE = "Pictures/Portal/portal_left.png";
    private static final String PORTAL_RED_IMAGE = "Pictures/Portal/portal_right.png";

    private static final double PLATFORM_TILE_WIDTH = 300.0;
    private static final double PLATFORM_TILE_HEIGHT = 169.0;
    private static final double BACKGROUND_TILE_SIZE = 256.0;

    private static final String IMPOSTOR_FACING_RIGHT_IMAGE = "Pictures/Character/angel_right.png";
    private static final String IMPOSTOR_FACING_LEFT_IMAGE = "Pictures/Character/angel_left.png";

    private static final double DEATH_DURATION = 1.0;

    //////// FIELDS ////////

    private final List<Portal> portals = new ArrayList<>();
    private final List<Impostor> impostors = new ArrayList<>();

    private boolean playerFacingRight = true;

    private AnimationTimer timer;
    private StackPane pauseLayer;
    private VBox pauseMenu;
    private boolean paused;

    private final List<Waterfall> waterfalls = new ArrayList<>();
    private long lastFrame = -1;

    private boolean isDying = false;
    private double deathTimer = 0;
    private final Rectangle deathOverlay = new Rectangle();
    private final Canvas darknessCanvas = new Canvas();

    //////// CONSTRUCTOR ////////

    public Level11(GameConfig config, AppRouter router) {
        super(config, router);
    }

    //////// ABSTRACT OVERRIDES ////////

    @Override
    protected String getLevelTitle() {
        return "Level 11";
    }

    @Override
    protected int getPreviousLevelId() {
        return 10;
    }

    @Override
    protected int getNextLevelId() {
        return 12;
    }

    //////// LEVEL LAYOUT ////////

    @Override
    protected void buildLevel() {
        setTiledBackground(BACKGROUND_IMAGE, BACKGROUND_TILE_SIZE, 144);

        // Ground floor
        addSolidBlockTiled(0, 660, config.getWorldWidth(), 60, PLATFORM_IMAGE);

        // Chamber top and bottom walls
        addSolidBlockTiled(200, 200, 880, 50, PLATFORM_IMAGE);
        addSolidBlockTiled(200, 410, 880, 50, PLATFORM_IMAGE);

        // Chamber side walls
        addSolidBlockTiled(200, 250, 50, 160, PLATFORM_IMAGE);
        addSolidBlockTiled(1030, 250, 50, 160, PLATFORM_IMAGE);

        createLevelPlayer(150, 660 - config.getPlayerHeight());

        setGoal(1100, 50);
        applyImageToGoal(GOAL_IMAGE);

        buildPortals();
        buildWaterfalls();
        buildImpostors();
    }

    private void buildPortals() {
        // Red portal directly above the outer part of the inner chamber, centered
        Portal portalRedTop = new Portal(616, 0, 48, 64, PORTAL_RED_IMAGE);

        // Red portal at the bottom right of the inner part of the inner chamber
        Portal portalRedBottomRight = new Portal(972, 336, 48, 64, PORTAL_RED_IMAGE);

        // Blue portal at the bottom left of the inner part of the inner chamber
        Portal portalBlueBottomLeft = new Portal(260, 336, 48, 64, PORTAL_BLUE_IMAGE);

        // Blue portal directly above the bottommost stone platform touching the bottom
        // screen edge
        Portal portalBlueBottomCenter = new Portal(616, 596, 48, 64, PORTAL_BLUE_IMAGE);

        ColorAdjust redTint = new ColorAdjust();
        redTint.setHue(0.7);
        portalRedTop.getNode().setEffect(redTint);
        portalRedBottomRight.getNode().setEffect(redTint);

        portalRedTop.linkTo(portalRedBottomRight);
        portalRedBottomRight.linkTo(portalRedTop);
        portalBlueBottomLeft.linkTo(portalBlueBottomCenter);
        portalBlueBottomCenter.linkTo(portalBlueBottomLeft);

        portals.add(portalRedTop);
        portals.add(portalRedBottomRight);
        portals.add(portalBlueBottomLeft);
        portals.add(portalBlueBottomCenter);

        root.getChildren().add(portalRedTop.getNode());
        root.getChildren().add(portalRedBottomRight.getNode());
        root.getChildren().add(portalBlueBottomLeft.getNode());
        root.getChildren().add(portalBlueBottomCenter.getNode());
    }

    private void buildWaterfalls() {
        waterfalls.clear();

        List<double[]> sources = List.of(
                new double[] { 32, 0 },
                new double[] { 1248, 0 },
                new double[] { 282, 250 });
        waterfalls.add(new Waterfall(sources, 64, config.getWorldWidth(), config.getWorldHeight(), solidBlocks));

        for (Waterfall waterfall : waterfalls) {
            root.getChildren().add(waterfall.getNode());
        }
    }

    private void buildImpostors() {
        impostors.clear();
        double w = config.getPlayerWidth();
        double h = config.getPlayerHeight();

        double columnWidth = config.getWorldWidth() / 8.0;
        double y = 200 - h;

        for (int column : new int[] { 2, 3, 6, 7 }) {
            double x = (column - 0.5) * columnWidth - w / 2.0;
            addImpostor(x, y, w, h);
        }
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
        if (goal == null || imagePath == null) {
            return;
        }

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

    //////// VISION ////////

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

        if (player == null) {
            return;
        }

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

    //////// SCENE CREATION ////////

    @Override
    public Scene createScene() {
        root.getChildren().clear();
        blocks.clear();
        movePlatforms.clear();
        solidBlocks.clear();
        traps.clear();
        activeKeys.clear();
        portals.clear();
        impostors.clear();

        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        paused = false;
        isDying = false;
        deathTimer = 0;

        deathOverlay.setWidth(config.getWorldWidth());
        deathOverlay.setHeight(config.getWorldHeight());
        deathOverlay.setFill(Color.RED);
        deathOverlay.setOpacity(0);
        deathOverlay.setMouseTransparent(true);

        buildLevel();
        configureDarknessLayer();

        // Re-add on top so the death flash always renders above the darkness layer
        root.getChildren().remove(deathOverlay);
        root.getChildren().add(deathOverlay);

        createPauseLayer();

        StackPane container = new StackPane(root, pauseLayer);
        Scene scene = new Scene(container, config.getWorldWidth(), config.getWorldHeight());

        installInput(scene);
        startLoop();

        return scene;
    }

    //////// UPDATE LOOP ////////

    private void update(double deltaSeconds) {
        if (paused) {
            return;
        }

        if (isDying) {
            deathTimer += deltaSeconds;
            deathOverlay.setOpacity(Math.min(1.0, deathTimer / DEATH_DURATION));

            if (deathTimer >= DEATH_DURATION) {
                resetLevel();
            }
            return;
        }

        solidPreviousX = player.getX();

        boolean inStream = false;
        for (Waterfall waterfall : waterfalls) {
            if (waterfall.isInStream(player.getX(), player.getY(), player.getWidth(), player.getHeight())) {
                inStream = true;
                break;
            }
        }

        if (inStream) {
            activeKeys.remove(KeyCode.W);
            activeKeys.remove(KeyCode.UP);
            activeKeys.remove(KeyCode.SPACE);
        }

        double moveSpeed = inStream ? config.getMoveSpeed() * 0.5 : config.getMoveSpeed();

        player.handleInput(activeKeys, config.getControlConfig(), moveSpeed, config.getJumpVelocity());
        player.applyPhysics(deltaSeconds, config.getGravity());

        double pushSum = 0;
        for (Waterfall waterfall : waterfalls) {
            pushSum += waterfall.horizontalPush(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        }
        double push = Math.max(-1, Math.min(1, pushSum));

        if (push != 0) {
            player.moveBy(push * config.getMoveSpeed() * deltaSeconds, 0);
        }

        resolveSolidCollisions();
        clampSolidPlayer();
        handlePortals(deltaSeconds);
        checkGoalManual();
        updateImpostors(deltaSeconds);
        updateDarknessLayer();
    }

    private void handlePortals(double deltaSeconds) {
        for (Portal portal : portals) {
            portal.tickCooldown(deltaSeconds);

            if (!portal.isOnCooldown()) {
                boolean teleported = false;
                if (player.getBounds().intersects(portal.getBounds())) {
                    Portal linked = portal.getLinkedPortal();

                    if (linked != null) {
                        player.setPosition(linked.getExitX(player.getWidth()), linked.getExitY(player.getHeight()));
                        portal.startCooldown();
                        linked.startCooldown();
                        teleported = true;
                    }
                }

                if (!teleported) {
                    for (Impostor imp : impostors) {
                        if (imp.isActive && imp.node.getBoundsInParent().intersects(portal.getBounds())) {
                            Portal linked = portal.getLinkedPortal();
                            if (linked != null) {
                                imp.x = linked.getExitX(imp.width);
                                imp.y = linked.getExitY(imp.height);
                                imp.node.setLayoutX(imp.x);
                                imp.node.setLayoutY(imp.y);
                                portal.startCooldown();
                                linked.startCooldown();
                                break;
                            }
                        }
                    }
                }
            }
        }
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
        if (isDying) {
            return;
        }

        isDying = true;
        deathTimer = 0;
        deathOverlay.setOpacity(0);
    }

    private void resetLevel() {
        isDying = false;
        deathOverlay.setOpacity(0);
        player.resetToSpawn();
        solidPreviousX = player.getX();

        for (Impostor imp : impostors) {
            imp.reset();
            updateImpostorSprite(imp);
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

            if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) {
                playerFacingRight = true;
            } else if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) {
                playerFacingRight = false;
            }

            if (!paused) {
                activeKeys.add(event.getCode());
            }
        });

        scene.setOnKeyReleased(event -> {
            activeKeys.remove(event.getCode());

            if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) {
                if (activeKeys.contains(KeyCode.RIGHT) || activeKeys.contains(KeyCode.D)) {
                    playerFacingRight = true;
                }
            } else if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) {
                if (activeKeys.contains(KeyCode.LEFT) || activeKeys.contains(KeyCode.A)) {
                    playerFacingRight = false;
                }
            }
        });
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

    //////// IMPOSTORS BEHAVIOR ////////

    private void addImpostor(double x, double y, double w, double h) {
        Impostor imp = new Impostor(x, y, w, h);
        applyImageToImpostor(imp, IMPOSTOR_FACING_RIGHT_IMAGE, IMPOSTOR_FACING_LEFT_IMAGE);
        impostors.add(imp);
        root.getChildren().add(imp.node);
    }

    private void applyImageToImpostor(Impostor imp, String rightImg, String leftImg) {
        if (rightImg == null || leftImg == null)
            return;
        imp.rightImagePath = rightImg;
        imp.leftImagePath = leftImg;
        imp.node.setArcWidth(0);
        imp.node.setArcHeight(0);
        updateImpostorSprite(imp);
    }

    private void updateImpostorSprite(Impostor imp) {
        if (imp.rightImagePath == null || imp.leftImagePath == null)
            return;
        String path = imp.facingRight ? imp.rightImagePath : imp.leftImagePath;
        try {
            Image img = new Image(new java.io.File(path).toURI().toString());
            imp.node.setFill(new ImagePattern(img));
        } catch (Exception e) {
            System.err.println("Failed to load impostor image: " + path);
        }
    }

    private void updateImpostors(double deltaSeconds) {
        double px = player.getX();

        for (Impostor imp : impostors) {
            if (!imp.isActive) {
                if (Math.abs(px - imp.x) < 800) {
                    imp.isActive = true;
                }
            }

            if (imp.isActive) {
                double pushSum = 0;
                for (Waterfall waterfall : waterfalls) {
                    pushSum += waterfall.horizontalPush(imp.x, imp.y, imp.width, imp.height);
                }
                double push = Math.max(-1, Math.min(1, pushSum));

                if (!isImpostorLookedAt(imp) && imp.onGround) {
                    boolean wantsToFaceRight = px > imp.x;
                    if (wantsToFaceRight != imp.facingRight) {
                        imp.facingRight = wantsToFaceRight;
                        updateImpostorSprite(imp);
                    }

                    int direction = imp.facingRight ? 1 : -1;
                    double walkSpeed = direction * push > 0 ? imp.speed * 2 : imp.speed;

                    imp.x += direction * walkSpeed * deltaSeconds;
                } else if (push != 0) {
                    imp.x += push * imp.speed * deltaSeconds;
                }

                resolveImpostorPhysics(imp, deltaSeconds);

                if (player.getBounds().intersects(imp.node.getBoundsInParent())) {
                    onSolidPlayerOutOfWorld();
                }
            }
        }
    }

    private boolean isImpostorLookedAt(Impostor imp) {
        double px = player.getX() + player.getWidth() / 2;
        double ax = imp.x + imp.width / 2;

        if (playerFacingRight && ax >= px)
            return true;
        if (!playerFacingRight && ax <= px)
            return true;

        return false;
    }

    private void resolveImpostorPhysics(Impostor imp, double deltaSeconds) {
        imp.velocityY += config.getGravity() * deltaSeconds;
        imp.y += imp.velocityY * deltaSeconds;

        imp.onGround = false;

        for (SolidBlock block : solidBlocks) {
            double iLeft = imp.x;
            double iRight = imp.x + imp.width;
            double iTop = imp.y;
            double iBottom = imp.y + imp.height;

            double bLeft = block.getX();
            double bRight = block.getX() + block.getWidth();
            double bTop = block.getY();
            double bBottom = block.getY() + block.getHeight();

            if (iRight > bLeft && iLeft < bRight && iBottom > bTop && iTop < bBottom) {
                double overlapTop = iBottom - bTop;
                double overlapBottom = bBottom - iTop;
                double overlapLeft = iRight - bLeft;
                double overlapRight = bRight - iLeft;

                double minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
                        Math.min(overlapTop, overlapBottom));

                if (minOverlap == overlapTop && imp.velocityY >= 0) {
                    imp.y = bTop - imp.height;
                    imp.velocityY = 0;
                    imp.onGround = true;
                } else if (minOverlap == overlapBottom && imp.velocityY <= 0) {
                    imp.y = bBottom;
                    imp.velocityY = 0;
                } else if (minOverlap == overlapLeft) {
                    imp.x = bLeft - imp.width;
                } else if (minOverlap == overlapRight) {
                    imp.x = bRight;
                }
            }
        }

        // Clamp impostors to screen borders
        if (imp.x < 0) {
            imp.x = 0;
        } else if (imp.x > config.getWorldWidth() - imp.width) {
            imp.x = config.getWorldWidth() - imp.width;
        }

        imp.node.setLayoutX(imp.x);
        imp.node.setLayoutY(imp.y);
    }

    private static class Impostor {
        Rectangle node;
        double x, y;
        double startX, startY;
        double velocityY = 0;
        boolean isActive = false;
        boolean onGround = false;
        double speed = 110.0;
        double width, height;

        boolean facingRight = true;
        String rightImagePath;
        String leftImagePath;

        Impostor(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.startX = x;
            this.startY = y;
            this.width = width;
            this.height = height;
            this.node = new Rectangle(width, height, Color.RED);
            this.node.setLayoutX(x);
            this.node.setLayoutY(y);
        }

        void reset() {
            this.x = startX;
            this.y = startY;
            this.velocityY = 0;
            this.isActive = false;
            this.onGround = false;
            this.facingRight = true;
            this.node.setLayoutX(x);
            this.node.setLayoutY(y);
        }
    }

}
