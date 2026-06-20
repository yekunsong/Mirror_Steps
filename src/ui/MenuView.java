package ui;

import config.GameConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/*
 * Main menu screen.
 *
 * This class is a UI file only.
 * It should not contain gameplay physics, level collision logic, or player logic.
 *
 * Relationship notes:
 * - MenuView does not know how to switch scenes by itself
 * - AppRouter passes callback methods into this class
 * - when a button is pressed, MenuView only calls the callback
 *
 * This makes the screen easier for one teammate to maintain independently.
 *
 * Future extension directions:
 * - add a Credits button
 * - add an Instructions button
 * - add a Continue button if save data is added later
 */
public final class MenuView {

    /*
     * Builds the menu scene using the fixed shared size from GameConfig.
     * Every screen now uses the same width and height.
     */
    public Scene createScene(GameConfig config, Runnable startGame, Runnable openSettings, Runnable exitGame) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(0));
        root.setMinSize(config.getStageWidth(), config.getStageHeight());
        root.setPrefSize(config.getStageWidth(), config.getStageHeight());
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
        startButton.setOnAction(event -> startGame.run());

        Button level1Button = new Button("Open Level 1");
        level1Button.getStyleClass().add("secondary-button");
        level1Button.setMaxWidth(Double.MAX_VALUE);
        level1Button.setOnAction(event -> startGame.run());

        Button settingsButton = new Button("Settings");
        settingsButton.getStyleClass().add("secondary-button");
        settingsButton.setMaxWidth(Double.MAX_VALUE);
        settingsButton.setOnAction(event -> openSettings.run());

        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().add("secondary-button");
        exitButton.setMaxWidth(Double.MAX_VALUE);
        exitButton.setOnAction(event -> exitGame.run());

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

        return new Scene(root, config.getStageWidth(), config.getStageHeight());
    }
}
