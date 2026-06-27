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
import javafx.scene.control.Button; 
import javafx.scene.control.Label; 
import javafx.scene.Scene; 
import javafx.scene.input.KeyCode; 
import javafx.scene.layout.Background; 
import javafx.scene.layout.BackgroundFill; 
import javafx.scene.layout.CornerRadii; 
import javafx.scene.layout.StackPane; 
import javafx.scene.layout.VBox; 
import javafx.scene.paint.Color; 
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Rectangle;
import level.BaseLevel; 
 
public final class Level7 extends BaseLevel { 

    private static final double LIGHT_RADIUS = 170; 
    private static final double LIGHT_EDGE_START = 0.62;

    private static final double REF_W = 1170.0;
    private static final double REF_H = 663.0;
    
    private AnimationTimer timer; 
    private Method baseUpdateMethod; 
    private VBox pauseMenu; 
    private StackPane pauseLayer; 
    private boolean paused; 
    private long lastFrame = -1; 
 
    private final Canvas darknessCanvas = new Canvas();

    private boolean facingRight = true;
    
    private boolean isDying = false;
    private double deathTimer = 0;
    private static final double DEATH_DURATION = 1.0; // seconds for the red fade
    private final Rectangle deathOverlay = new Rectangle();
    
    private final List<Platform> levelPlatforms = new ArrayList<>();
    private final List<Angel> angels = new ArrayList<>();
    
    public Level7(GameConfig config, AppRouter router) { 
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
        
        // Initialize death overlay properties
        deathOverlay.setWidth(config.getWorldWidth());
        deathOverlay.setHeight(config.getWorldHeight());
        deathOverlay.setFill(Color.RED);
        deathOverlay.setOpacity(0);
        deathOverlay.setMouseTransparent(true);
 
        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight()); 
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))); 
        paused = false; 
        
        root.getChildren().add(darknessCanvas); 
        root.getChildren().add(deathOverlay);
 
        createPlayer(); 
        buildLevel(); 
        configureDarknessLayer(); 
        createPauseLayer(); 
        
        configureDarknessLayer(); 
 
        // FIX: Remove from old parent if it exists before adding to the new root
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
    protected void buildLevel() {
        levelPlatforms.clear();

        /*
         * Black rectangle layout based on the image.
         *
         * Reference image dimensions:
         * width  = 1170
         * height = 663
         */

        // Top-left platform
        addLevelPlatform(0, 113, 169, 77);

        // Top-middle platform
        addLevelPlatform(297, 113, 159, 77);

        // Top-right long platform
        addLevelPlatform(600, 113, 570, 77);

        // Middle-left long platform
        addLevelPlatform(0, 353, 853, 76);

        // Middle-right platform
        addLevelPlatform(1009, 353, 161, 76);

        // Bottom floor
        addLevelPlatform(0, 604, 1170, 49);

        setGoal(scaleX(74), scaleY(534));
        
        angels.clear();

        // 1. Top left (near spawn level)
        addAngel(scaleX(70), scaleY(68)); 
        
        // 2. Middle right
        addAngel(scaleX(1080), scaleY(308));

        // 3. Bottom right
        addAngel(scaleX(1080), scaleY(558));
    }

    private void addAngel(double x, double y) {
        double w = config.getPlayerWidth();
        double h = config.getPlayerHeight();
        Angel angel = new Angel(x, y, w, h);
        
        // Determine which platform this angel rests on
        angel.floorY = findFloorBelow(x, w, y);
        
        angels.add(angel);
        root.getChildren().add(angel.node);
    }
    
    private boolean isAngelInFlashlight(Angel angel) {
        double px = player.getX() + player.getWidth() / 2;
        double py = player.getY() + player.getHeight() / 2;
        double ax = angel.x + angel.width / 2;  // Center of angel
        double ay = angel.y + angel.height / 2;

        // Check if angel is on the correct side
        if (facingRight && ax < px) return false;
        if (!facingRight && ax > px) return false;

        // Check distance
        double dist = Math.abs(ax - px);
        if (dist > 600) return false;

        // Check if inside the cone slope
        double coneHalfHeightAtDist = 24 + (dist / REF_W) * 180;
        
        return ay > py - coneHalfHeightAtDist && ay < py + coneHalfHeightAtDist;
    }
    
    private void resolveAngelPhysics(Angel angel, double deltaSeconds) {
        // Apply gravity
        angel.velocityY += config.getGravity() * deltaSeconds;
        
        // Update vertical position
        angel.y += angel.velocityY * deltaSeconds;
        
        // Check platform collisions
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
            
            // Check intersection
            if (angelRight > platLeft && angelLeft < platRight &&
                angelBottom > platTop && angelTop < platBottom) {
                
                double overlapTop = angelBottom - platTop;
                double overlapBottom = platBottom - angelTop;
                double overlapLeft = angelRight - platLeft;
                double overlapRight = platRight - angelLeft;
                
                double minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
                                              Math.min(overlapTop, overlapBottom));
                
                if (minOverlap == overlapTop && angel.velocityY >= 0) {
                    // Landed on platform
                    angel.y = platform.y - angel.height;
                    angel.velocityY = 0;
                    angel.onGround = true;
                    angel.velocityX = 0; // Stop horizontal drift
                    
                } else if (minOverlap == overlapBottom && angel.velocityY <= 0) {
                    // Hit ceiling
                    angel.y = platBottom;
                    angel.velocityY = 0;
                    
                } else if (minOverlap == overlapLeft) {
                    // Hit wall on left
                    angel.x = platLeft - angel.width;
                    angel.velocityX = 0;
                    
                } else if (minOverlap == overlapRight) {
                    // Hit wall on right
                    angel.x = platRight;
                    angel.velocityX = 0;
                }
            }
        }
        
        // Update visual position
        angel.node.setLayoutX(angel.x);
        angel.node.setLayoutY(angel.y);
    }
    
    /**
     * Finds the top Y of the nearest platform below the given position.
     * Used to determine which "tier" an entity belongs to.
     */
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

    /**
     * Reliable grounded check: the player is grounded if their feet are
     * resting on top of any platform (within a small epsilon).
     * This is called AFTER collision resolution, so a grounded player's
     * bottom sits exactly on the platform top.
     */
    /**
     * Returns the Y of the platform the player is currently standing on,
     * or -1 if the player is airborne.
     */
    private double getPlayerCurrentFloorY() {
        double pLeft = player.getX();
        double pRight = player.getX() + player.getWidth();
        double pBottom = player.getY() + player.getHeight();

        for (Platform p : levelPlatforms) {
            boolean horizontallyOver = pRight > p.x && pLeft < (p.x + p.width);
            boolean restingOnTop = Math.abs(pBottom - p.y) < 6;
            if (horizontallyOver && restingOnTop) {
                return p.y;
            }
        }
        return -1;
    }
    
    private double scaleX(double x) {
        return x / REF_W * config.getWorldWidth();
    }

    private double scaleY(double y) {
        return y / REF_H * config.getWorldHeight();
    }

    private void addLevelPlatform(double refX, double refY, double refW, double refH) {
        double x = scaleX(refX);
        double y = scaleY(refY);
        double w = scaleX(refW);
        double h = scaleY(refH);

        addSolidBlock(x, y, w, h);
        levelPlatforms.add(new Platform(x, y, w, h));
    }
    
    /*@Override
    protected void buildLevel() {
        // TOP FLOOR: Left side, where player spawns/falls onto
        double topFloorY = 185;
        double topFloorWidth = config.getWorldWidth() * 0.75; // 65% of screen width
        addSolidBlock(0, topFloorY, topFloorWidth, FLOOR_HEIGHT);

        // MIDDLE FLOOR: Right side, player drops from top floor to this
        double middleFloorY = 430;
        double middleFloorStartX = config.getWorldWidth() * 0.20; // Starts at 30% of screen
        addSolidBlock(middleFloorStartX, middleFloorY, config.getWorldWidth() - middleFloorStartX, FLOOR_HEIGHT);

        // BOTTOM FLOOR: Full width, final floor where goal is
        addSolidBlock(0, FLOOR_Y, config.getWorldWidth(), FLOOR_HEIGHT);

        // GOAL: Bottom floor, right side
        setGoal(config.getWorldWidth() - 54, FLOOR_Y - 72);
    }*/
 
    @Override 
    protected int getPreviousLevelId() { 
        return 6; 
    } 
 
    @Override 
    protected int getNextLevelId() { 
        return 8; 
    }
    
    @Override
    protected void onSolidPlayerOutOfWorld() {
        if (isDying) return; // Prevent double triggers
        
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
 
    private void configureDarknessLayer() {
        darknessCanvas.setWidth(config.getWorldWidth());
        darknessCanvas.setHeight(config.getWorldHeight());
        darknessCanvas.setMouseTransparent(true);
        
        // FIX: Remove from old parent if it exists to avoid the "Duplicate Children" error
        if (darknessCanvas.getParent() != null) {
            ((javafx.scene.layout.Pane) darknessCanvas.getParent()).getChildren().remove(darknessCanvas);
        }

        root.getChildren().add(darknessCanvas);

        // Set this once; it persists on the GraphicsContext
        GraphicsContext gc = darknessCanvas.getGraphicsContext2D();
        gc.setFillRule(FillRule.EVEN_ODD);
        
        // Draw darkness immediately so there is no flash on load
        updateDarknessLayer();
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

            // If releasing LEFT but RIGHT still held -> face right
            if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) {
                if (activeKeys.contains(KeyCode.RIGHT) || activeKeys.contains(KeyCode.D)) {
                    facingRight = true;
                }
            }
            // If releasing RIGHT but LEFT still held -> face left
            else if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) {
                if (activeKeys.contains(KeyCode.LEFT) || activeKeys.contains(KeyCode.A)) {
                    facingRight = false;
                }
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
        if (paused) return;

        if (isDying) {
            deathTimer += deltaSeconds;
            // Calculate fade opacity (0 to 1)
            double opacity = Math.min(1.0, deathTimer / DEATH_DURATION);
            deathOverlay.setOpacity(opacity);

            if (deathTimer >= DEATH_DURATION) {
                resetLevel();
            }
            return; // Skip normal game logic (pauses the game world)
        }

        solidPreviousX = player.getX();
        invokeBaseUpdate(deltaSeconds);
        resolveFloorCollision();
        updateAngels(deltaSeconds);
        updateDarknessLayer();
    }
 
    private void updateAngels(double deltaSeconds) {
        double playerX = player.getX();

        // Determine which platform the player is currently standing on
        double playerStandingOnFloorY = getPlayerCurrentFloorY();

        for (Angel angel : angels) {
            if (!angel.isActive) {
                /*
                 * ACTIVATION LOGIC v6 (platform-exact):
                 *
                 * Activate only when the player is standing on the EXACT SAME
                 * platform (by Y) that the angel belongs to.
                 *
                 * playerStandingOnFloorY will be -1 if airborne.
                 * angel.floorY is the Y of the platform the angel rests on.
                 *
                 * We compare with a small tolerance because scaled values
                 * might differ by a pixel or two.
                 */
                if (playerStandingOnFloorY > 0 
                        && Math.abs(playerStandingOnFloorY - angel.floorY+5) < 10) {
                    angel.isActive = true;
                    System.out.println("Angel activated. Angel floorY="
                        + String.format("%.0f", angel.floorY)
                        + ", Player standing on="
                        + String.format("%.0f", playerStandingOnFloorY));
                }
            }

            if (angel.isActive) {
                // MOVEMENT: chase horizontally, freeze when in flashlight
                if (!isAngelInFlashlight(angel)) {
                    if (angel.onGround) {
                        if (playerX > angel.x) {
                            angel.x += angel.speed * deltaSeconds;
                        } else {
                            angel.x -= angel.speed * deltaSeconds;
                        }
                    }
                }

                resolveAngelPhysics(angel, deltaSeconds);

                // Catch the player -> reset
                if (player.getBounds().intersects(angel.node.getBoundsInParent())) {
                    onSolidPlayerOutOfWorld();
                }
            }
        }
    }
 
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
 
    private void resolveFloorCollision() {
        for (Platform platform : levelPlatforms) {
            resolvePlatformCollision(platform);
        }
    }

    private void resolvePlatformCollision(Platform platform) {
        double playerLeft = player.getX();
        double playerRight = player.getX() + player.getWidth();
        double playerTop = player.getY();
        double playerBottom = player.getY() + player.getHeight();
        
        double platLeft = platform.x;
        double platRight = platform.x + platform.width;
        double platTop = platform.y;
        double platBottom = platform.y + platform.height;
        
        // Check if player intersects with platform
        boolean intersects = playerRight > platLeft && playerLeft < platRight &&
                             playerBottom > platTop && playerTop < platBottom;
        
        if (!intersects) return;
        
        // Calculate overlap on each axis
        double overlapLeft = playerRight - platLeft;
        double overlapRight = platRight - playerLeft;
        double overlapTop = playerBottom - platTop;
        double overlapBottom = platBottom - playerTop;
        
        // Find the smallest overlap to determine collision side
        double minOverlap = Math.min(Math.min(overlapLeft, overlapRight), 
                                      Math.min(overlapTop, overlapBottom));
        
        // Resolve based on which side has minimum overlap
        if (minOverlap == overlapTop && player.getVelocityY() >= 0) {
            // Landing on TOP
            player.landOn(platform.y);
        } else if (minOverlap == overlapBottom && player.getVelocityY() <= 0) {
            // Hitting BOTTOM (ceiling) - pass platBottom, NOT platform.y!
            player.hitCeiling(platBottom);
        } else if (minOverlap == overlapLeft) {
            // Hitting LEFT side
            player.getNode().setLayoutX(platLeft - player.getWidth());
        } else if (minOverlap == overlapRight) {
            // Hitting RIGHT side
            player.getNode().setLayoutX(platRight);
        }
    }

    // Helper class for platform data
    private static class Platform {
        double x, y, width, height;
        Platform(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
    
    private void updateDarknessLayer() {
        double width = darknessCanvas.getWidth();
        double height = darknessCanvas.getHeight();

        GraphicsContext gc = darknessCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        double centerX = player.getX() + player.getWidth() * 0.5;
        double centerY = player.getY() + player.getHeight() * 0.5;

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
                new Stop(1.0, Color.rgb(0, 0, 0, 0.98))
        );

        gc.setFill(playerLightGradient);

        // Permanent flashlight
        drawDarknessWithFlashlightCutout(gc, centerX, centerY, width, height);
    }
    
    private void drawDarknessWithFlashlightCutout(
            GraphicsContext gc,
            double playerCenterX,
            double playerCenterY,
            double width,
            double height
    ) {
        double farX = facingRight ? width + 120 : -120;

        double topY = Math.max(0, playerCenterY - 160);
        double bottomY = Math.min(height, playerCenterY + 200);

        gc.beginPath();

        // Full-screen darkness
        gc.moveTo(0, 0);
        gc.lineTo(width, 0);
        gc.lineTo(width, height);
        gc.lineTo(0, height);
        gc.closePath();

        // Flashlight cone cutout
        gc.moveTo(playerCenterX, playerCenterY - 24);
        gc.lineTo(farX, topY);
        gc.lineTo(farX, bottomY);
        gc.lineTo(playerCenterX, playerCenterY + 24);
        gc.closePath();

        gc.fill();
    }
 
    /*private void updateDarknessLayer() {
        double centerX = player.getX() + player.getWidth() * 0.5;
        double centerY = player.getY() + player.getHeight() * 0.5;

        // Use brighter radius if player has the key (flashlight effect)
        double currentRadius = hasKey ? FLASHLIGHT_RADIUS : LIGHT_RADIUS;

        lightOverlay.setFill(new RadialGradient(
                0,
                0,
                centerX,
                centerY,
                currentRadius,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(0, 0, 0, 0.0)),
                new Stop(0.45, Color.rgb(0, 0, 0, 0.0)),
                new Stop(LIGHT_EDGE_START, Color.rgb(0, 0, 0, 0.55)),
                new Stop(1.0, Color.rgb(0, 0, 0, 0.98))));
    }*/
    
    /*private void updateDarknessLayer() { 
        double centerX = player.getX() + player.getWidth() * 0.5; 
        double centerY = player.getY() + player.getHeight() * 0.5; 
 
        lightOverlay.setFill(new RadialGradient( 
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
                new Stop(1.0, Color.rgb(0, 0, 0, 0.98)))); 
    }*/
 
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

        Angel(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.startX = x;
            this.startY = y;
            this.width = width;
            this.height = height;
            this.node = new Rectangle(width, height, Color.RED);
            // Make them circular (or rounded)
            this.node.setArcWidth(width);  
            this.node.setArcHeight(height);
            this.node.setLayoutX(x);
            this.node.setLayoutY(y);
        }
        
        void reset() {
            this.x = startX;
            this.y = startY;
            this.velocityX = 0;
            this.velocityY = 0;
            this.isActive = false;
            this.onGround = false;
            this.node.setLayoutX(x);
            this.node.setLayoutY(y);
        }
    }
 
} 

 

 