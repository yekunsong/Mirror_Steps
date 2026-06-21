package level;

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

public abstract class BaseLevel {

    protected final GameConfig config;
    protected final AppRouter router;
    protected final Pane root = new Pane();
    protected final List<Block> blocks = new ArrayList<>();
    protected final Set<KeyCode> activeKeys = new HashSet<>();
    protected Player player;
    protected Rectangle goal;
    protected VBox pauseMenu;
    protected StackPane overlay;

    private AnimationTimer timer;
    private boolean changingLevel;
    private boolean paused;

    protected BaseLevel(GameConfig config, AppRouter router) {
        this.config = config;
        this.router = router;
    }

    public Scene createScene() {
        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        root.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

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

    protected void addBlock(double x, double y, double width, double height) {
        Block block = new Block(x, y, width, height, config.getBlockColor());
        blocks.add(block);
        root.getChildren().add(block.getNode());
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

    private void createPlayer() {
        player = new Player(60, 420, config.getPlayerWidth(), config.getPlayerHeight(), config.getPlayerColor());
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
}
