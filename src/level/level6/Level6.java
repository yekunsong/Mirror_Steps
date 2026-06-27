package level.level6; 

import config.GameConfig; 
import core.AppRouter; 
import entity.Player; 
import java.lang.reflect.Method; 
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
import javafx.scene.shape.Rectangle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.FillRule;
import level.BaseLevel; 
 
public final class Level6 extends BaseLevel { 
 
    private static final double FLOOR_Y = 680; 
    private static final double FLOOR_HEIGHT = 80; 
    private static final double PLAYER_SPAWN_X = 24; 
    private static final double LIGHT_RADIUS = 170; 
    private static final double LIGHT_EDGE_START = 0.62;

    private AnimationTimer timer; 
    private Method baseUpdateMethod; 
    private VBox pauseMenu; 
    private StackPane pauseLayer; 
    private boolean paused; 
    private long lastFrame = -1; 
    
    private final Rectangle key = new Rectangle(20, 20, Color.web("#facc15"));
    private boolean hasKey = false;
    private static final double KEY_SIZE = 20;
 
    private final Canvas darknessCanvas = new Canvas();

    private boolean hasFlashlight = false;   // or whatever pickup flag you're using
    private boolean facingRight = true;
    
    public Level6(GameConfig config, AppRouter router) { 
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
 
        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight()); 
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))); 
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
        
     // KEY: Place on top floor near spawn point
        key.setArcWidth(10);
        key.setArcHeight(10);
        key.setLayoutX(PLAYER_SPAWN_X + 60); // 60px to the right of spawn
        key.setLayoutY(topFloorY - KEY_SIZE - 2); // Sitting on top of the floor
        root.getChildren().add(key);
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
    protected void onSolidPlayerOutOfWorld() {
        player.resetToSpawn();
        solidPreviousX = player.getX();
        hasKey = false;
        hasFlashlight = false;
        key.setVisible(true);
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
        double spawnY = -config.getPlayerHeight() - 50; // 50px above the top of the screen
        player = new Player(PLAYER_SPAWN_X, spawnY, config.getPlayerWidth(), config.getPlayerHeight(), config.getPlayerColor()); 
        root.getChildren().add(player.getNode()); 
        solidPreviousX = player.getX(); 
    }  
 
    private void configureDarknessLayer() {
        darknessCanvas.setWidth(config.getWorldWidth());
        darknessCanvas.setHeight(config.getWorldHeight());
        darknessCanvas.setMouseTransparent(true);

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
        if (paused) {
            return;
        }

        solidPreviousX = player.getX();
        invokeBaseUpdate(deltaSeconds);
        resolveFloorCollision();
        checkKeyCollection();
        updateDarknessLayer();
    }

    private void checkKeyCollection() {
        if (!hasKey && player.getBounds().intersects(key.getBoundsInParent())) {
            hasKey = true;
            hasFlashlight = true;
            key.setVisible(false);
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
        // Define all platforms
        Platform[] platforms = new Platform[] {
            new Platform(0, 185, config.getWorldWidth() * 0.75, FLOOR_HEIGHT),  // TOP
            new Platform(config.getWorldWidth() * 0.20, 430, config.getWorldWidth() - (config.getWorldWidth() * 0.20), FLOOR_HEIGHT),  // MIDDLE
            new Platform(0, FLOOR_Y, config.getWorldWidth(), FLOOR_HEIGHT)  // BOTTOM
        };
        
        for (Platform platform : platforms) {
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

        if (hasFlashlight) {
            drawDarknessWithFlashlightCutout(gc, centerX, centerY, width, height);
        } else {
            gc.fillRect(0, 0, width, height);
        }
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

        gc.save();

        /*
         * EVEN_ODD fill rule:
         * - first shape: full screen darkness
         * - second shape: flashlight cone hole
         *
         * So the darkness is drawn everywhere EXCEPT inside the cone.
         */
        gc.setFillRule(FillRule.EVEN_ODD);

        gc.beginPath();

        // Full screen rectangle
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

        gc.restore();
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
 
} 
