package ui;

import config.GameConfig;
import core.AppRouter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/*
 * Main menu view.
 *
 * Architectural role:
 * - This class is responsible only for constructing the menu user interface.
 * - It must not contain gameplay rules, movement code, collision logic, or direct
 *   stage navigation logic.
 *
 * Interaction model:
 * - The router passes itself into this class.
 * - Each button directly calls a simple navigation method on AppRouter.
 * - The class therefore remains a view layer rather than becoming a controller for
 *   the whole application.
 *
 * Extension guidance:
 * - A future revision can add additional buttons such as Credits, Instructions, or
 *   Continue without changing the core architecture.
 */
public final class MenuView {

    /*
     * Builds and returns the menu scene using the shared fixed-size window settings.
     */
    public Scene createScene(GameConfig config, AppRouter router) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(0));
        root.setMinSize(config.getWorldWidth(), config.getWorldHeight());
        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight());
        root.setStyle("-fx-background-color: white;");

        Label title = new Label("Mirror Steps");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Minimal JavaFX platformer");
        subtitle.getStyleClass().add("app-subtitle");

        Label description = new Label("This version only keeps menu, settings, three levels, one player, and simple blocks.");
        description.getStyleClass().add("small-label");
        description.setWrapText(true);
        description.setMaxWidth(460);

        Button startButton = new Button("Start Game");
        startButton.getStyleClass().add("primary-button");
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setOnAction(event -> router.showLevel(1));

        Button level1Button = new Button("Open Level 1");
        level1Button.getStyleClass().add("secondary-button");
        level1Button.setMaxWidth(Double.MAX_VALUE);
        level1Button.setOnAction(event -> router.showLevel(1));

        Button settingsButton = new Button("Settings");
        settingsButton.getStyleClass().add("secondary-button");
        settingsButton.setMaxWidth(Double.MAX_VALUE);
        settingsButton.setOnAction(event -> router.showSettings());

        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().add("secondary-button");
        exitButton.setMaxWidth(Double.MAX_VALUE);
        exitButton.setOnAction(event -> router.closeApp());

        VBox card = new VBox(14, title, subtitle, description, startButton, level1Button, settingsButton, exitButton);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(44));
        card.prefWidthProperty().bind(root.widthProperty().multiply(0.42));
        card.maxWidthProperty().bind(root.widthProperty().multiply(0.48));
        card.prefHeightProperty().bind(root.heightProperty().multiply(0.56));
        card.setFillWidth(true);
        card.getStyleClass().add("panel-card");

        VBox stageCenter = new VBox(card);
        stageCenter.setAlignment(Pos.CENTER);
        stageCenter.setFillWidth(true);
        stageCenter.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        root.setCenter(stageCenter);

        return new Scene(root, config.getWorldWidth(), config.getWorldHeight());
    }
}
