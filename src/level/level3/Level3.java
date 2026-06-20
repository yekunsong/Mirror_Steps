package level.level3;

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
 * Concrete scene implementation for Level 3.
 *
 * Architectural role:
 * - This class implements the final stage in the simplified three-level structure.
 * - It follows the same pattern as Level1 and Level2 while changing the final
 *   transition behavior to return to the menu.
 *
 * Responsibilities:
 * - define the Level 3 layout
 * - maintain player interaction and collision logic
 * - provide the final completion route back to the menu
 *
 * Extension guidance:
 * - A future revision can replace the return-to-menu behavior with a dedicated ending
 *   scene if required.
 */
public final class Level3 {

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

    public Level3(GameConfig config, AppRouter router) {
        this.config = config;
        this.router = router;
    }

    /*
     * Builds and returns the complete Level 3 scene.
     */
    public Scene createScene() {
        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        root.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        player = new Player(60, 420, config.getPlayerWidth(), config.getPlayerHeight(), config.getPlayerColor());
        root.getChildren().add(player.getNode());

        addBlock(0, config.getWorldHeight() - 40, 220, 40);
        addBlock(300, config.getWorldHeight() - 40, 240, 40);
        addBlock(620, config.getWorldHeight() - 40, config.getWorldWidth() - 620, 40);
        addBlock(180, 360, 150, 24);
        addBlock(520, 280, 160, 24);

        goal = new Rectangle(36, 72, config.getGoalColor());
        goal.setLayoutX(config.getWorldWidth() - 90);
        goal.setLayoutY(208);
        root.getChildren().add(goal);

        pauseMenu = createPauseMenu("Level 3", 2);
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

    /*
     * Creates the pause menu used in the final level.
     *
     * The final action button is labelled "Finish" because completing Level 3 returns
     * the application to the menu scene rather than opening another level.
     */
    private VBox createPauseMenu(String titleText, int previousLevel) {
        Label title = new Label(titleText);
        title.getStyleClass().add("pause-title");

        Button menuButton = new Button("Menu");
        menuButton.getStyleClass().add("secondary-button");
        menuButton.setOnAction(event -> switchToMenu());

        Button previousButton = new Button("Prev");
        previousButton.getStyleClass().add("secondary-button");
        previousButton.setDisable(previousLevel == 0);
        previousButton.setOnAction(event -> switchToLevel(previousLevel));

        Button finishButton = new Button("Finish");
        finishButton.getStyleClass().add("primary-button");
        finishButton.setOnAction(event -> switchToMenu());

        Button resumeButton = new Button("Resume");
        resumeButton.getStyleClass().add("secondary-button");
        resumeButton.setOnAction(event -> togglePause((StackPane) pauseMenu.getParent(), false));

        VBox menu = new VBox(14, title, resumeButton, menuButton, previousButton, finishButton);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(28));
        menu.setMaxWidth(260);
        menu.getStyleClass().add("pause-panel");
        return menu;
    }

    private void addBlock(double x, double y, double width, double height) {
        Block block = new Block(x, y, width, height, config.getBlockColor());
        blocks.add(block);
        root.getChildren().add(block.getNode());
    }

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

    private void update(double deltaSeconds) {
        if (paused) {
            return;
        }
        player.handleInput(activeKeys, config.getControlConfig(), config.getMoveSpeed(), config.getJumpVelocity());
        player.applyPhysics(deltaSeconds, config.getGravity());
        resolveCollisions();
        clampPlayer();
        checkGoal();
    }

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
     * Returns to the menu scene when the player reaches the final goal area.
     */
    private void checkGoal() {
        if (!changingLevel && player.getBounds().intersects(goal.getBoundsInParent())) {
            switchToMenu();
        }
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
