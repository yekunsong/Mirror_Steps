package ui;

import config.GameConfig;
import java.util.List;
import java.util.function.IntConsumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import level.LevelModule;

/*
 * Level selection view.
 * This UI-layer class belongs to the shared presentation package and turns the
 * registered level modules into selectable cards.
 * Teammates can expand it with unlock states, thumbnails, or tags later.
 */
public final class LevelSelectView {

    public Scene createScene(GameConfig config, List<LevelModule> levels, IntConsumer onLevelSelected, Runnable backToMenu) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(0));
        root.setMinSize(config.getStageWidth(), config.getStageHeight());
        root.setPrefSize(config.getStageWidth(), config.getStageHeight());

        Label title = new Label("Level Select");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Choose a level. Each one has its own configuration and can be expanded by your team.");
        subtitle.getStyleClass().add("app-subtitle");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(520);

        VBox cards = new VBox(14);
        for (LevelModule level : levels) {
            var levelConfig = level.getConfig();
            Button levelButton = new Button(
                    levelConfig.getTitle() + "\n" +
                    levelConfig.getSubtitle() + "\n" +
                    levelConfig.getMechanicLabel()
            );
            levelButton.getStyleClass().add("level-button");
            levelButton.setWrapText(true);
            levelButton.setMaxWidth(Double.MAX_VALUE);
            levelButton.setMinHeight(110);
            levelButton.setOnAction(event -> onLevelSelected.accept(level.getId()));
            cards.getChildren().add(levelButton);
        }

        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("secondary-button");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setOnAction(event -> backToMenu.run());

        VBox card = new VBox(14, title, subtitle, cards, backButton);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(40));
        card.prefWidthProperty().bind(root.widthProperty().multiply(0.52));
        card.maxWidthProperty().bind(root.widthProperty().multiply(0.62));
        card.prefHeightProperty().bind(root.heightProperty().multiply(0.76));
        card.getStyleClass().add("panel-card");

        VBox stageCenter = new VBox(card);
        stageCenter.setAlignment(Pos.CENTER);
        stageCenter.setFillWidth(true);
        stageCenter.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        root.setCenter(stageCenter);

        return new Scene(root, config.getStageWidth(), config.getStageHeight());
    }
}
