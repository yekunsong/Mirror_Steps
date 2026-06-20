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
 * Simplified Settings screen.
 *
 * This page is currently a shared information page rather than a full settings system.
 * It exists so the UI owner in the team has a clear file to extend later.
 *
 * Relationship notes:
 * - this class is not a child of MenuView
 * - AppRouter opens this screen and provides the back callback
 *
 * Future extension directions:
 * - audio sliders
 * - custom key binding display
 * - fullscreen toggle if the team later wants it back
 * - difficulty or accessibility options
 */
public final class SettingsView {

    /*
     * Builds the settings screen using exactly the same window size as every other scene.
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
