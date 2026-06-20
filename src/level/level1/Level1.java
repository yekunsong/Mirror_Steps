package level.level1;

import config.GameConfig;
import core.AppRouter;
import entity.Block;
import entity.Player;
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
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/*
 * Level 1 scene file.
 *
 * This file is intentionally self-contained so the Level 1 owner can maintain it
 * without reading shared runtime or factory code.
 *
 * Responsibilities of this file:
 * - create the Level 1 layout
 * - create the player
 * - create the goal area
 * - run the update loop
 * - handle collisions for this level
 * - trigger scene switching through AppRouter
 *
 * Relationship notes:
 * - Level1 is not a child of Level2 or Level3
 * - all three level classes are independent siblings
 * - Level1 uses Player and Block objects rather than inheriting from them
 *
 * Future extension directions:
 * - change the block layout
 * - add decorative background nodes
 * - add unique mechanics only for Level 1
 * - add text hints for beginner movement
 */
public final class Level1 {

    private final GameConfig config;
    private final AppRouter router;
    private final Pane root = new Pane();
    private final List<Block> blocks = new ArrayList<>();
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private Player player;
    private Rectangle goal;
    private AnimationTimer timer;
    private boolean changingLevel;
    private VBox pauseMenu;
    private boolean paused;

    public Level1(GameConfig config, AppRouter router) {
        this.config = config;
        this.router = router;
    }

    /*
     * Builds the full scene for Level 1.
     * The scene size is fixed to the same width and height used by Menu and Settings.
     */
    public Scene createScene() {
        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        root.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        player = new Player(60, 420, config.getPlayerWidth(), config.getPlayerHeight(), config.getPlayerColor());
        root.getChildren().add(player.getNode());

        addBlock(0, config.getWorldHeight() - 40, config.getWorldWidth(), 40);
        addBlock(160, 430, 180, 24);
        addBlock(390, 360, 160, 24);
        addBlock(610, 290, 160, 24);

        goal = new Rectangle(36, 72, config.getGoalColor());
        goal.setLayoutX(config.getWorldWidth() - 90);
        goal.setLayoutY(218);
        root.getChildren().add(goal);

        pauseMenu = createPauseMenu("Level 1", 0, 2);
        pauseMenu.setVisible(false);
        pauseMenu.setManaged(false);

        StackPane overlay = new StackPane(pauseMenu);
        overlay.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        overlay.setMinSize(config.getWorldWidth(), config.getWorldHeight());
        overlay.setVisible(false);
        overlay.setManaged(false);
        overlay.getStyleClass().add("overlay-backdrop");

        StackPane container = new StackPane(root, overlay);
        StackPane.setAlignment(pauseMenu, Pos.CENTER);

        Scene scene = new Scene(container, config.getStageWidth(), config.getStageHeight());
        installInput(scene);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                togglePause(overlay, !paused);
                return;
            }
            if (!paused) {
                activeKeys.add(event.getCode());
            }
        });
        scene.setOnKeyReleased(event -> activeKeys.remove(event.getCode()));
        start();
        return scene;
    }

    private VBox createPauseMenu(String titleText, int previousLevel, int nextLevel) {
        Label title = new Label(titleText);
        title.getStyleClass().add("pause-title");

        Button menuButton = new Button("Menu");
        menuButton.getStyleClass().add("secondary-button");
        menuButton.setOnAction(event -> switchToMenu());

        Button previousButton = new Button("Prev");
        previousButton.getStyleClass().add("secondary-button");
        previousButton.setDisable(previousLevel == 0);
        previousButton.setOnAction(event -> switchToLevel(previousLevel));

        Button nextButton = new Button("Next");
        nextButton.getStyleClass().add("primary-button");
        nextButton.setDisable(nextLevel == 0);
        nextButton.setOnAction(event -> switchToLevel(nextLevel));

        Button resumeButton = new Button("Resume");
        resumeButton.getStyleClass().add("secondary-button");
        resumeButton.setOnAction(event -> togglePause((StackPane) pauseMenu.getParent(), false));

        VBox menu = new VBox(14, title, resumeButton, menuButton, previousButton, nextButton);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(28));
        menu.setMaxWidth(260);
        menu.getStyleClass().add("pause-panel");
        return menu;
    }

    /*
     * Level-local helper for adding terrain.
     * If the team wants new platforms, this is one of the simplest places to edit.
     */
    private void addBlock(double x, double y, double width, double height) {
        Block block = new Block(x, y, width, height, config.getBlockColor());
        blocks.add(block);
        root.getChildren().add(block.getNode());
    }

    /*
     * Registers keyboard state and creates the timer-based game loop.
     */
    private void installInput(Scene scene) {
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

    /*
     * One frame of gameplay update.
     */
    private void update(double deltaSeconds) {
        if (paused) {
            return;
        }
        player.handleInput(activeKeys, config.getControlConfig(), config.getMoveSpeed(), config.getJumpVelocity());
        player.applyPhysics(deltaSeconds, config.getGravity());
        resolveCollisions();
        clampPlayer();
        checkGoal(2);
    }

    private void togglePause(StackPane overlay, boolean newState) {
        paused = newState;
        overlay.setVisible(newState);
        overlay.setManaged(newState);
        pauseMenu.setVisible(newState);
        pauseMenu.setManaged(newState);
        if (newState) {
            activeKeys.clear();
        }
    }

    /*
     * Handles simple top-side landing collisions between player and blocks.
     * This is deliberately simple so the logic is easy for students to read.
     */
    private void resolveCollisions() {
        player.setOnGround(false);

        for (Block block : blocks) {
            if (!player.getBounds().intersects(block.getBounds())) {
                continue;
            }

            double previousBottom = player.getPreviousY() + player.getHeight();
            if (player.getVelocityY() >= 0 && previousBottom <= block.getY() + 16) {
                player.landOn(block.getY());
            }
        }
    }

    /*
     * Keeps the player inside the play area and respawns them if they fall off-screen.
     */
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

    /*
     * Automatic level completion: touching the goal changes to the next level.
     */
    private void checkGoal(int nextLevel) {
        if (!changingLevel && player.getBounds().intersects(goal.getBoundsInParent())) {
            switchToLevel(nextLevel);
        }
    }

    /*
     * Scene changes are protected by `changingLevel` so one collision does not trigger
     * multiple scene switches in the same frame.
     */
    private void switchToLevel(int level) {
        if (changingLevel || level == 0) {
            return;
        }
        changingLevel = true;
        stop();
        router.showLevel(level);
    }

    private void switchToMenu() {
        if (changingLevel) {
            return;
        }
        changingLevel = true;
        stop();
        router.showMenu();
    }

    /*
     * Timer start/stop helpers are separated to keep scene switching safe.
     */
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
}
