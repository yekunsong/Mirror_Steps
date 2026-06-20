package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/*
 * Compact pause overlay shown from gameplay.
 * This UI-layer class belongs to the shared presentation package and provides the
 * ESC menu used by every level.
 * Keep this menu focused on in-level actions so the gameplay scene stays clean.
 */
public final class PauseView {

    public VBox createOverlay(double stageWidth, double stageHeight,
                              Runnable resumeGame, Runnable restartLevel, Runnable nextLevel, Runnable previousLevel, Runnable backToMenu,
                              boolean canGoPrevious, boolean canGoNext) {
        /*
         * The overlay is intentionally small so the player still feels inside the level.
         */
        Label title = new Label("Paused");
        title.getStyleClass().add("pause-title");

        Button resumeButton = new Button("Resume");
        resumeButton.getStyleClass().add("pause-button-primary");
        resumeButton.setMaxWidth(Double.MAX_VALUE);
        resumeButton.setOnAction(event -> resumeGame.run());

        Button previousButton = new Button("Previous");
        previousButton.getStyleClass().add("pause-button");
        previousButton.setMaxWidth(Double.MAX_VALUE);
        previousButton.setOnAction(event -> previousLevel.run());
        previousButton.setDisable(!canGoPrevious);

        Button restartButton = new Button("Restart");
        restartButton.getStyleClass().add("pause-button");
        restartButton.setMaxWidth(Double.MAX_VALUE);
        restartButton.setOnAction(event -> restartLevel.run());

        Button nextButton = new Button("Next");
        nextButton.getStyleClass().add("pause-button");
        nextButton.setMaxWidth(Double.MAX_VALUE);
        nextButton.setOnAction(event -> nextLevel.run());
        nextButton.setDisable(!canGoNext);

        Button menuButton = new Button("Back to level select");
        menuButton.getStyleClass().add("pause-button");
        menuButton.setMaxWidth(Double.MAX_VALUE);
        menuButton.setOnAction(event -> backToMenu.run());

        VBox overlay = new VBox(10, title, resumeButton, previousButton, restartButton, nextButton, menuButton);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPadding(new Insets(28));
        overlay.setPrefWidth(Math.max(320, stageWidth * 0.22));
        overlay.setMaxWidth(Math.max(360, stageWidth * 0.26));
        overlay.setMaxHeight(stageHeight * 0.7);
        overlay.getStyleClass().add("pause-overlay");
        return overlay;
    }
}
