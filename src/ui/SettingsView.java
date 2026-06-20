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
 * Settings view.
 *
 * Architectural role:
 * - This class constructs the settings scene.
 * - In the current simplified framework, it functions as a placeholder page and
 *   extension point rather than a complete settings subsystem.
 *
 * Relationship note:
 * - AppRouter opens this scene and supplies the callback required to return to the
 *   menu scene.
 *
 * Extension guidance:
 * - A future revision can add audio controls, accessibility settings, key-binding
 *   summaries, or display options here while keeping the overall structure stable.
 */
public final class SettingsView {

    /*
     * Builds and returns the settings scene using the same fixed dimensions as all
     * other application scenes.
     */
    public Scene createScene(GameConfig config, Runnable backToMenu) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(0));
        root.setMinSize(config.getStageWidth(), config.getStageHeight());
        root.setPrefSize(config.getStageWidth(), config.getStageHeight());
        root.setStyle("-fx-background-color: white;");

        Label title = new Label("Settings");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("This page is intentionally simple. Add future shared options here only.");
        subtitle.getStyleClass().add("app-subtitle");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(560);

        Label controls = new Label("Controls: A / D move, W or Space jump, ESC is reserved for future expansion.");
        controls.getStyleClass().add("sidebar-text");
        controls.setWrapText(true);

        Label ownership = new Label("Recommended owner: the teammate responsible for shared UI pages.");
        ownership.getStyleClass().add("sidebar-text");
        ownership.setWrapText(true);

        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("primary-button");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setOnAction(event -> backToMenu.run());

        VBox card = new VBox(16, title, subtitle, controls, ownership, backButton);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(40));
        card.prefWidthProperty().bind(root.widthProperty().multiply(0.5));
        card.maxWidthProperty().bind(root.widthProperty().multiply(0.58));
        card.getStyleClass().add("panel-card");

        VBox stageCenter = new VBox(card);
        stageCenter.setAlignment(Pos.CENTER);
        stageCenter.setFillWidth(true);
        stageCenter.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        root.setCenter(stageCenter);

        return new Scene(root, config.getStageWidth(), config.getStageHeight());
    }
}
