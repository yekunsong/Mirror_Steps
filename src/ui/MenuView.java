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
 * Main menu view.
 * This UI-layer class belongs to the shared presentation package and should only
 * handle menu layout, not gameplay logic.
 * Teammates can extend this screen with credits, settings, or chapter selection later.
 */
public final class MenuView {

    public Scene createScene(GameConfig config, Runnable startGame, Runnable openLevelSelect, Runnable exitGame) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(0));
        root.setMinSize(config.getStageWidth(), config.getStageHeight());
        root.setPrefSize(config.getStageWidth(), config.getStageHeight());

        Label title = new Label("Mirror Steps");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("JavaFX platformer framework");
        subtitle.getStyleClass().add("app-subtitle");

        Label description = new Label("This build focuses on navigation, independent level settings and a reusable OOP structure.");
        description.getStyleClass().add("small-label");
        description.setWrapText(true);
        description.setMaxWidth(460);

        Button startButton = new Button("Start Game");
        startButton.getStyleClass().add("primary-button");
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setOnAction(event -> startGame.run());

        Button levelSelectButton = new Button("Level Select");
        levelSelectButton.getStyleClass().add("secondary-button");
        levelSelectButton.setMaxWidth(Double.MAX_VALUE);
        levelSelectButton.setOnAction(event -> openLevelSelect.run());

        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().add("secondary-button");
        exitButton.setMaxWidth(Double.MAX_VALUE);
        exitButton.setOnAction(event -> exitGame.run());

        VBox card = new VBox(14, title, subtitle, description, startButton, levelSelectButton, exitButton);
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
