package level.level5;

import config.GameConfig;
import core.AppRouter;
import entity.Player;
import java.lang.reflect.Method;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import level.BaseLevel;

// Level 5: Collapsing bridge prologue to levels 6-8
public final class Level5 extends BaseLevel {

    // === LEVEL CONSTANTS ===

    // Floor layout
    private static final double FLOOR_Y = 380;
    private static final double FLOOR_LEFT_WIDTH = 520;
    private static final double FLOOR_GAP_WIDTH = 500;
    private static final double FLOOR_RIGHT_X = FLOOR_LEFT_WIDTH + FLOOR_GAP_WIDTH;
    private static final double BRIDGE_HEIGHT = 12;
    private static final double CEILING_CLEARANCE = 100;
    private static final double PLAYER_SPAWN_X = 24;

    // Tile size for textures
    private static final double TILE_SIZE = 48;

    // Assets
    private static final String BACKGROUND_IMAGE = "Pictures/Backgrounds/jungle_background_with_flowers.png";
    private static final String FLOOR_TILE_IMAGE = "Pictures/Platforms/gold.png";
    private static final String BRIDGE_TILE_IMAGE = "Pictures/Platforms/blue.png";
    private static final String CEILING_TILE_IMAGE = "Pictures/Platforms/gold.png";
    private static final String GOAL_IMAGE = "Pictures/Portal/door2.png";
    private static final String SPAWN_DOOR_IMAGE = "Pictures/Portal/door1.png";

    // How big to draw the door images (in pixels)
    private static final double DOOR_VISUAL_WIDTH = 60;
    private static final double DOOR_VISUAL_HEIGHT = 75;

    // === STATE ===

    private AnimationTimer timer;
    private Method baseUpdateMethod;
    private VBox pauseMenu;
    private StackPane pauseLayer;
    private boolean paused;
    private long lastFrame = -1;

    // Calculated at runtime so the ceiling sits clear of the player's spawn point
    private double ceilingBottomY;

    // Bridge state (the bridge gets removed once the player walks onto it)
    private Rectangle bridgeBlock;
    private boolean bridgeCollapsed;

    // Invisible trigger sitting below the screen.
    // Player completes the level by falling into the void below the bridge.
    private Rectangle voidGoal;

    // Carry-over from other dark-level template, kept for safety but stays transparent here.
    private final Rectangle lightOverlay = new Rectangle();

    public Level5(GameConfig config, AppRouter router) {
        super(config, router);
    }

    // ============================================================
    // SCENE SETUP
    // ============================================================

    @Override
    public Scene createScene() {
        // Wipe everything from any previous run of this level
        root.getChildren().clear();
        blocks.clear();
        movePlatforms.clear();
        solidBlocks.clear();
        traps.clear();
        activeKeys.clear();

        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        paused = false;

        createPlayer();
        buildLevel();
        configureDarknessLayer();
        createPauseLayer();

        // Move the player node to the top of the render stack
        // so door visuals always sit behind the player
        root.getChildren().remove(player.getNode());
        root.getChildren().add(player.getNode());

        StackPane container = new StackPane(root, pauseLayer);
        Scene scene = new Scene(container, config.getWorldWidth(), config.getWorldHeight());
        installInput(scene);
        startLoop();
        return scene;
    }

    @Override
    protected String getLevelTitle() {
        return "Level 5";
    }

    @Override
    protected int getPreviousLevelId() {
        return 4;
    }

    @Override
    protected int getNextLevelId() {
        return 6;
    }

    @Override
    protected void buildLevel() {
        setBackgroundImage(BACKGROUND_IMAGE);

        // Left floor
        addSolidBlock(0, FLOOR_Y, FLOOR_LEFT_WIDTH, config.getWorldHeight() - FLOOR_Y);
        tileBlock(solidBlocks.get(solidBlocks.size() - 1).getNode(), FLOOR_TILE_IMAGE, TILE_SIZE, 24);

        // Bridge
        bridgeBlock = new Rectangle(FLOOR_GAP_WIDTH, BRIDGE_HEIGHT);
        tileRectangle(bridgeBlock, BRIDGE_TILE_IMAGE, 24, BRIDGE_HEIGHT);
        bridgeBlock.setLayoutX(FLOOR_LEFT_WIDTH);
        bridgeBlock.setLayoutY(FLOOR_Y);
        root.getChildren().add(bridgeBlock);

        // Right floor 
        addSolidBlock(FLOOR_RIGHT_X, FLOOR_Y, config.getWorldWidth() - FLOOR_RIGHT_X, config.getWorldHeight() - FLOOR_Y);
        tileBlock(solidBlocks.get(solidBlocks.size() - 1).getNode(), FLOOR_TILE_IMAGE, TILE_SIZE, 24);

        // Ceiling 
        ceilingBottomY = Math.max(0, player.getY() - CEILING_CLEARANCE);
        addSolidBlock(0, 0, config.getWorldWidth(), ceilingBottomY);
        tileBlock(solidBlocks.get(solidBlocks.size() - 1).getNode(), CEILING_TILE_IMAGE, TILE_SIZE, 24);

        // Visible goal portal on right floor (still functional)
        setGoal(config.getWorldWidth() - 54, FLOOR_Y - 72);
        applyImageToGoal(GOAL_IMAGE, FLOOR_Y);

        // Spawn door, drawn at the player's spawn position on the left floor
        double spawnDoorX = PLAYER_SPAWN_X + config.getPlayerWidth() / 2 - DOOR_VISUAL_WIDTH / 2;
        double spawnDoorY = FLOOR_Y - DOOR_VISUAL_HEIGHT;
        addDoorVisual(spawnDoorX, spawnDoorY, SPAWN_DOOR_IMAGE);

        // Invisible void goal below the screen
        voidGoal = new Rectangle(1000, 10, Color.TRANSPARENT);
        voidGoal.setMouseTransparent(true);
        voidGoal.setLayoutX(FLOOR_LEFT_WIDTH);
        voidGoal.setLayoutY(config.getWorldHeight() + 50);
        root.getChildren().add(voidGoal);
    }

    private void createPlayer() {
        // Spawn on the left floor
        double spawnY = FLOOR_Y - config.getPlayerHeight() - 2;
        player = new Player(PLAYER_SPAWN_X, spawnY, config.getPlayerWidth(), config.getPlayerHeight(), config.getPlayerColor());
        root.getChildren().add(player.getNode());
        solidPreviousX = player.getX();
    }

    @Override
    protected void onSolidPlayerOutOfWorld() {
        player.resetToSpawn();
        solidPreviousX = player.getX();
    }

    // ============================================================
    // IMAGE / TILING HELPERS
    // ============================================================

    // Fills a rectangle with a repeating image pattern.
    // TILE_SIZE controls how big each "tile" is when rendered.
    private void tileRectangle(Rectangle rect, String imagePath, double tileWidth, double tileHeight) {
        try {
            Image tileImage = new Image(new java.io.File(imagePath).toURI().toString());
            ImagePattern pattern = new ImagePattern(
                tileImage,
                0, 0,
                tileWidth, tileHeight,
                false
            );
            rect.setFill(pattern);
        } catch (Exception e) {
            System.err.println("Failed to tile image: " + imagePath);
            System.err.println("Reason: " + e.getMessage());
        }
    }

    // Wrapper that lets us tile any Node that's actually a Rectangle underneath.
    // SolidBlock nodes are Rectangles, so this works on them.
    private void tileBlock(javafx.scene.Node node, String imagePath, double tileWidth, double tileHeight) {
        if (node instanceof Rectangle) {
            tileRectangle((Rectangle) node, imagePath, tileWidth, tileHeight);
        }
    }

    // Places a door visual so its bottom aligns with the given floorY.
    // The hitbox itself is left transparent so the door image isn't squished to fit it.
    private void applyImageToGoal(String imagePath, double floorY) {
        if (goal == null || imagePath == null) {
            return;
        }
        try {
            goal.setFill(Color.TRANSPARENT);

            Image img = new Image(new java.io.File(imagePath).toURI().toString());
            Rectangle doorVisual = new Rectangle(DOOR_VISUAL_WIDTH, DOOR_VISUAL_HEIGHT);
            doorVisual.setFill(new ImagePattern(img));
            doorVisual.setMouseTransparent(true);

            double goalCenterX = goal.getLayoutX() + goal.getWidth() / 2;

            doorVisual.setLayoutX(goalCenterX - DOOR_VISUAL_WIDTH / 2);
            doorVisual.setLayoutY(floorY - DOOR_VISUAL_HEIGHT);

            root.getChildren().add(doorVisual);
        } catch (Exception e) {
            System.err.println("Failed to load goal image: " + imagePath);
            System.err.println("Reason: " + e.getMessage());
        }
    }

    // Places the door image at a specific position.
    // Unlike applyImageToGoal, this doesn't affect the hitbox.
    private void addDoorVisual(double x, double y, String imagePath) {
        if (imagePath == null) {
            return;
        }
        try {
            Image img = new Image(new java.io.File(imagePath).toURI().toString());
            Rectangle door = new Rectangle(DOOR_VISUAL_WIDTH, DOOR_VISUAL_HEIGHT);
            door.setFill(new ImagePattern(img));
            door.setMouseTransparent(true);
            door.setLayoutX(x);
            door.setLayoutY(y);

            root.getChildren().add(door);
        } catch (Exception e) {
            System.err.println("Failed to load door image: " + imagePath);
            System.err.println("Reason: " + e.getMessage());
        }
    }

    // ============================================================
    // INPUT
    // ============================================================

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

    // ============================================================
    // GAME LOOP
    // ============================================================

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
        if (paused) return;

        solidPreviousX = player.getX();
        invokeBaseUpdate(deltaSeconds);
        resolveFloorCollision();
        checkBridgeCollapse();
        checkVoidGoal();
        updateDarknessLayer();
    }

    // Calls BaseLevel's private update method via reflection.
    // If reflection fails for any reason, falls back to a hand-written version.
    private void invokeBaseUpdate(double deltaSeconds) {
        try {
            if (baseUpdateMethod == null) {
                baseUpdateMethod = BaseLevel.class.getDeclaredMethod("update", double.class);
                baseUpdateMethod.setAccessible(true);
            }
            baseUpdateMethod.invoke(this, deltaSeconds);
        } catch (ReflectiveOperationException exception) {
            runFallbackUpdate(deltaSeconds);
        }
    }

    private void runFallbackUpdate(double deltaSeconds) {
        player.handleInput(activeKeys, config.getControlConfig(), config.getMoveSpeed(), config.getJumpVelocity());
        player.applyPhysics(deltaSeconds, config.getGravity());
        resolveSolidCollisions();
        resolveFloorCollision();
        clampSolidPlayer();

        if (goal != null && player.getBounds().intersects(goal.getBoundsInParent())) {
            onGoalReached();
        }
    }

    // ============================================================
    // PLAYER COLLISION
    // ============================================================

    private void resolveFloorCollision() {
        // Build the platform list fresh each frame so the bridge can drop out once it collapses
        Platform[] platforms = new Platform[] {
            // Left floor
            new Platform(0, FLOOR_Y, FLOOR_LEFT_WIDTH, config.getWorldHeight() - FLOOR_Y),
            // Bridge - only present until the player steps onto it
            bridgeBlock != null && !bridgeCollapsed ?
                new Platform(FLOOR_LEFT_WIDTH, FLOOR_Y, FLOOR_GAP_WIDTH, BRIDGE_HEIGHT) : null,
            // Right floor
            new Platform(FLOOR_RIGHT_X, FLOOR_Y, config.getWorldWidth() - FLOOR_RIGHT_X, config.getWorldHeight() - FLOOR_Y),
            // Ceiling
            new Platform(0, 0, config.getWorldWidth(), ceilingBottomY)
        };

        for (Platform platform : platforms) {
            if (platform != null) {
                resolvePlatformCollision(platform);
            }
        }
    }

    // Figures out which side of the platform the player hit by checking which axis has the smallest overlap, then snaps the player out of it.
    private void resolvePlatformCollision(Platform platform) {
        double playerLeft = player.getX();
        double playerRight = player.getX() + player.getWidth();
        double playerTop = player.getY();
        double playerBottom = player.getY() + player.getHeight();

        double platLeft = platform.x;
        double platRight = platform.x + platform.width;
        double platTop = platform.y;
        double platBottom = platform.y + platform.height;

        boolean intersects = playerRight > platLeft && playerLeft < platRight &&
                             playerBottom > platTop && playerTop < platBottom;

        if (!intersects) return;

        double overlapLeft = playerRight - platLeft;
        double overlapRight = platRight - playerLeft;
        double overlapTop = playerBottom - platTop;
        double overlapBottom = platBottom - playerTop;

        double minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
                                      Math.min(overlapTop, overlapBottom));

        if (minOverlap == overlapTop && player.getVelocityY() >= 0) {
            // Landing on top of the platform
            player.landOn(platform.y);
        } else if (minOverlap == overlapBottom && player.getVelocityY() <= 0) {
            // Bumping head on the bottom of a platform
            player.hitCeiling(platBottom);
        } else if (minOverlap == overlapLeft) {
            // Hit the platform's left edge while moving right
            player.getNode().setLayoutX(platLeft - player.getWidth());
        } else if (minOverlap == overlapRight) {
            // Hit the platform's right edge while moving left
            player.getNode().setLayoutX(platRight);
        }
    }

    // ============================================================
    // BRIDGE / VOID GOAL
    // ============================================================

    private void checkBridgeCollapse() {
        if (bridgeCollapsed || bridgeBlock == null) return;

        // The player must step onto the bridge first.
        // The 20px buffer prevents the bridge from collapsing the instant the player edge touches it.
        boolean onBridge =
                player.getBounds().intersects(bridgeBlock.getBoundsInParent()) &&
                (player.getY() + player.getHeight()) <= (FLOOR_Y + BRIDGE_HEIGHT + 2) &&
                player.getX() >= FLOOR_LEFT_WIDTH + 20; // <- buffer

        if (onBridge) {
            bridgeCollapsed = true;
            root.getChildren().remove(bridgeBlock);
        }
    }

    // The real goal is falling into the pit.
    // This trigger sits just below the bottom of the screen.
    private void checkVoidGoal() {
        if (voidGoal != null && player.getBounds().intersects(voidGoal.getBoundsInParent())) {
            onGoalReached();
        }
    }

    // ============================================================
    // DARKNESS / LIGHTING (stub)
    // ============================================================

    // Held over from the dark-level template so the rendering pipeline still has all its layers.
    // Stays fully transparent here since Level 5 is meant to be a daylight level.
    private void configureDarknessLayer() {
        lightOverlay.setWidth(config.getWorldWidth());
        lightOverlay.setHeight(config.getWorldHeight());
        lightOverlay.setMouseTransparent(true);
        lightOverlay.setFill(Color.TRANSPARENT);
        updateDarknessLayer();
        root.getChildren().add(lightOverlay);
    }

    private void updateDarknessLayer() {
        lightOverlay.setFill(Color.TRANSPARENT);
    }

    // ============================================================
    // PAUSE MENU
    // ============================================================

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

    private void togglePause(boolean newState) {
        paused = newState;
        pauseLayer.setVisible(newState);
        pauseLayer.setManaged(newState);
        pauseMenu.setVisible(newState);
        pauseMenu.setManaged(newState);

        if (newState) {
            // Drop all held keys so the player doesn't keep moving after unpause
            activeKeys.clear();
        } else {
            // Reset frame timer so we don't get a massive deltaSeconds spike on first frame back
            lastFrame = -1;
        }
    }

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

    // ============================================================
    // INNER CLASSES
    // ============================================================

    // Data holder for a collision platform.
    // Just position and size.
    private static class Platform {
        double x, y, width, height;
        Platform(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}