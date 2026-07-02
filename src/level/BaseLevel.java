package level;

import config.GameConfig;
import core.AppRouter;
import entity.Block;
import entity.MovePlatform;
import entity.Player;
import entity.SolidBlock;
import entity.Trap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import entity.PushableBlock;
import entity.FloorButton;
import entity.Portal;

public abstract class BaseLevel {

    protected final GameConfig config;
    protected final AppRouter router;
    protected final Pane root = new Pane();
    protected final List<Block> blocks = new ArrayList<>();
    protected final List<MovePlatform> movePlatforms = new ArrayList<>();
    protected final List<SolidBlock> solidBlocks = new ArrayList<>();
    protected final List<Trap> traps = new ArrayList<>();
    protected final Set<KeyCode> activeKeys = new HashSet<>();
	protected final List<PushableBlock> pushableBlocks = new ArrayList<>();
    protected final List<Portal> portals = new ArrayList<>();
    protected final List<FloorButton> floorButtons = new ArrayList<>();
    
    protected Player player;
    protected Rectangle goal;
    protected VBox pauseMenu;
    protected StackPane overlay;
    protected double solidPreviousX;
    protected MovePlatform activeMovePlatform;

    private AnimationTimer timer;
    private boolean changingLevel;
    private boolean paused;

    protected BaseLevel(GameConfig config, AppRouter router) {
        this.config = config;
        this.router = router;
    }

    public Scene createScene() {
        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        //root.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        createPlayer();
        buildLevel();
        createPauseLayer();

        StackPane container = new StackPane(root, overlay);
        StackPane.setAlignment(pauseMenu, Pos.CENTER);

        Scene scene = new Scene(container, config.getWorldWidth(), config.getWorldHeight());
        installInput(scene);
        start();
        return scene;
    }

    protected abstract String getLevelTitle();

    protected abstract void buildLevel();

    protected abstract int getPreviousLevelId();

    protected abstract int getNextLevelId();

    protected void onGoalReached() {
        switchToLevel(getNextLevelId());
    }

    protected String getNextButtonText() {
        return "Next";
    }
    
    protected void setBackgroundImage(String imagePath) {
        try {
            Image image = new Image(new java.io.File(imagePath).toURI().toString());

            BackgroundImage backgroundImage = new BackgroundImage(
                image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(
                    1, 1,
                    true, true,
                    false, false
                )
            );

            root.setBackground(new Background(backgroundImage));
        } catch (Exception e) {
            System.err.println("Failed to load background image: " + imagePath);
            System.err.println("Reason: " + e.getMessage());
        }
    }

    protected void addBlock(double x, double y, double width, double height) {
        Block block = new Block(x, y, width, height, config.getBlockColor());
        blocks.add(block);
        root.getChildren().add(block.getNode());
    }

    protected void addBlock(double x, double y, double width, double height, String imagePath) {
        Block block = new Block(x, y, width, height, config.getBlockColor(), imagePath);
        blocks.add(block);
        root.getChildren().add(block.getNode());
    }

    protected void addSolidBlock(double x, double y, double width, double height) {
        SolidBlock block = new SolidBlock(x, y, width, height, config.getBlockColor());
        solidBlocks.add(block);
        root.getChildren().add(block.getNode());
    }

    protected void addSolidBlock(double x, double y, double width, double height, String imagePath) {
        SolidBlock block = new SolidBlock(x, y, width, height, config.getBlockColor(), imagePath);
        solidBlocks.add(block);
        root.getChildren().add(block.getNode());
    }

    protected void addMovePlatform(
            double x,
            double y,
            double width,
            double height,
            Color color,
            MovePlatform.Direction direction,
            double minBound,
            double maxBound,
            double speed) {
        MovePlatform platform =
                new MovePlatform(x, y, width, height, color, direction, minBound, maxBound, speed);
        movePlatforms.add(platform);
        root.getChildren().add(platform.getNode());
    }

    protected void addMovePlatform(
            double x,
            double y,
            double width,
            double height,
            Color color,
            String imagePath,
            MovePlatform.Direction direction,
            double minBound,
            double maxBound,
            double speed) {
        MovePlatform platform =
                new MovePlatform(x, y, width, height, color, imagePath, direction, minBound, maxBound, speed);
        movePlatforms.add(platform);
        root.getChildren().add(platform.getNode());
    }

    protected void addTrap(double x, double y, double width, double height, Color color) {
        Trap trap = new Trap(x, y, width, height, color);
        traps.add(trap);
        root.getChildren().add(trap.getNode());
    }

    protected void addTrap(double x, double y, double width, double height, Color color, String imagePath) {
        Trap trap = new Trap(x, y, width, height, color, imagePath);
        traps.add(trap);
        root.getChildren().add(trap.getNode());
    }
    
    protected FloorButton addFloorButton(double x, double y, double width, double height) {
        FloorButton button = new FloorButton(x, y, width, height);
        floorButtons.add(button);
        root.getChildren().add(button.getNode());
        return button;
    }
    
    protected FloorButton addFloorButton(double x, double y, double width, double height, String imagePath) {
        FloorButton button = new FloorButton(x, y, width, height, imagePath);
        floorButtons.add(button);
        root.getChildren().add(button.getNode());
        return button;
    }

    protected void setGoal(double x, double y) {
        goal = new Rectangle(36, 72, config.getGoalColor());
        goal.setLayoutX(x);
        goal.setLayoutY(y);
        root.getChildren().add(goal);
    }

    protected void switchToLevel(int level) {
        if (changingLevel || level == 0) {
            return;
        }
        changingLevel = true;
        stop();
        router.showLevel(level);
    }

    protected void switchToMenu() {
        if (changingLevel) {
            return;
        }
        changingLevel = true;
        stop();
        router.showMenu();
    }

    protected void resolveSolidCollisions() {

        for (SolidBlock block : solidBlocks) {
            if (!player.getBounds().intersects(block.getBounds())) {
                continue;
            }

            double playerLeft = player.getX();
            double playerRight = player.getX() + player.getWidth();
            double playerTop = player.getY();
            double playerBottom = player.getY() + player.getHeight();

            double blockLeft = block.getX();
            double blockRight = block.getX() + block.getWidth();
            double blockTop = block.getY();
            double blockBottom = block.getY() + block.getHeight();

            double previousLeft = solidPreviousX;
            double previousRight = solidPreviousX + player.getWidth();
            double previousTop = player.getPreviousY();
            double previousBottom = player.getPreviousY() + player.getHeight();

            if (player.getVelocityY() >= 0 && previousBottom <= blockTop + 16) {
                player.landOn(blockTop);
                continue;
            }

            if (player.getVelocityY() < 0 && previousTop >= blockBottom - 16) {
                player.hitCeiling(blockBottom);
                continue;
            }

            if (previousRight <= blockLeft) {
                player.setPosition(blockLeft - player.getWidth(), player.getY());
                continue;
            }

            if (previousLeft >= blockRight) {
                player.setPosition(blockRight, player.getY());
                continue;
            }

            double overlapLeft = playerRight - blockLeft;
            double overlapRight = blockRight - playerLeft;
            double overlapTop = playerBottom - blockTop;
            double overlapBottom = blockBottom - playerTop;
            double smallest = Math.min(Math.min(overlapLeft, overlapRight), Math.min(overlapTop, overlapBottom));

            if (smallest == overlapTop) {
                player.landOn(blockTop);
            } else if (smallest == overlapBottom) {
                player.hitCeiling(blockBottom);
            } else if (smallest == overlapLeft) {
                player.setPosition(blockLeft - player.getWidth(), player.getY());
            } else {
                player.setPosition(blockRight, player.getY());
            }
        }
    }

    protected void clampSolidPlayer() {
        if (player.getX() < 0) {
            player.setPosition(0, player.getY());
        }

        if (player.getX() > config.getWorldWidth() - player.getWidth()) {
            player.setPosition(config.getWorldWidth() - player.getWidth(), player.getY());
        }

        if (player.getY() > config.getWorldHeight() + 100) {
            onSolidPlayerOutOfWorld();
        }
    }

    protected void onSolidPlayerOutOfWorld() {
        player.resetToSpawn();
        solidPreviousX = player.getX();
    }
   
    protected double getSpawnX() {
        return 60;
    }

    protected double getSpawnY() {
        return 420;
    }

    private void createPlayer() {
        player = new Player(getSpawnX(), getSpawnY(), config.getPlayerWidth(), config.getPlayerHeight(), config.getPlayerColor());
        root.getChildren().add(player.getNode());
    }

    private void createPauseLayer() {
        pauseMenu = createPauseMenu();
        pauseMenu.setVisible(false);
        pauseMenu.setManaged(false);

        overlay = new StackPane(pauseMenu);
        overlay.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        overlay.setMinSize(config.getWorldWidth(), config.getWorldHeight());
        overlay.setVisible(false);
        overlay.setManaged(false);
        overlay.getStyleClass().add("overlay-backdrop");
    }

    private VBox createPauseMenu() {
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

        Button nextButton = new Button(getNextButtonText());
        nextButton.getStyleClass().add("primary-button");
        nextButton.setDisable(getNextLevelId() == 0);
        nextButton.setOnAction(event -> onGoalReached());

        VBox menu = new VBox(14, title, resumeButton, menuButton, previousButton, nextButton);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(28));
        menu.setMaxWidth(260);
        menu.getStyleClass().add("pause-panel");
        return menu;
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

        timer = new AnimationTimer() {
            private long lastFrame = -1;

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
    }

    private void update(double deltaSeconds) {
        if (paused) {
            return;
        }

        for (MovePlatform platform : movePlatforms) {
            platform.update(deltaSeconds);
        }

        if (activeMovePlatform != null) {
            player.moveBy(activeMovePlatform.getDeltaX(), activeMovePlatform.getDeltaY());
        }
        solidPreviousX = player.getX();

        player.handleInput(activeKeys, config.getControlConfig(), config.getMoveSpeed(), config.getJumpVelocity());
        player.applyPhysics(deltaSeconds, config.getGravity());
        player.setOnGround(false);   
        
        resolveCollisions();
        resolveSolidCollisions();
        clampPlayer();
        updatePushableBlocks(deltaSeconds);
        updateFloorButtons();
        updatePortals(deltaSeconds);
        onAfterUpdate(deltaSeconds);
        checkGoal();
    }

    private void resolveCollisions() {
        activeMovePlatform = null;

        for (Block block : blocks) {
            if (!player.getBounds().intersects(block.getBounds())) {
                continue;
            }

            double previousBottom = player.getPreviousY() + player.getHeight();
            if (player.getVelocityY() >= 0 && previousBottom <= block.getY() + 16) {
                player.landOn(block.getY());
            }
        }

        for (MovePlatform platform : movePlatforms) {
            if (!player.getBounds().intersects(platform.getBounds())) {
                continue;
            }

            double playerLeft = player.getX();
            double playerRight = player.getX() + player.getWidth();
            double playerTop = player.getY();
            double playerBottom = player.getY() + player.getHeight();

            double blockLeft = platform.getX();
            double blockRight = platform.getX() + platform.getWidth();
            double blockTop = platform.getY();
            double blockBottom = platform.getY() + platform.getHeight();

            double previousLeft = playerLeft;
            double previousRight = playerRight;
            if (platform.getDirection() == MovePlatform.Direction.HORIZONTAL) {
                previousLeft -= platform.getDeltaX();
                previousRight -= platform.getDeltaX();
            }

            double previousTop = player.getPreviousY();
            double previousBottom = player.getPreviousY() + player.getHeight();

            if (player.getVelocityY() >= 0 && previousBottom <= blockTop + 16) {
                player.landOn(blockTop);
                activeMovePlatform = platform;
                continue;
            }

            if (player.getVelocityY() < 0 && previousTop >= blockBottom - 16) {
                player.hitCeiling(blockBottom);
                continue;
            }

            if (previousRight <= blockLeft) {
                player.setPosition(blockLeft - player.getWidth(), player.getY());
                continue;
            }

            if (previousLeft >= blockRight) {
                player.setPosition(blockRight, player.getY());
                continue;
            }

            double overlapLeft = playerRight - blockLeft;
            double overlapRight = blockRight - playerLeft;
            double overlapTop = playerBottom - blockTop;
            double overlapBottom = blockBottom - playerTop;
            double smallest = Math.min(Math.min(overlapLeft, overlapRight), Math.min(overlapTop, overlapBottom));

            if (smallest == overlapTop) {
                player.landOn(blockTop);
                activeMovePlatform = platform;
            } else if (smallest == overlapBottom) {
                player.hitCeiling(blockBottom);
            } else if (smallest == overlapLeft) {
                player.setPosition(blockLeft - player.getWidth(), player.getY());
            } else {
                player.setPosition(blockRight, player.getY());
            }
        }

        for (Trap trap : traps) {
            if (player.getBounds().intersects(trap.getBounds())) {
                player.resetToSpawn();
                return;
            }
        }
    }

    private void clampPlayer() {
        if (player.getX() < 0) {
            player.setPosition(0, player.getY());
        }

        if (player.getX() > config.getWorldWidth() - player.getWidth()) {
            player.setPosition(config.getWorldWidth() - player.getWidth(), player.getY());
        }

        if (player.getY() > config.getWorldHeight() + 100) {
            player.resetToSpawn();
        }
    }

    private void checkGoal() {
        if (!changingLevel && goal != null && player.getBounds().intersects(goal.getBoundsInParent())) {
            onGoalReached();
        }
    }

    private void togglePause(boolean newState) {
        paused = newState;
        overlay.setVisible(newState);
        overlay.setManaged(newState);
        pauseMenu.setVisible(newState);
        pauseMenu.setManaged(newState);

        if (newState) {
            activeKeys.clear();
        }
    }

    private void start() {
        if (timer != null) {
            timer.start();
        }
    }

    private void stop() {
        if (timer != null) {
            timer.stop();
        }
    }
    
    
    /*
     * Each level overrides this to provide its own background image path.
     * Returning null means the level keeps the plain white background.
     */
    protected String getBackgroundImagePath() {
        return null;
    }   
    
    protected void addPushableBlock(double x, double y, double width, double height, Color color) {
        PushableBlock block = new PushableBlock(x, y, width, height, color);
        pushableBlocks.add(block);
        root.getChildren().add(block.getNode());
    }

    // ADD THIS NEW METHOD:
    protected void addPushableBlock(double x, double y, double width, double height, Color color, String imagePath) {
        PushableBlock block = new PushableBlock(x, y, width, height, color, imagePath);
        pushableBlocks.add(block);
        root.getChildren().add(block.getNode());
    }
    
    private void updatePushableBlocks(double deltaSeconds) {
        for (PushableBlock pb : pushableBlocks) {

            // 1. Player interaction
            if (player.getBounds().intersects(pb.getBounds())) {
                double prevPlayerBottom = player.getPreviousY() + player.getHeight();
                boolean isLandingOnTop = player.getVelocityY() >= 0 && prevPlayerBottom <= pb.getY() + 8;

                if (isLandingOnTop) {
                    player.landOn(pb.getY());
                } else {
                    double playerCenter = player.getX() + player.getWidth() / 2.0;
                    double blockCenter = pb.getX() + pb.getWidth() / 2.0;
                    double pushSpeed = config.getMoveSpeed() * 0.6;

                    if (playerCenter < blockCenter) {
                        pb.setVelocityX(pushSpeed);
                        player.setPosition(pb.getX() - player.getWidth(), player.getY());
                    } else {
                        pb.setVelocityX(-pushSpeed);
                        player.setPosition(pb.getX() + pb.getWidth(), player.getY());
                    }
                }
            }

            // 2. Apply gravity + horizontal movement
            pb.applyPhysics(deltaSeconds, config.getGravity());

            // 3. Resolve block landing on normal (one-way) platforms
            pb.setOnGround(false);
            for (Block block : blocks) {
                if (pb.getBounds().intersects(block.getBounds())) {
                    if (pb.getVelocityY() >= 0 && pb.getPreviousY() + pb.getHeight() <= block.getY() + 16) {
                        pb.landOn(block.getY());
                    }
                }
            }

            // 3b. Resolve block collision with SOLID blocks (full walls/floors)
            for (SolidBlock solid : solidBlocks) {
                if (!pb.getBounds().intersects(solid.getBounds())) {
                    continue;
                }

                double pbLeft = pb.getX();
                double pbRight = pb.getX() + pb.getWidth();
                double pbTop = pb.getY();
                double pbBottom = pb.getY() + pb.getHeight();

                double solidLeft = solid.getX();
                double solidRight = solid.getX() + solid.getWidth();
                double solidTop = solid.getY();
                double solidBottom = solid.getY() + solid.getHeight();

                double prevTop = pb.getPreviousY();
                double prevBottom = prevTop + pb.getHeight();
                double prevLeft = pb.getPreviousX();
                double prevRight = prevLeft + pb.getWidth();

                // Landing on top of the solid block
                if (pb.getVelocityY() >= 0 && prevBottom <= solidTop + 16) {
                    pb.landOn(solidTop);
                    continue;
                }

                // Hitting the underside of the solid block
                if (pb.getVelocityY() < 0 && prevTop >= solidBottom - 16) {
                    pb.setPosition(pb.getX(), solidBottom);
                    pb.setVelocityY(0);
                    continue;
                }

                // Side collision - approaching from the left
                if (prevRight <= solidLeft) {
                    pb.setPosition(solidLeft - pb.getWidth(), pb.getY());
                    pb.stopHorizontalMovement();
                    continue;
                }

                // Side collision - approaching from the right
                if (prevLeft >= solidRight) {
                    pb.setPosition(solidRight, pb.getY());
                    pb.stopHorizontalMovement();
                    continue;
                }

                // Fallback: resolve using smallest overlap
                double overlapLeft = pbRight - solidLeft;
                double overlapRight = solidRight - pbLeft;
                double overlapTop = pbBottom - solidTop;
                double overlapBottom = solidBottom - pbTop;
                double smallest = Math.min(Math.min(overlapLeft, overlapRight), Math.min(overlapTop, overlapBottom));

                if (smallest == overlapTop) {
                    pb.landOn(solidTop);
                } else if (smallest == overlapBottom) {
                    pb.setPosition(pb.getX(), solidBottom);
                    pb.setVelocityY(0);
                } else if (smallest == overlapLeft) {
                    pb.setPosition(solidLeft - pb.getWidth(), pb.getY());
                    pb.stopHorizontalMovement();
                } else {
                    pb.setPosition(solidRight, pb.getY());
                    pb.stopHorizontalMovement();
                }
            }

            // ============================================================
            // 3c. NEW: Resolve block landing on MOVING PLATFORMS (lifts)
            // ============================================================
            for (MovePlatform platform : movePlatforms) {
                if (!pb.getBounds().intersects(platform.getBounds())) {
                    continue;
                }

                double prevBottom = pb.getPreviousY() + pb.getHeight();
                double platformTop = platform.getY();

                // Box lands on top of the moving platform
                if (pb.getVelocityY() >= 0 && prevBottom <= platformTop + 16) {
                    pb.landOn(platformTop);
                    continue;
                }

                // Side collision with moving platform
                double prevRight = pb.getPreviousX() + pb.getWidth();
                double prevLeft = pb.getPreviousX();
                double platformLeft = platform.getX();
                double platformRight = platform.getX() + platform.getWidth();

                if (prevRight <= platformLeft) {
                    pb.setPosition(platformLeft - pb.getWidth(), pb.getY());
                    pb.stopHorizontalMovement();
                } else if (prevLeft >= platformRight) {
                    pb.setPosition(platformRight, pb.getY());
                    pb.stopHorizontalMovement();
                }
            }

            // 4. Resolve block-to-block collisions (Stacking & Walls)
            for (PushableBlock other : pushableBlocks) {
                if (pb == other) continue;

                if (pb.getBounds().intersects(other.getBounds())) {
                    double prevBottom = pb.getPreviousY() + pb.getHeight();

                    if (pb.getVelocityY() >= 0 && prevBottom <= other.getY() + 16) {
                        pb.landOn(other.getY());
                    } else {
                        if (pb.getVelocityX() > 0 && pb.getX() < other.getX()) {
                            pb.setPosition(other.getX() - pb.getWidth(), pb.getY());
                            pb.stopHorizontalMovement();
                        } else if (pb.getVelocityX() < 0 && pb.getX() > other.getX()) {
                            pb.setPosition(other.getX() + other.getWidth(), pb.getY());
                            pb.stopHorizontalMovement();
                        }
                    }
                }
            }

            // 5. Keep inside the screen
            if (pb.getX() < 0) {
                pb.setPosition(0, pb.getY());
            }
            if (pb.getX() + pb.getWidth() > config.getWorldWidth()) {
                pb.setPosition(config.getWorldWidth() - pb.getWidth(), pb.getY());
            }
        }
    }
    /*
     * Checks every floor button and updates its pressed state.
     * A button is pressed when the player OR any pushable block overlaps it.
     */
    private void updateFloorButtons() {
        for (FloorButton button : floorButtons) {
            boolean pressed = false;

            if (player.getBounds().intersects(button.getBounds())) {
                pressed = true;
            }

            if (!pressed) {
                for (PushableBlock pb : pushableBlocks) {
                    if (pb.getBounds().intersects(button.getBounds())) {
                        pressed = true;
                        break;
                    }
                }
            }

            button.setPressed(pressed);
        }
    }
    /*
     * Optional hook called every frame after the standard update logic.
     * Levels can override this to add custom per-frame behavior such as
     * triggered platforms, timers, or scripted events.
     */
    protected void onAfterUpdate(double deltaSeconds) {
        // Default: do nothing.
    }
    
    protected Portal addPortal(double x, double y, double width, double height, String imagePath) {
        Portal portal = new Portal(x, y, width, height, imagePath);
        portals.add(portal);
        root.getChildren().add(portal.getNode());
        return portal;
    }

    /*
     * Teleports the player and pushable blocks when they enter a portal.
     */
    private void updatePortals(double deltaSeconds) {
        // Reduce cooldowns
        for (Portal portal : portals) {
            portal.tickCooldown(deltaSeconds);
        }

        for (Portal portal : portals) {
            Portal exit = portal.getLinkedPortal();
            if (exit == null || portal.isOnCooldown()) {
                continue;
            }

            // Teleport the player
            if (player.getBounds().intersects(portal.getBounds())) {
                double newX = exit.getExitX(player.getWidth());
                double newY = exit.getExitY(player.getHeight());
                player.setPosition(newX, newY);

                portal.startCooldown();
                exit.startCooldown();
                continue;
            }

            // Teleport pushable blocks
            for (PushableBlock pb : pushableBlocks) {
                if (pb.getBounds().intersects(portal.getBounds())) {
                    double newX = exit.getExitX(pb.getWidth());
                    double newY = exit.getExitY(pb.getHeight());
                    pb.setPosition(newX, newY);

                    portal.startCooldown();
                    exit.startCooldown();
                    break;
                }
            }
        }
    }

}
