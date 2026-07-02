package ui;

import config.GameConfig;
import core.AppRouter;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public final class LevelsView {

    private ImageView makeImageButton(String imagePath, double width, double height, Runnable onClick) {
        Image img = new Image(AppRouter.resourceUri("Pictures/UI/" + imagePath));
        ImageView iv = new ImageView(img);
        iv.setFitWidth(width);
        iv.setFitHeight(height);
        iv.setPreserveRatio(true);
        iv.setStyle("-fx-cursor: hand;");
        iv.setOnMouseEntered(e -> { iv.setScaleX(1.08); iv.setScaleY(1.08); });
        iv.setOnMouseExited(e -> { iv.setScaleX(1.0);  iv.setScaleY(1.0);  });
        iv.setOnMousePressed(e -> { iv.setScaleX(0.96); iv.setScaleY(0.96); });
        iv.setOnMouseReleased(e -> { iv.setScaleX(1.08); iv.setScaleY(1.08); });
        iv.setOnMouseClicked(e -> onClick.run());
        return iv;
    }

    public Scene createScene(GameConfig config, AppRouter router) {
        Image bgImage = new Image(AppRouter.resourceUri("Pictures/Backgrounds/main_background.png"));
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(config.getWorldWidth());
        bgView.setFitHeight(config.getWorldHeight());
        bgView.setPreserveRatio(false);

        Image cardImage = new Image(AppRouter.resourceUri("Pictures/UI/menu_map.png"));
        ImageView cardView = new ImageView(cardImage);
        cardView.setPreserveRatio(true);
        cardView.setFitWidth(700);

        Image titleImage = new Image(AppRouter.resourceUri("Pictures/UI/levels_title.png"));
        ImageView title = new ImageView(titleImage);
        title.setFitWidth(220);
        title.setPreserveRatio(true);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(14);
        grid.setVgap(14);
        for (int i = 1; i <= 14; i++) {
            int level = i;
            ImageView btn = makeImageButton("level" + i + ".png", 80, 80, () -> router.showLevel(level));
            int col = (i - 1) % 4;
            int row = (i - 1) / 4;
            grid.add(btn, col, row);
        }

        VBox content = new VBox(14, title, grid);
        content.setAlignment(Pos.CENTER);

        StackPane cardStack = new StackPane(cardView, content);
        cardStack.setAlignment(Pos.CENTER);

        // ── RETURN TO MENU BUTTON (top-left corner) ─────────────────────────────
        ImageView menuButton = makeImageButton("return.png", 120, 40, () -> router.showMenu());

        StackPane root = new StackPane(bgView, cardStack, menuButton);
        root.setAlignment(Pos.CENTER);
        StackPane.setAlignment(menuButton, Pos.TOP_LEFT);
        StackPane.setMargin(menuButton, new Insets(20, 0, 0, 20));
        root.setMinSize(config.getWorldWidth(), config.getWorldHeight());
        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight());

        return new Scene(root, config.getWorldWidth(), config.getWorldHeight());
    }
}
