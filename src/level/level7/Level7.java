package level.level7;

import config.GameConfig;
import core.AppRouter;
import entity.Player;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
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

// Level 7: Weeping Angel introduction
public final class Level7 extends BaseLevel {

    // === LEVEL CONSTANTS ===

    // Flashlight visuals
    private static final double LIGHT_RADIUS = 170;
    private static final double LIGHT_EDGE_START = 0.62;

    // Reference resolution the level layout was designed for.
    // Used by scaleX/scaleY so the layout adapts to any window size.
    private static final double REF_W = 1170.0;
    private static final double REF_H = 663.0;

    // Tile sizes for textures
    private static final double PLATFORM_TILE_WIDTH = 48;
    private static final double PLATFORM_TILE_HEIGHT = 24;
    private static final double BACKGROUND_TILE_SIZE = 256;

    // How long the red death fade lasts before respawning
    private static final double DEATH_DURATION = 1.0;

    // Assets
    private static final String BACKGROUND_IMAGE = "Pictures/Backgrounds/brick_background.png";
    private static final String PLATFORM_TILE_IMAGE = "Pictures/Platforms/blue.png";
    private static final String GOAL_IMAGE = "Pictures/Portal/door2.png";
    private static final String SPAWN_DOOR_IMAGE = "Pictures/Portal/door1.png";

    // How big to draw the door images (in pixels)
    private static final double DOOR_VISUAL_WIDTH = 60;
    private static final double DOOR_VISUAL_HEIGHT = 75;

    // Angel facing sprites - set both to null to keep default red ovals
    private static final String ANGEL_FACING_RIGHT_IMAGE = "Pictures/Character/angel_right.png";
    private static final String ANGEL_FACING_LEFT_IMAGE = "Pictures/Character/angel_left.png";

    // === STATE ===

    private AnimationTimer timer;
    private Method baseUpdateMethod;
    private VBox pauseMenu;
    private StackPane pauseLayer;
    private boolean paused;
    private long lastFrame = -1;

    // Tracks which way the player is facing for flashlight cone
    private boolean facingRight = true;

    // Death sequence state
    private boolean isDying = false;
    private double deathTimer = 0;
    private final Rectangle deathOverlay = new Rectangle();

    // The darkness canvas that creates the flashlight cone "cut out"
    private final Canvas darknessCanvas = new Canvas();

    // All platforms in the level
    private final List<Platform> levelPlatforms = new ArrayList<>();

    // All active angel enemies
    private final List<Angel> angels = new ArrayList<>();

    public Level7(GameConfig config, AppRouter router) {
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

        // Set up the red flash that plays on death
        deathOverlay.setWidth(config.getWorldWidth());
        deathOverlay.setHeight(config.getWorldHeight());
        deathOverlay.setFill(Color.RED);
        deathOverlay.setOpacity(0);
        deathOverlay.setMouseTransparent(true);

        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        paused = false;

        root.getChildren().add(darknessCanvas);
        root.getChildren().add(deathOverlay);

        createPlayer();
        buildLevel();
        configureDarknessLayer();

        // Make sure deathOverlay sits on top of the darkness layer.
        // It can end up parented to the old root after a level switch, so re-parent it here.
        if (deathOverlay.getParent() != null) {
            ((javafx.scene.layout.Pane) deathOverlay.getParent()).getChildren().remove(deathOverlay);
        }
        root.getChildren().add(deathOverlay);

        createPauseLayer();

        StackPane container = new StackPane(root, pauseLayer);
        Scene scene = new Scene(container, config.getWorldWidth(), config.getWorldHeight());
        installInput(scene);
        startLoop();
        return scene;
    }

    @Override
    protected String getLevelTitle() {
        return "Level 7";
    }

    @Override
    protected int getPreviousLevelId() {
        return 6;
    }

    @Override
    protected int getNextLevelId() {
        return 8;
    }

    @Override
    protected void buildLevel() {
        setTiledBackground(BACKGROUND_IMAGE, BACKGROUND_TILE_SIZE, 144);

        levelPlatforms.clear();

        // Top platforms
        addLevelPlatform(0, 113, 169, 77);       // Top-left
        addLevelPlatform(297, 113, 159, 77);     // Top-middle
        addLevelPlatform(600, 113, 570, 77);     // Top-right long platform

        // Middle platforms
        addLevelPlatform(0, 353, 853, 76);       // Middle-left long platform
        addLevelPlatform(1009, 353, 161, 76);    // Middle-right

        // Bottom floor
        addLevelPlatform(0, 604, 1170, 49);

        // Goal on bottom-left
        setGoal(scaleX(74), scaleY(534));
        applyImageToGoal(GOAL_IMAGE);

        // Spawn door, drawn at the player's spawn position
        // We sit it on the top-right platform where the player drops from
        double spawnDoorX = scaleX(1075) + config.getPlayerWidth() / 2 - DOOR_VISUAL_WIDTH / 2;
        double spawnDoorY = scaleY(113) - DOOR_VISUAL_HEIGHT;
        addDoorVisual(spawnDoorX, spawnDoorY, SPAWN_DOOR_IMAGE);

        // Clean up any leftover angel nodes from a previous run
        for (Angel a : angels) root.getChildren().remove(a.node);
        angels.clear();

        // Angel on top level (near spawn)
        addAngel(scaleX(70), scaleY(68));
        // Angel on middle right
        addAngel(scaleX(1080), scaleY(308));
        // Angel on bottom right
        addAngel(scaleX(1080), scaleY(558));
    }

    private void createPlayer() {
        // Spawn above the top-right platform
        double spawnX = scaleX(1075);
        double spawnY = config.getPlayerHeight();

        player = new Player(
                spawnX,
                spawnY,
                config.getPlayerWidth(),
                config.getPlayerHeight(),
                config.getPlayerColor()
        );

        root.getChildren().add(player.getNode());
        solidPreviousX = player.getX();
    }

    // ============================================================
    // SCALING / LAYOUT HELPERS
    // ============================================================

    // Converts a coordinate from the reference 1170x663 design space into the actual current window dimensions.
    private double scaleX(double x) {
        return x / REF_W * config.getWorldWidth();
    }

    private double scaleY(double y) {
        return y / REF_H * config.getWorldHeight();
    }

    // Adds a platform to the level using reference coordinates.
    // Also applies the platform texture automatically and stores it for collision checks.
    private void addLevelPlatform(double refX, double refY, double refW, double refH) {
        double x = scaleX(refX);
        double y = scaleY(refY);
        double w = scaleX(refW);
        double h = scaleY(refH);

        addSolidBlock(x, y, w, h);
        tileBlock(
            solidBlocks.get(solidBlocks.size() - 1).getNode(),
            PLATFORM_TILE_IMAGE,
            PLATFORM_TILE_WIDTH,
            PLATFORM_TILE_HEIGHT
        );

        levelPlatforms.add(new Platform(x, y, w, h));
    }

    // ============================================================
    // IMAGE / TILING HELPERS
    // ============================================================

    // Fills a rectangle with a repeating image pattern.
    // tileWidth and tileHeight controls how big each "tile" is when rendered.
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

            // Insert just above the tiled background (which sits at index 0)
            // so the door is behind the player and all gameplay elements.
            int insertIndex = Math.min(1, root.getChildren().size());
            root.getChildren().add(insertIndex, door);
        } catch (Exception e) {
            System.err.println("Failed to load door image: " + imagePath);
            System.err.println("Reason: " + e.getMessage());
        }
    }

    // Applies an image to an angel enemy.
    // Stores both facing sprite paths on the angel and sets the initial right-facing sprite.
    // If either path is null, the angel keeps its default red oval look.
    private void applyImageToAngel(Angel angel, String rightImagePath, String leftImagePath) {
        if (rightImagePath == null || leftImagePath == null) {
            return;
        }
        angel.rightImagePath = rightImagePath;
        angel.leftImagePath = leftImagePath;

        // Drop the oval rounding since the image will have its own silhouette
        angel.node.setArcWidth(0);
        angel.node.setArcHeight(0);

        // Set initial sprite based on which way the angel is currently facing
        updateAngelSprite(angel);
    }

    // Swaps the angel's sprite based on its facingRight flag.
    // Called every time the angel changes direction.
    private void updateAngelSprite(Angel angel) {
        if (angel.rightImagePath == null || angel.leftImagePath == null) {
            return;
        }
        String path = angel.facingRight ? angel.rightImagePath : angel.leftImagePath;
        try {
            Image img = new Image(new java.io.File(path).toURI().toString());
            angel.node.setFill(new ImagePattern(img));
        } catch (Exception e) {
            System.err.println("Failed to load angel image: " + path);
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

        // If we're in the middle of a death animation, fade the red overlay in and respawn once it finishes.
        // Skip all other game logic during this.
        if (isDying) {
            deathTimer += deltaSeconds;
            double opacity = Math.min(1.0, deathTimer / DEATH_DURATION);
            deathOverlay.setOpacity(opacity);

            if (deathTimer >= DEATH_DURATION) {
                resetLevel();
            }
            return;
        }

        solidPreviousX = player.getX();
        invokeBaseUpdate(deltaSeconds);
        resolveFloorCollision();
        updateAngels(deltaSeconds);
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
        for (Platform platform : levelPlatforms) {
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

    // Returns the top Y of whatever platform the player is currently standing on, or -1 if they're in the air.
    // Used to decide which angels should activate.
    private double getPlayerCurrentFloorY() {
        double pLeft = player.getX();
        double pRight = player.getX() + player.getWidth();
        double pBottom = player.getY() + player.getHeight();

        for (Platform p : levelPlatforms) {
            boolean horizontallyOver = pRight > p.x && pLeft < (p.x + p.width);
            // Small epsilon so floating-point errors don't make a grounded player look like they're in the air.
            boolean restingOnTop = Math.abs(pBottom - p.y) < 6;
            if (horizontallyOver && restingOnTop) {
                return p.y;
            }
        }
        return -1;
    }

    // ============================================================
    // DEATH / RESPAWN
    // ============================================================

    @Override
    protected void onSolidPlayerOutOfWorld() {
        // Guards against multiple death triggers in a single frame
        if (isDying) return;

        isDying = true;
        deathTimer = 0;
        deathOverlay.setOpacity(0);
    }

    private void resetLevel() {
        isDying = false;
        deathOverlay.setOpacity(0);
        player.resetToSpawn();
        solidPreviousX = player.getX();

        for (Angel angel : angels) {
            angel.reset();
            updateAngelSprite(angel);
        }
    }

    // ============================================================
    // DARKNESS / FLASHLIGHT RENDERING
    // ============================================================

    private void configureDarknessLayer() {
        darknessCanvas.setWidth(config.getWorldWidth());
        darknessCanvas.setHeight(config.getWorldHeight());
        darknessCanvas.setMouseTransparent(true);

        // After a level switch, the canvas might still be parented to the old root.
        // This detaches it first to avoid a "duplicate children" exception.
        if (darknessCanvas.getParent() != null) {
            ((javafx.scene.layout.Pane) darknessCanvas.getParent()).getChildren().remove(darknessCanvas);
        }

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

        // Player has a flashlight permanently in this level
        drawDarknessWithFlashlightCutout(gc, centerX, centerY, width, height);
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
    // ANGEL ENEMIES
    // ============================================================

    private void addAngel(double x, double y) {
        double w = config.getPlayerWidth();
        double h = config.getPlayerHeight();
        Angel angel = new Angel(x, y, w, h);

        // Figure out which platform Y this angel will land on.
        // This is also later used to decide when to activate it.
        angel.floorY = findFloorBelow(x, w, y);

        // Apply directional sprites if both are set, otherwise stays as the default red oval
        applyImageToAngel(angel, ANGEL_FACING_RIGHT_IMAGE, ANGEL_FACING_LEFT_IMAGE);

        angels.add(angel);
        root.getChildren().add(angel.node);
    }

    private void updateAngels(double deltaSeconds) {
        double playerX = player.getX();
        double playerStandingOnFloorY = getPlayerCurrentFloorY();

        for (Angel angel : angels) {
            // Activate the angel only when the player stands on its platform level.
            // This stops every angel in the level from charging at you the second you start moving anywhere.
            if (!angel.isActive) {
                if (playerStandingOnFloorY > 0
                        && Math.abs(playerStandingOnFloorY - angel.floorY + 5) < 10) {
                    angel.isActive = true;
                    System.out.println("Angel activated. Angel floorY="
                            + String.format("%.0f", angel.floorY)
                            + ", Player standing on="
                            + String.format("%.0f", playerStandingOnFloorY));
                }
            }

            if (angel.isActive) {
                // Classic Weeping Angel rule: they only move when you're not looking.
                // Here "looking" means being hit by the flashlight cone.
                if (!isAngelInFlashlight(angel)) {
                    if (angel.onGround) {

                        // Update facing direction first
                        boolean wantsToFaceRight = playerX > angel.x;
                        if (wantsToFaceRight != angel.facingRight) {
                            angel.facingRight = wantsToFaceRight;
                            updateAngelSprite(angel);
                        }

                        // Then move in that direction
                        if (angel.facingRight) {
                            angel.x += angel.speed * deltaSeconds;
                        } else {
                            angel.x -= angel.speed * deltaSeconds;
                        }
                    }
                }

                resolveAngelPhysics(angel, deltaSeconds);

                // Touching an angel = death sequence starts
                if (player.getBounds().intersects(angel.node.getBoundsInParent())) {
                    onSolidPlayerOutOfWorld();
                }
            }
        }
    }

    // Checks if an angel sits inside the player's flashlight cone.
    // Used to freeze the angel in place.
    private boolean isAngelInFlashlight(Angel angel) {
        double px = player.getX() + player.getWidth() / 2;
        double py = player.getY() + player.getHeight() / 2;
        double ax = angel.x + angel.width / 2;
        double ay = angel.y + angel.height / 2;

        // Must be in front of the player based on facing direction
        if (facingRight && ax < px) return false;
        if (!facingRight && ax > px) return false;

        // Angels too far away don't get the freeze treatment
        double dist = Math.abs(ax - px);
        if (dist > 600) return false;

        // Cone widens out as it gets further from the player.
        // Coefficient picked to roughly match the visual cone width.
        double coneHalfHeightAtDist = 24 + (dist / REF_W) * 180;

        return ay > py - coneHalfHeightAtDist && ay < py + coneHalfHeightAtDist;
    }

    // Angel gravity + platform collision.
    // Same player's collision but applied to the angel.
    private void resolveAngelPhysics(Angel angel, double deltaSeconds) {
        angel.velocityY += config.getGravity() * deltaSeconds;
        angel.y += angel.velocityY * deltaSeconds;

        angel.onGround = false;

        for (Platform platform : levelPlatforms) {
            double angelLeft = angel.x;
            double angelRight = angel.x + angel.width;
            double angelTop = angel.y;
            double angelBottom = angel.y + angel.height;

            double platLeft = platform.x;
            double platRight = platform.x + platform.width;
            double platTop = platform.y;
            double platBottom = platform.y + platform.height;

            if (angelRight > platLeft && angelLeft < platRight &&
                angelBottom > platTop && angelTop < platBottom) {

                double overlapTop = angelBottom - platTop;
                double overlapBottom = platBottom - angelTop;
                double overlapLeft = angelRight - platLeft;
                double overlapRight = platRight - angelLeft;

                double minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
                                              Math.min(overlapTop, overlapBottom));

                if (minOverlap == overlapTop && angel.velocityY >= 0) {
                    // Landed on top of floor
                    angel.y = platform.y - angel.height;
                    angel.velocityY = 0;
                    angel.onGround = true;
                    angel.velocityX = 0;

                } else if (minOverlap == overlapBottom && angel.velocityY <= 0) {
                    // Bumped its head on ceiling
                    angel.y = platBottom;
                    angel.velocityY = 0;

                } else if (minOverlap == overlapLeft) {
                    angel.x = platLeft - angel.width;
                    angel.velocityX = 0;

                } else if (minOverlap == overlapRight) {
                    angel.x = platRight;
                    angel.velocityX = 0;
                }
            }
        }

        angel.node.setLayoutX(angel.x);
        angel.node.setLayoutY(angel.y);
    }

    // Walks downward from a point to find which platform's top sits just below it.
    // Used at spawn time to figure out which "level" an angel belongs to.
    private double findFloorBelow(double x, double w, double y) {
        double best = Double.MAX_VALUE;
        for (Platform p : levelPlatforms) {
            boolean horizontallyOver = (x + w) > p.x && x < (p.x + p.width);
            if (horizontallyOver && p.y >= y) {
                if (p.y < best) {
                    best = p.y;
                }
            }
        }
        return best;
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

    // A single Weeping Angel enemy.
    // Stores its position, velocity, visual node, and the platform it spawns on.
    private static class Angel {
        Rectangle node;
        double x, y;
        double startX, startY;
        double velocityX, velocityY;
        boolean isActive = false;
        boolean onGround = false;
        double speed = 110.0;
        double width, height;
        double floorY;

        // Facing direction and the two sprite paths for swapping
        boolean facingRight = true;
        String rightImagePath;
        String leftImagePath;

        Angel(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.startX = x;
            this.startY = y;
            this.width = width;
            this.height = height;
            this.node = new Rectangle(width, height, Color.RED);
            // Round it off into an oval shape so it doesn't look like a flat rectangle
            this.node.setArcWidth(width);
            this.node.setArcHeight(height);
            this.node.setLayoutX(x);
            this.node.setLayoutY(y);
        }

        // Send the angel back to its starting position
        // Called after the player dies.
        void reset() {
            this.x = startX;
            this.y = startY;
            this.velocityX = 0;
            this.velocityY = 0;
            this.isActive = false;
            this.onGround = false;
            this.facingRight = true;
            this.node.setLayoutX(x);
            this.node.setLayoutY(y);
        }
    }
}