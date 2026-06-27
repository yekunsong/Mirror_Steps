package level.level5; 

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
import level.BaseLevel;
 
public final class Level5 extends BaseLevel { 
 
    private static final double FLOOR_Y = 380; 
    private static final double FLOOR_LEFT_WIDTH = 520; 
    private static final double FLOOR_GAP_WIDTH = 300; 
    private static final double FLOOR_RIGHT_X = FLOOR_LEFT_WIDTH + FLOOR_GAP_WIDTH; 
    private static final double BRIDGE_HEIGHT = 12; 
    private static final double CEILING_CLEARANCE = 100; 
    private static final double PLAYER_SPAWN_X = 24;
    private double ceilingBottomY;
    private Rectangle bridgeBlock;
    private Rectangle voidGoal;
    private boolean bridgeCollapsed;
 
    private final Rectangle lightOverlay = new Rectangle(); 
    private AnimationTimer timer; 
    private Method baseUpdateMethod; 
    private VBox pauseMenu; 
    private StackPane pauseLayer; 
    private boolean paused; 
    private long lastFrame = -1; 
 
    public Level5(GameConfig config, AppRouter router) { 
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
        root.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY))); 
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
        return "Level 5"; 
    } 
 
    @Override
    protected void buildLevel() {
        addSolidBlock(0, FLOOR_Y, FLOOR_LEFT_WIDTH, config.getWorldHeight() - FLOOR_Y);

        // BRIDGE: create manually since addSolidBlock returns void
        bridgeBlock = new Rectangle(FLOOR_GAP_WIDTH, BRIDGE_HEIGHT, Color.GRAY);
        bridgeBlock.setLayoutX(FLOOR_LEFT_WIDTH);
        bridgeBlock.setLayoutY(FLOOR_Y);
        root.getChildren().add(bridgeBlock);
        // No need to add to solidBlocks/blocks - we handle bridge collision manually
        addSolidBlock(FLOOR_RIGHT_X, FLOOR_Y, config.getWorldWidth() - FLOOR_RIGHT_X, config.getWorldHeight() - FLOOR_Y);

        ceilingBottomY = Math.max(0, player.getY() - CEILING_CLEARANCE);
        addSolidBlock(0, 0, config.getWorldWidth(), ceilingBottomY);

        // Normal goal (keep for now)
        setGoal(config.getWorldWidth() - 54, FLOOR_Y - 72);

        // VOID GOAL: placed well below screen so player falls out of view first
        voidGoal = new Rectangle(400, 10, Color.TRANSPARENT);
        voidGoal.setMouseTransparent(true);
        voidGoal.setLayoutX(FLOOR_LEFT_WIDTH);
        voidGoal.setLayoutY(config.getWorldHeight() + 50); // 200px below screen bottom
        root.getChildren().add(voidGoal);
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
    protected void onSolidPlayerOutOfWorld() { 
        player.resetToSpawn(); 
        solidPreviousX = player.getX(); 
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
        double spawnY = FLOOR_Y - config.getPlayerHeight() - 2; 
        player = new Player(PLAYER_SPAWN_X, spawnY, config.getPlayerWidth(), config.getPlayerHeight(), config.getPlayerColor()); 
        root.getChildren().add(player.getNode()); 
        solidPreviousX = player.getX(); 
    } 
 
    private void configureDarknessLayer() { 
        lightOverlay.setWidth(config.getWorldWidth()); 
        lightOverlay.setHeight(config.getWorldHeight()); 
        lightOverlay.setMouseTransparent(true); 
        lightOverlay.setFill(Color.TRANSPARENT); 
        updateDarknessLayer(); 
        root.getChildren().add(lightOverlay); 
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
        // Define all platforms for Level 5
        Platform[] platforms = new Platform[] {
            // LEFT FLOOR
            new Platform(0, FLOOR_Y, FLOOR_LEFT_WIDTH, config.getWorldHeight() - FLOOR_Y),
            // BRIDGE (only if not collapsed)
            bridgeBlock != null && !bridgeCollapsed ? 
                new Platform(FLOOR_LEFT_WIDTH, FLOOR_Y, FLOOR_GAP_WIDTH, BRIDGE_HEIGHT) : null,
            // RIGHT FLOOR
            new Platform(FLOOR_RIGHT_X, FLOOR_Y, config.getWorldWidth() - FLOOR_RIGHT_X, config.getWorldHeight() - FLOOR_Y),
            // CEILING
            new Platform(0, 0, config.getWorldWidth(), ceilingBottomY)
        };
        
        for (Platform platform : platforms) {
            if (platform != null) {
                resolvePlatformCollision(platform);
            }
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
            // Hitting BOTTOM (ceiling)
            player.hitCeiling(platBottom);
        } else if (minOverlap == overlapLeft) {
            // Hitting LEFT side
            player.getNode().setLayoutX(platLeft - player.getWidth());
        } else if (minOverlap == overlapRight) {
            // Hitting RIGHT side
            player.getNode().setLayoutX(platRight);
        }
    }

    // Helper class for platform data (DEFINE LOCALLY, don't import from Level6!)
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
        lightOverlay.setFill(Color.TRANSPARENT); 
    } 
 
    private void checkBridgeCollapse() {
        if (bridgeCollapsed || bridgeBlock == null) return;

        boolean onBridge =
                player.getBounds().intersects(bridgeBlock.getBoundsInParent()) &&
                (player.getY() + player.getHeight()) <= (FLOOR_Y + BRIDGE_HEIGHT + 2) &&
                player.getX() >= FLOOR_LEFT_WIDTH + 20; // Must walk 60px onto bridge first

        if (onBridge) {
            bridgeCollapsed = true;
            root.getChildren().remove(bridgeBlock);
        }
    }
    
    private void checkVoidGoal() {
        if (voidGoal != null && player.getBounds().intersects(voidGoal.getBoundsInParent())) {
            onGoalReached();
        }
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