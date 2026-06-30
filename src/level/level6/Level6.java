package level.level6;

import config.GameConfig;
import core.AppRouter;
import entity.Player;
import java.lang.reflect.Method;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Rectangle;
import level.BaseLevel;

// Level 6: Flashlight pickup introduction
public final class Level6 extends BaseLevel {

    // === LEVEL CONSTANTS ===

    // Floor layout
    private static final double FLOOR_Y = 680;
    private static final double FLOOR_HEIGHT = 80;
    private static final double PLAYER_SPAWN_X = 24;

    // Flashlight visuals
    private static final double LIGHT_RADIUS = 170;
    private static final double LIGHT_EDGE_START = 0.62;

    // Tile sizes for textures
    private static final double TILE_SIZE = 48;
    private static final double BACKGROUND_TILE_SIZE = 256;

    // Key pickup size
    private static final double KEY_SIZE = 25;

    // Assets
    private static final String BACKGROUND_IMAGE = "Pictures/Backgrounds/brick_background.png";
    private static final String FLOOR_TILE_IMAGE = "Pictures/Platforms/blue.png";
    private static final String GOAL_IMAGE = "Pictures/Portal/door2.png";
    private static final String KEY_IMAGE = "Pictures/Key/flashlight2.png"; // Set when image ready
    //private static final String SPAWN_DOOR_IMAGE = "Pictures/Portal/door1.png";

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

    // Tracks which way the player is facing for flashlight cone
    private boolean facingRight = true;

    // Key/flashlight pickup state
    // Might wanna change aspect ratio if final image is different
    private final Rectangle key = new Rectangle(KEY_SIZE, 10, Color.web("#facc15"));
    private boolean hasKey = false;
    private boolean hasFlashlight = false;

    // The darkness canvas that creates the flashlight cone "cut out"
    private final Canvas darknessCanvas = new Canvas();

    public Level6(GameConfig config, AppRouter router) {
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

        StackPane container = new StackPane(root, pauseLayer);
        Scene scene = new Scene(container, config.getWorldWidth(), config.getWorldHeight());
        installInput(scene);
        startLoop();
        return scene;
    }

    @Override
    protected String getLevelTitle() {
        return "Level 6";
    }

    @Override
    protected int getPreviousLevelId() {
        return 5;
    }

    @Override
    protected int getNextLevelId() {
        return 7;
    }

    @Override
    protected void buildLevel() {
        setTiledBackground(BACKGROUND_IMAGE, BACKGROUND_TILE_SIZE, 144);

        // Top floor
        double topFloorY = 185;
        double topFloorWidth = config.getWorldWidth() * 0.75;
        addSolidBlock(0, topFloorY, topFloorWidth, FLOOR_HEIGHT);
        tileBlock(solidBlocks.get(solidBlocks.size() - 1).getNode(), FLOOR_TILE_IMAGE, TILE_SIZE, 24);

        // Middle floor
        double middleFloorY = 430;
        double middleFloorStartX = config.getWorldWidth() * 0.20;
        addSolidBlock(middleFloorStartX, middleFloorY, config.getWorldWidth() - middleFloorStartX, FLOOR_HEIGHT);
        tileBlock(solidBlocks.get(solidBlocks.size() - 1).getNode(), FLOOR_TILE_IMAGE, TILE_SIZE, 24);

        // Bottom floor
        addSolidBlock(0, FLOOR_Y, config.getWorldWidth(), FLOOR_HEIGHT);
        tileBlock(solidBlocks.get(solidBlocks.size() - 1).getNode(), FLOOR_TILE_IMAGE, TILE_SIZE, 24);

        // Goal on bottom floor
        setGoal(config.getWorldWidth() - 54, FLOOR_Y - 72);
        applyImageToGoal(GOAL_IMAGE);

        // Spawn door, drawn at the player's spawn position on the top floor
        //double spawnDoorX = PLAYER_SPAWN_X + config.getPlayerWidth() / 2 - DOOR_VISUAL_WIDTH / 2;
        //double spawnDoorY = topFloorY - DOOR_VISUAL_HEIGHT;
        //addDoorVisual(spawnDoorX, spawnDoorY, SPAWN_DOOR_IMAGE);

        // Key/flashlight pickup on top floor near spawn point
        key.setArcWidth(10);
        key.setArcHeight(10);
        key.setLayoutX(PLAYER_SPAWN_X + 60);
        key.setLayoutY(topFloorY - KEY_SIZE - 2);
        applyImageToKey(KEY_IMAGE);
        root.getChildren().add(key);
    }

    private void createPlayer() {
        // Spawn slightly above the top of the screen so we drop down onto the top floor
        double spawnY = -config.getPlayerHeight() - 50;
        player = new Player(PLAYER_SPAWN_X, spawnY, config.getPlayerWidth(), config.getPlayerHeight(), config.getPlayerColor());
        root.getChildren().add(player.getNode());
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

    // Adds a tiled background at the bottom layer of the scene.
    private void setTiledBackground(String imagePath, double tileWidth, double tileHeight) {
        try {
            Image tileImage = new Image(new java.io.File(imagePath).toURI().toString());
            ImagePattern pattern = new ImagePattern(
                tileImage,
                0, 0,
                tileWidth, tileHeight,
                false
            );

            Rectangle bgRect = new Rectangle(config.getWorldWidth(), config.getWorldHeight());
            bgRect.setFill(pattern);
            bgRect.setMouseTransparent(true);

            // Index 0 = bottom of the render stack
            root.getChildren().add(0, bgRect);
        } catch (Exception e) {
            System.err.println("Failed to load tiled background: " + imagePath);
            System.err.println("Reason: " + e.getMessage());
        }
    }

    // Places a door visual centered on the goal hitbox.
    // The hitbox itself is left transparent so the door image isn't squished to fit it.
    private void applyImageToGoal(String imagePath) {
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
            double goalBottom = goal.getLayoutY() + goal.getHeight();

            doorVisual.setLayoutX(goalCenterX - DOOR_VISUAL_WIDTH / 2);
            doorVisual.setLayoutY(goalBottom - DOOR_VISUAL_HEIGHT);

            // Insert just above the tiled background so the door sits behind the player
            int insertIndex = Math.min(1, root.getChildren().size());
            root.getChildren().add(insertIndex, doorVisual);
        } catch (Exception e) {
            System.err.println("Failed to load goal image: " + imagePath);
            System.err.println("Reason: " + e.getMessage());
        }
    }

    // Places the door image at a specific position.
    // Unlike applyImageToGoal, this doesn't affect the hitbox.
    // Inserts behind interactive nodes so the player walks "through" the doorway visually.
    // However the intent with this level is for the player to fall into it, so there is no spawn door.
    /*private void addDoorVisual(double x, double y, String imagePath) {
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

            // Insert just above the tiled background (which sits at index 0)
            // so the door is behind the player and all gameplay elements.
            int insertIndex = Math.min(1, root.getChildren().size());
            root.getChildren().add(insertIndex, door);
        } catch (Exception e) {
            System.err.println("Failed to load door image: " + imagePath);
            System.err.println("Reason: " + e.getMessage());
        }
    }*/

    // Applies an image to the key/flashlight pickup.
    // If KEY_IMAGE is null, the key keeps its default yellow square look.
    private void applyImageToKey(String imagePath) {
        if (imagePath == null) {
            return;
        }
        try {
            Image img = new Image(new java.io.File(imagePath).toURI().toString());
            ImagePattern pattern = new ImagePattern(img);
            key.setFill(pattern);
        } catch (Exception e) {
            System.err.println("Failed to load key image: " + imagePath);
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

            // Track facing direction so the flashlight cone points the right way
            if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) {
                facingRight = true;
            } else if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) {
                facingRight = false;
            }

            if (!paused) {
                activeKeys.add(event.getCode());
            }
        });

        scene.setOnKeyReleased(event -> {
            activeKeys.remove(event.getCode());

            // If user releases LEFT but RIGHT is still held, flip to facing right.
            // Same idea in reverse. Keeps the flashlight from getting stuck in the wrong direction.
            if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) {
                if (activeKeys.contains(KeyCode.RIGHT) || activeKeys.contains(KeyCode.D)) {
                    facingRight = true;
                }
            } else if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) {
                if (activeKeys.contains(KeyCode.LEFT) || activeKeys.contains(KeyCode.A)) {
                    facingRight = false;
                }
            }
        });
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
        checkKeyCollection();
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
        // Same 3-floor layout as buildLevel()
        Platform[] platforms = new Platform[] {
            new Platform(0, 185, config.getWorldWidth() * 0.75, FLOOR_HEIGHT),                                                  // Top
            new Platform(config.getWorldWidth() * 0.20, 430, config.getWorldWidth() - (config.getWorldWidth() * 0.20), FLOOR_HEIGHT),  // Middle
            new Platform(0, FLOOR_Y, config.getWorldWidth(), FLOOR_HEIGHT)                                                       // Bottom
        };

        for (Platform platform : platforms) {
            resolvePlatformCollision(platform);
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
    // KEY / FLASHLIGHT PICKUP
    // ============================================================

    private void checkKeyCollection() {
        if (!hasKey && player.getBounds().intersects(key.getBoundsInParent())) {
            hasKey = true;
            hasFlashlight = true;
            key.setVisible(false);
        }
    }

    @Override
    protected void onSolidPlayerOutOfWorld() {
        // Reset both the player and the key on death so they have to pick it up again
        player.resetToSpawn();
        solidPreviousX = player.getX();
        hasKey = false;
        hasFlashlight = false;
        key.setVisible(true);
    }

    // ============================================================
    // DARKNESS / FLASHLIGHT RENDERING
    // ============================================================

    private void configureDarknessLayer() {
        darknessCanvas.setWidth(config.getWorldWidth());
        darknessCanvas.setHeight(config.getWorldHeight());
        darknessCanvas.setMouseTransparent(true);

        root.getChildren().add(darknessCanvas);

        // EVEN_ODD fill rule lets us put holes in shapes
        // Used here for cutting the flashlight cone out of the darkness.
        GraphicsContext gc = darknessCanvas.getGraphicsContext2D();
        gc.setFillRule(FillRule.EVEN_ODD);

        // Draw once immediately.
        // Without this, upon loading into level, for a split second the level is in pure light.
        updateDarknessLayer();
    }

    private void updateDarknessLayer() {
        double width = darknessCanvas.getWidth();
        double height = darknessCanvas.getHeight();

        GraphicsContext gc = darknessCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        double centerX = player.getX() + player.getWidth() * 0.5;
        double centerY = player.getY() + player.getHeight() * 0.5;

        // Soft light around the player, fading to black at LIGHT_RADIUS distance.
        RadialGradient playerLightGradient = new RadialGradient(
                0,
                0,
                centerX,
                centerY,
                LIGHT_RADIUS,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(0, 0, 0, 0.0)),
                new Stop(0.45, Color.rgb(0, 0, 0, 0.0)),
                new Stop(LIGHT_EDGE_START, Color.rgb(0, 0, 0, 0.55)),
                new Stop(1.0, Color.rgb(0, 0, 0, 1))
        );

        gc.setFill(playerLightGradient);

        // Only draw the flashlight cone once the player has picked up the key.
        // Before that, they're stuck with just the small radial glow.
        if (hasFlashlight) {
            drawDarknessWithFlashlightCutout(gc, centerX, centerY, width, height);
        } else {
            gc.fillRect(0, 0, width, height);
        }
    }

    // Draws the dark layer with a triangular flashlight cone cut out of it.
    // Works by drawing both shapes in one path with the EVEN_ODD fill rule.
    // Any overlapping areas cancel out, leaving the cone area transparent.
    private void drawDarknessWithFlashlightCutout(
            GraphicsContext gc,
            double playerCenterX,
            double playerCenterY,
            double width,
            double height
    ) {
        // Cone extends off-screen in the direction the player is facing
        double farX = facingRight ? width + 120 : -120;

        double topY = Math.max(0, playerCenterY - 160);
        double bottomY = Math.min(height, playerCenterY + 200);

        gc.save();
        gc.setFillRule(FillRule.EVEN_ODD);

        gc.beginPath();

        // Full-screen darkness rectangle
        gc.moveTo(0, 0);
        gc.lineTo(width, 0);
        gc.lineTo(width, height);
        gc.lineTo(0, height);
        gc.closePath();

        // Flashlight cone, this gets "cut" from the darkness above
        gc.moveTo(playerCenterX, playerCenterY - 24);
        gc.lineTo(farX, topY);
        gc.lineTo(farX, bottomY);
        gc.lineTo(playerCenterX, playerCenterY + 24);
        gc.closePath();

        gc.fill();
        gc.restore();
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